package com.valerine.ar.core.codelabs.smartparking.helpers

import android.content.Context
import android.content.SharedPreferences
import android.opengl.GLSurfaceView
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.valerine.ar.core.codelabs.smartparking.R
import com.valerine.ar.core.codelabs.smartparking.SmartParkingActivity
import com.valerine.ar.core.examples.java.common.helpers.SnackbarHelper

class SmartParkingView(val activity: SmartParkingActivity) : DefaultLifecycleObserver {
    val root: View = View.inflate(activity, R.layout.activity_main, null)
    val surfaceView: GLSurfaceView = root.findViewById(R.id.surfaceview)

    val sharedPreferences: SharedPreferences = activity.getPreferences(Context.MODE_PRIVATE)

    val session
        get() = activity.arCoreSessionHelper.session

    val snackbarHelper = SnackbarHelper()

    var mapView: MapView? = null

    private val bottomSheet = root.findViewById<View>(R.id.bottom_sheet)

    val mapFragment =
        (activity.supportFragmentManager.findFragmentById(R.id.map)!! as SupportMapFragment).also {
            it.getMapAsync { googleMap -> mapView = MapView(activity, googleMap) }
        }

    val markerFab: FloatingActionButton =
        root.findViewById<FloatingActionButton>(R.id.fab_place_marker).apply {
            this.setOnClickListener {
                activity.renderer.placeMark()
            }
        }

    val mapFab: FloatingActionButton =
        root.findViewById<FloatingActionButton>(R.id.fab_open_map).apply {
            this.setOnClickListener {
                val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                } else {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }

    val trackUserFab: FloatingActionButton =
        root.findViewById<FloatingActionButton>(R.id.fab_track_user).apply {
            this.setOnClickListener {
                mapView?.let {
                    it.isTrackUser = !it.isTrackUser
                    if (it.isTrackUser) {
                        this.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity.applicationContext,
                                R.drawable.ic_my_location,
                            ),
                        )
                    } else {
                        this.setImageDrawable(
                            ContextCompat.getDrawable(
                                activity.applicationContext,
                                R.drawable.ic_location_disabled,
                            ),
                        )
                    }
                }
            }
        }

    override fun onResume(owner: LifecycleOwner) {
        surfaceView.onResume()
    }

    override fun onPause(owner: LifecycleOwner) {
        surfaceView.onPause()
    }
}
