package com.university.marathononline.ui.viewModel.tracking

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.university.marathononline.utils.KalmanFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LocationTracker(private val context: Context) {

    private val kalmanLatitude = KalmanFilter(q = 0.001, r = 1.0)
    private val kalmanLongitude = KalmanFilter(q = 0.001, r = 1.0)
    private val speedFilter = KalmanFilter(q = 0.001, r = 1.0)

    private val _isGPSEnabled = MutableStateFlow(false)
    val isGPSEnabled: StateFlow<Boolean> = _isGPSEnabled

    private val _position = MutableStateFlow("")
    val position: StateFlow<String> = _position

    private val _routes = MutableStateFlow<List<LatLng>>(emptyList())
    val routes: StateFlow<List<LatLng>> = _routes

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    var currentLocation: Location? = null
        private set

    var onLocationUpdate: ((Location, Double) -> Unit)? = null

    init {
        initializeLocationTracking()
        setupGPSStatusObserver()
    }

    private fun initializeLocationTracking() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    processLocationUpdate(location)
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun processLocationUpdate(location: Location) {
        val filteredLatitude = kalmanLatitude.processMeasurement(location.latitude)
        val filteredLongitude = kalmanLongitude.processMeasurement(location.longitude)

        val newLocation = "$filteredLatitude,$filteredLongitude"
        _position.value = newLocation

        val newPoint = LatLng(filteredLatitude, filteredLongitude)
        val newRoutes = _routes.value + newPoint
        _routes.value = newRoutes

        val filteredSpeed = speedFilter.processMeasurement(location.speed.toDouble()).toFloat()
        val distance = currentLocation?.distanceTo(location) ?: 0f

        if (currentLocation == null) {
            currentLocation = location
        }

        if (distance >= 1) {
            val distanceInKm = distance.div(1000)
            currentLocation = location

            // Notify callback about location update
            onLocationUpdate?.invoke(location, distanceInKm.toDouble())
        }
    }

    private fun setupGPSStatusObserver() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val isGPSAvailable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        _isGPSEnabled.value = isGPSAvailable

        val handler = Handler(Looper.getMainLooper())
        val checkGPSRunnable = object : Runnable {
            override fun run() {
                val currentGPSStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                if (currentGPSStatus != _isGPSEnabled.value) {
                    _isGPSEnabled.value = currentGPSStatus
                }
                handler.postDelayed(this, 3000)
            }
        }
        handler.post(checkGPSRunnable)
    }

    fun startLocationUpdates(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        return true
    }

    fun stopLocationUpdates() {
        if (::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    fun reset() {
        _position.value = ""
        _routes.value = emptyList()
        currentLocation = null
    }
}