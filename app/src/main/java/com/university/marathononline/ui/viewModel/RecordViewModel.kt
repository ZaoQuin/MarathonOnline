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
import com.google.android.gms.maps.model.LatLng
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.Record
import com.university.marathononline.data.repository.RegistrationRepository
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.data.request.CreateRecordRequest
import com.university.marathononline.data.response.RegistrationsResponse
import com.university.marathononline.utils.KalmanFilter
import com.university.marathononline.utils.formatDistance
import com.university.marathononline.utils.formatSpeed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class RecordViewModel(
    private val registrationRepository: RegistrationRepository,
    private val recordRepository: RecordRepository
) : BaseViewModel(listOf(registrationRepository, recordRepository)) {
    private val _isGPSEnabled = MutableLiveData<Boolean>()
    val isGPSEnabled: LiveData<Boolean> get() = _isGPSEnabled

    private val _createRecordResponse: MutableLiveData<Resource<Record>> = MutableLiveData()
    val createRecordResponse: LiveData<Resource<Record>> get() = _createRecordResponse

    private val _saveRecordIntoRegistration: MutableLiveData<Resource<RegistrationsResponse>> = MutableLiveData()
    val saveRecordIntoRegistration: LiveData<Resource<RegistrationsResponse>> get() = _saveRecordIntoRegistration

    private val _time = MutableStateFlow("0:00:00")
    val time = _time.asStateFlow()

    private val _speed = MutableStateFlow("0 km/h")
    val speed = _speed.asStateFlow()

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

    private val _position = MutableStateFlow("")
    val position: StateFlow<String> get() = _position

    private val _routes = MutableStateFlow<List<LatLng>>(emptyList())
    val routes: StateFlow<List<LatLng>> get() = _routes

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


        viewModelScope.launch(Dispatchers.IO) {
            setupGPSStatusObserver(context)
        }
    }

    private fun setupGPSStatusObserver(context: Context) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val isGPSAvailable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        _isGPSEnabled.postValue(isGPSAvailable)

        val handler = Handler(Looper.getMainLooper())
        val checkGPSRunnable = object : Runnable {
            override fun run() {
                val currentGPSStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                if (currentGPSStatus != _isGPSEnabled.value) {
                    _isGPSEnabled.postValue(currentGPSStatus)
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

        val createRecordRequest = CreateRecordRequest(
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

        createRecord(createRecordRequest)
    }

    @SuppressLint("DefaultLocale")
    private fun updateUI(location: Location) {
        viewModelScope.launch {
            val filteredLatitude = kalmanLatitude.processMeasurement(location.latitude)
            val filteredLongitude = kalmanLongitude.processMeasurement(location.longitude)

            val newLocation = "$filteredLatitude,$filteredLongitude"
            _position.emit(newLocation)

            val newPoint = LatLng(filteredLatitude, filteredLongitude)
            val newRoutes = _routes.value + newPoint
            _routes.emit(newRoutes)

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
    }

    @SuppressLint("DefaultLocale")
    private fun updateTime() {
        val elapsedTime = (SystemClock.elapsedRealtime() - startTime) / 1000
        val hours = (elapsedTime / 3600).toInt()
        val minutes = ((elapsedTime % 3600) / 60).toInt()
        val seconds = (elapsedTime % 60).toInt()
        _time.value = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun createRecord(request: CreateRecordRequest){
        viewModelScope.launch {
            _createRecordResponse.value = Resource.Loading
            _createRecordResponse.value = recordRepository.addRecordAndSaveIntoRegistration(request)
        }
    }

    fun saveRecordIntoRegistration(record: Record) {
        viewModelScope.launch {
            _saveRecordIntoRegistration.value = Resource.Loading
            _saveRecordIntoRegistration.value = registrationRepository.saveRecordIntoRegistration(record)
        }
    }
}