/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.valerine.ar.core.codelabs.smartparking.helpers

import android.opengl.GLSurfaceView
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Earth
import com.google.ar.core.GeospatialPose
import com.valerine.ar.core.codelabs.smartparking.SmartParkingActivity
import com.valerine.ar.core.codelabs.smartparking.R
import com.valerine.ar.core.examples.java.common.helpers.SnackbarHelper

class SmartParkingView(val activity: SmartParkingActivity) : DefaultLifecycleObserver {
    val root: View = View.inflate(activity, R.layout.activity_main, null)
    val surfaceView: GLSurfaceView = root.findViewById(R.id.surfaceview)

    val session
        get() = activity.arCoreSessionHelper.session

    val snackbarHelper = SnackbarHelper()

    var mapView: MapView? = null

    private val bottomSheet = root.findViewById<View>(R.id.bottom_sheet)

    val mapFragment =
        (activity.supportFragmentManager.findFragmentById(R.id.map)!! as SupportMapFragment).also {
            it.getMapAsync { googleMap -> mapView = MapView(activity, googleMap) }
        }

    val statusText = root.findViewById<TextView>(R.id.statusText)

    val markerFab = root.findViewById<FloatingActionButton>(R.id.fab_place_marker).apply {
        this.setOnClickListener {
            activity.renderer.placeMark()
        }
    }

    val mapFab = root.findViewById<FloatingActionButton>(R.id.fab_open_map).apply {
        this.setOnClickListener {
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED;
        }
    }

    fun updateStatusText(earth: Earth, cameraGeospatialPose: GeospatialPose?) {
        activity.runOnUiThread {
            val poseText = if (cameraGeospatialPose == null) "" else
                activity.getString(
                    R.string.geospatial_pose,
                    cameraGeospatialPose.latitude,
                    cameraGeospatialPose.longitude,
                    cameraGeospatialPose.horizontalAccuracy,
                    cameraGeospatialPose.altitude,
                    cameraGeospatialPose.verticalAccuracy,
                    cameraGeospatialPose.heading,
                    cameraGeospatialPose.headingAccuracy
                )
            statusText.text = activity.resources.getString(
                R.string.earth_state,
                earth.earthState.toString(),
                earth.trackingState.toString(),
                poseText
            )
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        surfaceView.onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        surfaceView.onPause()
    }
}
