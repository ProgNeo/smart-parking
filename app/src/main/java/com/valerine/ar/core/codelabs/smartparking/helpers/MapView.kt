package com.valerine.ar.core.codelabs.smartparking.helpers

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.graphics.Paint
import android.util.Log
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
import com.valerine.ar.core.database.models.ParkingPlace

class MapView(val activity: SmartParkingActivity, private val googleMap: GoogleMap) {
    private var setInitialCameraPosition = false
    private var cameraIdle = true

    private val userMarker = createUserMarker(USER_MARKER_COLOR)
    private var bookedParkingPlace: Marker? = null
    val carMarker = createCarMarker(CAR_MARKER_COLOR)

    val parkingPlaceMarkers = arrayListOf<Marker>()

    var isTrackUser = true

    init {
        googleMap.uiSettings.apply {
            isMapToolbarEnabled = false
            isIndoorLevelPickerEnabled = false
            isZoomControlsEnabled = false
            isTiltGesturesEnabled = false
            isScrollGesturesEnabled = false
        }

        googleMap.setOnMarkerClickListener { marker ->
            if (marker.title == activity.getString(R.string.parking_place)) {
                val parkingPlacesList = activity.databaseHelper.getParkingPlacesList()

                bookedParkingPlace?.let { parkingPlace ->
                    parkingPlace.setIcon(
                        BitmapDescriptorFactory.fromBitmap(
                            createColoredMarkerBitmap(
                                PARKING_FREE_COLOR, R.drawable.ic_parking_place
                            )
                        )
                    )
                    activity.databaseHelper.updateParkingPlace(parkingPlacesList.first {
                        it.id == parkingPlace.tag
                    }.apply { this.isBooked = false })

                    if (bookedParkingPlace?.tag == marker.tag) return@setOnMarkerClickListener false
                }

                bookedParkingPlace = marker

                marker.setIcon(
                    BitmapDescriptorFactory.fromBitmap(
                        createColoredMarkerBitmap(
                            PARKING_BOOKED_COLOR, R.drawable.ic_parking_place
                        )
                    )
                )
                val place = parkingPlacesList.first {
                    it.id == marker.tag
                }.apply { this.isBooked = true }

                activity.databaseHelper.updateParkingPlace(place)
                activity.renderer.placeParkingAnchor(place)
            }

            return@setOnMarkerClickListener false
        }
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
                CameraPosition.Builder().zoom(googleMap.cameraPosition.zoom).target(position)
            }
            if (isTrackUser) {
                googleMap.moveCamera(
                    CameraUpdateFactory.newCameraPosition(cameraPositionBuilder.build())
                )
            }
        }
    }

    private fun createUserMarker(
        color: Int,
    ): Marker {
        val markersOptions =
            MarkerOptions().position(LatLng(0.0, 0.0)).draggable(false).anchor(0.5f, 0.5f)
                .flat(true).visible(false).icon(
                    BitmapDescriptorFactory.fromBitmap(
                        createColoredMarkerBitmap(
                            color, R.drawable.ic_navigation_white
                        )
                    )
                )
        return googleMap.addMarker(markersOptions)!!
    }

    private fun createCarMarker(
        color: Int,
    ): Marker {
        val markersOptions =
            MarkerOptions().position(LatLng(0.0, 0.0)).draggable(false).anchor(0.5f, 0.5f)
                .flat(false).visible(false).icon(
                    BitmapDescriptorFactory.fromBitmap(
                        createColoredMarkerBitmap(
                            color, R.drawable.ic_directions_car
                        )
                    )
                )
        return googleMap.addMarker(markersOptions)!!
    }

    fun createParkingMarker(
        place: ParkingPlace
    ): Marker {
        val markersOptions =
            MarkerOptions().position(LatLng(place.latitude, place.longitude))
                .draggable(false).anchor(0.5f, 0.5f).flat(false).visible(!place.isEmployed)
                .title(activity.getString(R.string.parking_place)).icon(
                    BitmapDescriptorFactory.fromBitmap(
                        createColoredMarkerBitmap(
                            if (place.isBooked) PARKING_BOOKED_COLOR else PARKING_FREE_COLOR,
                            R.drawable.ic_parking_place
                        )
                    )
                )
        val marker = googleMap.addMarker(markersOptions)!!
        if (place.isBooked) {
            bookedParkingPlace = marker
            activity.renderer.placeParkingAnchor(place)
        }
        marker.tag = place.id
        return marker
    }

    private fun createColoredMarkerBitmap(
        @ColorInt color: Int, @DrawableRes drawableRes: Int
    ): Bitmap {
        val opt = BitmapFactory.Options()
        opt.inMutable = true
        val navigationIcon = BitmapFactory.decodeResource(
            activity.resources, drawableRes, opt
        )
        val p = Paint()
        p.colorFilter = LightingColorFilter(color,  /* add= */1)
        val canvas = Canvas(navigationIcon)
        canvas.drawBitmap(navigationIcon,  /* left= */0f,  /* top= */0f, p)
        return navigationIcon
    }

    companion object {
        private val USER_MARKER_COLOR: Int = Color.argb(255, 97, 189, 16)
        private val CAR_MARKER_COLOR: Int = Color.argb(255, 125, 125, 125)
        private val PARKING_FREE_COLOR: Int = Color.argb(255, 45, 92, 163)
        private val PARKING_BOOKED_COLOR: Int = Color.argb(255, 196, 43, 28)
    }
}