package com.valerine.ar.core.codelabs.smartparking

import android.opengl.Matrix
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps.model.LatLng
import com.google.ar.core.Anchor
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.valerine.ar.core.examples.java.common.helpers.DisplayRotationHelper
import com.valerine.ar.core.examples.java.common.helpers.TrackingStateHelper
import com.valerine.ar.core.examples.java.common.samplerender.Framebuffer
import com.valerine.ar.core.examples.java.common.samplerender.Mesh
import com.valerine.ar.core.examples.java.common.samplerender.SampleRender
import com.valerine.ar.core.examples.java.common.samplerender.Shader
import com.valerine.ar.core.examples.java.common.samplerender.Texture
import com.valerine.ar.core.examples.java.common.samplerender.arcore.BackgroundRenderer
import java.io.IOException

class SmartParkingRenderer(val activity: SmartParkingActivity) :
    SampleRender.Renderer, DefaultLifecycleObserver {

    companion object {
        const val TAG = "SmartParkingRenderer"

        private const val Z_NEAR = 0.1f
        private const val Z_FAR = 1000f
    }

    private lateinit var backgroundRenderer: BackgroundRenderer
    private lateinit var virtualSceneFramebuffer: Framebuffer
    private var hasSetTextureNames = false

    // Virtual object (ARCore mark)
    private lateinit var virtualObjectMesh: Mesh
    private lateinit var virtualObjectShader: Shader
    private lateinit var virtualObjectTexture: Texture

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val modelViewMatrix = FloatArray(16) // view x model

    private val modelViewProjectionMatrix = FloatArray(16) // projection x view x model

    private val session get() = activity.arCoreSessionHelper.session

    private val displayRotationHelper = DisplayRotationHelper(activity)
    private val trackingStateHelper = TrackingStateHelper(activity)

    override fun onResume(owner: LifecycleOwner) {
        displayRotationHelper.onResume()
        hasSetTextureNames = false
    }

    override fun onPause(owner: LifecycleOwner) {
        displayRotationHelper.onPause()
    }

    override fun onSurfaceCreated(render: SampleRender) {
        try {
            backgroundRenderer = BackgroundRenderer(render)
            virtualSceneFramebuffer = Framebuffer(render, /*width=*/ 1, /*height=*/ 1)

            virtualObjectTexture =
                Texture.createFromAsset(
                    render,
                    "models/pointer.png",
                    Texture.WrapMode.CLAMP_TO_EDGE,
                    Texture.ColorFormat.SRGB,
                )

            virtualObjectMesh = Mesh.createFromAsset(render, "models/pointer.obj")

            virtualObjectShader =
                Shader.createFromAssets(
                    render,
                    "shaders/ar_unlit_object.vert",
                    "shaders/ar_unlit_object.frag",
                    /*defines=*/ null,
                )
                    .setTexture(
                        "u_Texture",
                        virtualObjectTexture,
                    )

            backgroundRenderer.setUseDepthVisualization(render, false)
            backgroundRenderer.setUseOcclusion(render, false)
            loadMark()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read a required asset file", e)
            showError("Failed to read a required asset file: $e")
        }
    }

    override fun onSurfaceChanged(render: SampleRender, width: Int, height: Int) {
        displayRotationHelper.onSurfaceChanged(width, height)
        virtualSceneFramebuffer.resize(width, height)
    }

    override fun onDrawFrame(render: SampleRender) {
        val session = session ?: return

        if (!hasSetTextureNames) {
            session.setCameraTextureNames(intArrayOf(backgroundRenderer.cameraColorTexture.textureId))
            hasSetTextureNames = true
        }

        // -- Update per-frame state

        // Notify ARCore session that the view size changed so that the perspective matrix and
        // the video background can be properly adjusted.
        displayRotationHelper.updateSessionIfNeeded(session)

        // Obtain the current frame from ARSession. When the configuration is set to
        // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
        // camera framerate.
        val frame =
            try {
                session.update()
            } catch (e: CameraNotAvailableException) {
                Log.e(TAG, "Camera not available during onDrawFrame", e)
                showError("Camera not available. Try restarting the app.")
                return
            }

        val camera = frame.camera

        // BackgroundRenderer.updateDisplayGeometry must be called every frame to update the coordinates
        // used to draw the background camera image.
        backgroundRenderer.updateDisplayGeometry(frame)

        // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
        trackingStateHelper.updateKeepScreenOnFlag(camera.trackingState)

        // -- Draw background
        if (frame.timestamp != 0L) {
            // Suppress rendering if the camera did not produce the first frame yet. This is to avoid
            // drawing possible leftover data from previous sessions if the texture is reused.
            backgroundRenderer.drawBackground(render)
        }

        // If not tracking, don't draw 3D objects.
        if (camera.trackingState == TrackingState.PAUSED) {
            return
        }

        // Get projection matrix.
        camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR)

        // Get camera matrix and draw.
        camera.getViewMatrix(viewMatrix, 0)

        render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f)

        val earth = session.earth
        if (earth?.trackingState == TrackingState.TRACKING) {
            val cameraGeospatialPose = earth.cameraGeospatialPose
            activity.view.mapView?.updateMapPosition(
                latitude = cameraGeospatialPose.latitude,
                longitude = cameraGeospatialPose.longitude,
                heading = cameraGeospatialPose.heading,
            )
        }

        // Draw the placed anchor, if it exists.
        earthAnchor?.let {
            render.renderCompassAtAnchor(it)
        }

        // Compose the virtual scene with the background.
        backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR)
    }

    private var earthAnchor: Anchor? = null

    private fun loadMark() {
        val altitude = activity.view.sharedPreferences.getFloat("altitude", 0f).toDouble()
        val latitude = activity.view.sharedPreferences.getFloat("latitude", 0f).toDouble()
        val longitude = activity.view.sharedPreferences.getFloat("longitude", 0f).toDouble()
        if (longitude != 0.toDouble() && latitude != 0.toDouble() && altitude != 0.toDouble()) {
            val earth = session?.earth ?: return
            earthAnchor?.detach()

            val qx = 0f
            val qy = 0f
            val qz = 0f
            val qw = 1f

            val latLng = LatLng(latitude, longitude)

            earthAnchor = earth.createAnchor(latitude, longitude, altitude, qx, qy, qz, qw)

            activity.runOnUiThread(
                Runnable {
                    kotlin.run {
                        activity.view.mapView?.carMarker?.apply {
                            position = latLng
                            isVisible = true
                        }
                    }
                }
            )
        }
    }

    fun placeMark() {
        val earth = session?.earth ?: return
        if (earth.trackingState != TrackingState.TRACKING) {
            return
        }
        earthAnchor?.detach()

        val cameraGeospatialPose = earth.cameraGeospatialPose
        val altitude = cameraGeospatialPose.altitude + 3
        val latitude = cameraGeospatialPose.latitude
        val longitude = cameraGeospatialPose.longitude

        with(activity.view.sharedPreferences.edit()) {
            putFloat("altitude", altitude.toFloat())
            putFloat("latitude", latitude.toFloat())
            putFloat("longitude", longitude.toFloat())
            apply()
        }

        val qx = 0f
        val qy = 0f
        val qz = 0f
        val qw = 1f

        val latLng = LatLng(latitude, longitude)

        earthAnchor = earth.createAnchor(latitude, longitude, altitude, qx, qy, qz, qw)

        activity.view.mapView?.carMarker?.apply {
            position = latLng
            isVisible = true
        }
    }

    private fun SampleRender.renderCompassAtAnchor(anchor: Anchor) {
        anchor.pose.toMatrix(modelMatrix, 0)

        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)

        virtualObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix)
        draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer)
    }

    private fun showError(errorMessage: String) =
        activity.view.snackbarHelper.showError(activity, errorMessage)
}
