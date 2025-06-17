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

    private var isStationary = false
    private val stationaryThreshold = 0.3f
    private val maxRunningSpeed = 5.56f

    init {
        initializeLocationTracking()
        setupGPSStatusObserver()
    }

    private fun initializeLocationTracking() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateIntervalMillis(1500)
            .setMinUpdateDistanceMeters(5f)
            .setMaxUpdateDelayMillis(6000)
            .build()

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
        if (location.accuracy > 15f || location.speed > maxRunningSpeed) return

        val distance = currentLocation?.let { it.distanceTo(location) } ?: 0f
        val timeDelta = currentLocation?.let { (location.time - it.time) / 1000f } ?: 0f
        if (timeDelta > 0 && distance / timeDelta > maxRunningSpeed) return

        if (location.speed < stationaryThreshold) {
            if (!isStationary) {
                isStationary = true
                adjustLocationUpdateInterval(10000)
            }
        } else {
            if (isStationary) {
                isStationary = false
                adjustLocationUpdateInterval(3000)
            }
        }

        val accuracyFactor = location.accuracy.coerceIn(1f, 15f)
        kalmanLatitude.r = accuracyFactor * 0.05
        kalmanLongitude.r = accuracyFactor * 0.05
        speedFilter.r = accuracyFactor * 0.05

        val filteredLatitude = kalmanLatitude.processMeasurement(location.latitude)
        val filteredLongitude = kalmanLongitude.processMeasurement(location.longitude)

        val newLocation = "$filteredLatitude,$filteredLongitude"
        _position.value = newLocation

        val newPoint = LatLng(filteredLatitude, filteredLongitude)
        val newRoutes = _routes.value + newPoint
        _routes.value = newRoutes

        val filteredSpeed = speedFilter.processMeasurement(location.speed.toDouble()).toFloat()
        if (currentLocation == null || distance >= 1f) {
            currentLocation = location
            val distanceInKm = distance / 1000
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

    private fun adjustLocationUpdateInterval(interval: Long) {
        stopLocationUpdates()
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
            .setMinUpdateIntervalMillis(interval / 2)
            .setMinUpdateDistanceMeters(1f)
            .setMaxUpdateDelayMillis(interval * 2)
            .build()
        startLocationUpdates()
    }

    fun startLocationUpdates(): Boolean {
        val fineLocationGranted = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted && !coarseLocationGranted) {
            return false
        }

        val priority = if (fineLocationGranted) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        locationRequest = LocationRequest.Builder(priority, 3000)
            .setMinUpdateIntervalMillis(1000)
            .setMinUpdateDistanceMeters(1f)
            .setMaxUpdateDelayMillis(6000)
            .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        return true
    }

    fun getLastKnownLocation(callback: (LatLng?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val filteredLatitude = kalmanLatitude.processMeasurement(it.latitude)
                    val filteredLongitude = kalmanLongitude.processMeasurement(it.longitude)
                    callback(LatLng(filteredLatitude, filteredLongitude))
                } ?: callback(null)
            }
        } else {
            callback(null)
        }
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
        isStationary = false
    }
}