package com.valerine.ar.core.codelabs.smartparking

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.valerine.ar.core.codelabs.smartparking.helpers.ARCoreSessionLifecycleHelper
import com.valerine.ar.core.codelabs.smartparking.helpers.GeoPermissionsHelper
import com.valerine.ar.core.codelabs.smartparking.helpers.SmartParkingView
import com.valerine.ar.core.database.DatabaseHelper
import com.valerine.ar.core.database.models.ParkingPlace
import com.valerine.ar.core.examples.java.common.samplerender.SampleRender
import java.util.Locale

class SmartParkingActivity : AppCompatActivity(), LocationListener {
    companion object {
        private const val TAG = "SmartParkingActivity"
    }

    private lateinit var locationManager: LocationManager
    lateinit var arCoreSessionHelper: ARCoreSessionLifecycleHelper
    lateinit var view: SmartParkingView
    lateinit var renderer: SmartParkingRenderer
    lateinit var databaseHelper: DatabaseHelper

    var userLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        databaseHelper = DatabaseHelper(applicationContext)

        if (databaseHelper.getParkingPlacesList().isEmpty()) {
            ParkingPlace.mock().forEach {
                databaseHelper.insertParkingPlace(it)
            }
        }

        arCoreSessionHelper = ARCoreSessionLifecycleHelper(this)

        arCoreSessionHelper.exceptionCallback = { exception ->
            val message = when (exception) {
                is UnavailableUserDeclinedInstallationException -> "Please install Google Play Services for AR"
                is UnavailableApkTooOldException -> "Please update ARCore"
                is UnavailableSdkTooOldException -> "Please update this app"
                is UnavailableDeviceNotCompatibleException -> "This device does not support AR"
                is CameraNotAvailableException -> "Camera not available. Try restarting the app."
                else -> "Failed to create AR session: $exception"
            }
            Log.e(TAG, "ARCore threw an exception", exception)
            view.snackbarHelper.showError(this, message)
        }

        arCoreSessionHelper.beforeSessionResume = ::configureSession

        lifecycle.addObserver(arCoreSessionHelper)

        renderer = SmartParkingRenderer(this)
        lifecycle.addObserver(renderer)

        view = SmartParkingView(this)
        lifecycle.addObserver(view)
        setContentView(view.root)

        SampleRender(view.surfaceView, renderer, assets)
    }

    private fun configureSession(session: Session) {
        session.configure(
            session.config.apply {
                //focusMode = Config.FocusMode.AUTO
                geospatialMode = Config.GeospatialMode.ENABLED
            },
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        if (!GeoPermissionsHelper.hasGeoPermissions(this)) {
            Toast.makeText(
                this,
                "Camera and location permissions are needed to run this application",
                Toast.LENGTH_LONG
            ).show()

            if (!GeoPermissionsHelper.shouldShowRequestPermissionRationale(this)) {
                GeoPermissionsHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    fun getLocation() {
        runOnUiThread {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if ((ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    2
                )
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)
        }
    }

    override fun onLocationChanged(location: Location) {
        userLocation = location
    }
}