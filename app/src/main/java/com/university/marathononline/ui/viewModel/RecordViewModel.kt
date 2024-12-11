package com.university.marathononline.ui.viewModel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Race
import com.university.marathononline.data.models.User
import com.university.marathononline.data.repository.RegistrationRepository
import com.university.marathononline.data.repository.RaceRepository
import com.university.marathononline.data.request.CreateRaceRequest
import com.university.marathononline.data.response.RegistrationsResponse
import com.university.marathononline.utils.KalmanFilter
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.formatSpeed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class RecordViewModel(
    private val registrationRepository: RegistrationRepository,
    private val raceRepository: RaceRepository
) : BaseViewModel(listOf(registrationRepository, raceRepository)) {

    private val _isGPSEnabled = MutableLiveData<Boolean>()
    val isGPSEnabled: LiveData<Boolean> get() = _isGPSEnabled

    private val _createRaceResponse: MutableLiveData<Resource<Race>> = MutableLiveData()
    val createRaceResponse: LiveData<Resource<Race>> get() = _createRaceResponse

    private val _saveRaceIntoRegistration: MutableLiveData<Resource<RegistrationsResponse>> = MutableLiveData()
    val saveRaceIntoRegistration: LiveData<Resource<RegistrationsResponse>> get() = _saveRaceIntoRegistration

    private val _time = MutableStateFlow("0:00:00")
    val time = _time.asStateFlow()

    private val _speed = MutableStateFlow("0 km/h")
    val speed = _speed.asStateFlow()

    private val _position = MutableStateFlow("Not checking location")
    val position = _position.asStateFlow()

    private val _distance = MutableStateFlow("0 km")
    val distance = _distance.asStateFlow()

    private val _steps = MutableStateFlow(0)
    val steps = _steps.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private var startTime: Long = 0
    private var currentLocation: Location? = null
    private var totalDistance: Double = 0.0
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private val kalmanLatitude = KalmanFilter(q = 0.001, r = 1.0)
    private val kalmanLongitude = KalmanFilter(q = 0.001, r = 1.0)

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    fun initializeLocationTracking(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { updateUI(it) }
            }
        }

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        stepSensor?.let {
            sensorManager.registerListener(stepListener, it, SensorManager.SENSOR_DELAY_UI)
        }


        setupGPSStatusObserver(context)
    }

    private fun setupGPSStatusObserver(context: Context) {
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

    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null && event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                _steps.value += 1
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    fun startRecording(context: Context) {
        currentLocation = null
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        startTime = SystemClock.elapsedRealtime()
        _isRecording.value = true
        _steps.value = 0

        viewModelScope.launch {
            while (_isRecording.value) {
                updateTime()
                delay(1000)
            }
        }
    }

    fun stopRecording() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _isRecording.value = false

        val timeTakenInSeconds = (SystemClock.elapsedRealtime() - startTime) / 1000
        val avgSpeed = if (totalDistance > 0) totalDistance / (timeTakenInSeconds / 3600f) else 0.0

        val createRaceRequest = CreateRaceRequest(
            steps = _steps.value,
            distance = totalDistance,
            timeTaken = timeTakenInSeconds,
            avgSpeed = avgSpeed,
            timestamp = LocalDateTime.now().toString()
        )

        // Log the data
        println("Recording stopped:")
        println("Time: $timeTakenInSeconds seconds")
        println("Distance: ${formatDistance(totalDistance)}")
        println("Speed: $avgSpeed km/h")
        println("Steps: ${_steps.value}")

        createRace(createRaceRequest)
    }

    @SuppressLint("DefaultLocale")
    private fun updateUI(location: Location) {
        val filteredLatitude = kalmanLatitude.processMeasurement(location.latitude)
        val filteredLongitude = kalmanLongitude.processMeasurement(location.longitude)

        _position.value = "Position: $filteredLatitude, $filteredLongitude"

        val speedInKmH = location.speed * 3.6
        val distance = currentLocation?.distanceTo(location) ?: 0f

        if (currentLocation == null) {
            currentLocation = location
        }

        if (distance >= 1) {
            totalDistance += distance.div(1000)
            _speed.value = formatSpeed(speedInKmH)
            _distance.value = formatDistance(totalDistance)
            currentLocation = location
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateTime() {
        val elapsedTime = (SystemClock.elapsedRealtime() - startTime) / 1000
        val hours = (elapsedTime / 3600).toInt()
        val minutes = ((elapsedTime % 3600) / 60).toInt()
        val seconds = (elapsedTime % 60).toInt()
        _time.value = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun createRace(request: CreateRaceRequest){
        viewModelScope.launch {
            _createRaceResponse.value = Resource.Loading
            _createRaceResponse.value = raceRepository.addRaceAndSaveIntoRegistration(request)
        }
    }

    fun saveRaceIntoRegistration(race: Race) {
        viewModelScope.launch {
            _saveRaceIntoRegistration.value = Resource.Loading
            _saveRaceIntoRegistration.value = registrationRepository.saveRaceIntoRegistration(race)
        }
    }
}