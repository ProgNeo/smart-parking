package com.valerine.ar.core.codelabs.smartparking.helpers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.valerine.ar.core.codelabs.smartparking.SmartParkingActivity
import com.valerine.ar.core.codelabs.smartparking.R

class MapView(val activity: SmartParkingActivity, private val googleMap: GoogleMap) {
    private var setInitialCameraPosition = false
    private val userMarker = createUserMarker(USER_MARKER_COLOR)
    private var cameraIdle = true

    val carMarker = createCarMarker(CAR_MARKER_COLOR)

    init {
        googleMap.uiSettings.apply {
            isMapToolbarEnabled = false
            isIndoorLevelPickerEnabled = false
            isZoomControlsEnabled = false
            isTiltGesturesEnabled = false
            isScrollGesturesEnabled = false
        }

        googleMap.setOnMarkerClickListener { false }

        googleMap.setOnCameraMoveListener { cameraIdle = false }
        googleMap.setOnCameraIdleListener { cameraIdle = true }
    }

    fun updateMapPosition(latitude: Double, longitude: Double, heading: Double) {
        val position = LatLng(latitude, longitude)
        activity.runOnUiThread {
            if (!cameraIdle) {
                return@runOnUiThread
            }
            userMarker.isVisible = true
            userMarker.position = position
            userMarker.rotation = heading.toFloat()

            val cameraPositionBuilder: CameraPosition.Builder = if (!setInitialCameraPosition) {
                setInitialCameraPosition = true
                CameraPosition.Builder().zoom(21f).target(position)
            } else {
                CameraPosition.Builder()
                    .zoom(googleMap.cameraPosition.zoom)
                    .target(position)
            }
            googleMap.moveCamera(
                CameraUpdateFactory.newCameraPosition(cameraPositionBuilder.build())
            )
        }
    }

    private fun createUserMarker(
        color: Int,
    ): Marker {
        val markersOptions = MarkerOptions()
            .position(LatLng(0.0, 0.0))
            .draggable(false)
            .anchor(0.5f, 0.5f)
            .flat(true)
            .visible(false)
            .icon(BitmapDescriptorFactory.fromBitmap(createColoredMarkerBitmap(color, R.drawable.ic_navigation_white)))
        return googleMap.addMarker(markersOptions)!!
    }

    private fun createCarMarker(
        color: Int,
    ): Marker {
        val markersOptions = MarkerOptions()
            .position(LatLng(0.0, 0.0))
            .draggable(false)
            .anchor(0.5f, 0.5f)
            .flat(true)
            .visible(false)
            .icon(BitmapDescriptorFactory.fromBitmap(createColoredMarkerBitmap(color, R.drawable.ic_directions_car)))
        return googleMap.addMarker(markersOptions)!!
    }

    private fun createColoredMarkerBitmap(@ColorInt color: Int, @DrawableRes drawableRes: Int): Bitmap {
        val opt = BitmapFactory.Options()
        opt.inMutable = true
        val navigationIcon =
            BitmapFactory.decodeResource(
                activity.resources,
                drawableRes,
                opt
            )
        val p = Paint()
        p.colorFilter = LightingColorFilter(color,  /* add= */1)
        val canvas = Canvas(navigationIcon)
        canvas.drawBitmap(navigationIcon,  /* left= */0f,  /* top= */0f, p)
        return navigationIcon
    }

    companion object {
        private val USER_MARKER_COLOR: Int = Color.argb(255, 0, 255, 0)
        private val CAR_MARKER_COLOR: Int = Color.argb(255, 125, 125, 125)
    }
}