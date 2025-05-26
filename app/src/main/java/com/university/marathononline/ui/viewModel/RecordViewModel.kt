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
import com.google.android.gms.maps.model.LatLng
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.GuidedModeStats
import com.university.marathononline.data.models.PaceStatus
import com.university.marathononline.data.models.Record
import com.university.marathononline.data.models.TrainingDay
import com.university.marathononline.data.repository.RegistrationRepository
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.data.repository.TrainingDayRepository
import com.university.marathononline.data.request.CreateRecordRequest
import com.university.marathononline.data.response.RegistrationsResponse
import com.university.marathononline.utils.KalmanFilter
import com.university.marathononline.utils.TrainingSessionManager
import com.university.marathononline.utils.VoiceGuidanceService
import com.university.marathononline.utils.formatDistance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class RecordViewModel(
    private val registrationRepository: RegistrationRepository,
    private val recordRepository: RecordRepository,
    private val trainingDayRepository: TrainingDayRepository
) : BaseViewModel(listOf(registrationRepository, recordRepository, trainingDayRepository)) {

    private val _trainingDay = MutableLiveData<TrainingDay>()
    val trainingDay: LiveData<TrainingDay> get() = _trainingDay

    private val _isGPSEnabled = MutableLiveData<Boolean>()
    val isGPSEnabled: LiveData<Boolean> get() = _isGPSEnabled

    private val _createRecordResponse: MutableLiveData<Resource<Record>> = MutableLiveData()
    val createRecordResponse: LiveData<Resource<Record>> get() = _createRecordResponse

    private val _saveRecordIntoRegistration: MutableLiveData<Resource<RegistrationsResponse>> = MutableLiveData()
    val saveRecordIntoRegistration: LiveData<Resource<RegistrationsResponse>> get() = _saveRecordIntoRegistration

    private val _saveRecordIntoTrainingDay: MutableLiveData<Resource<TrainingDay>> = MutableLiveData()
    val saveRecordIntoTrainingDay: LiveData<Resource<TrainingDay>> get() = _saveRecordIntoTrainingDay

    private val _getCurrentTrainingDay: MutableLiveData<Resource<TrainingDay>> = MutableLiveData()
    val getCurrentTrainingDay: LiveData<Resource<TrainingDay>> get() = _getCurrentTrainingDay

    private val _time = MutableStateFlow("0:00:00")
    val time = _time.asStateFlow()

    private val _averagePace = MutableStateFlow("-- min/km")
    val averagePace = _averagePace.asStateFlow()

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
    private var isGuidedMode: Boolean = false
    private var trainingSessionManager: TrainingSessionManager? = null

    // Store application context to avoid context leaks
    private var applicationContext: Context? = null

    private val kalmanLatitude = KalmanFilter(q = 0.001, r = 1.0)
    private val kalmanLongitude = KalmanFilter(q = 0.001, r = 1.0)
    private val speedFilter = KalmanFilter(q = 0.001, r = 1.0)

    private val _position = MutableStateFlow("")
    val position: StateFlow<String> get() = _position

    private val _routes = MutableStateFlow<List<LatLng>>(emptyList())
    val routes: StateFlow<List<LatLng>> get() = _routes

    private val _guidedModeStats = MutableStateFlow(GuidedModeStats())
    val guidedModeStats = _guidedModeStats.asStateFlow()

    // Track the current pace for real-time updates
    private var currentAvgPace: Double = 0.0

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    fun initializeLocationTracking(context: Context) {
        // Store application context to use it later
        applicationContext = context.applicationContext

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

    fun setGuidedMode(enabled: Boolean, context: Context? = null) {
        isGuidedMode = enabled

        // Always clean up existing manager if present to avoid resource leaks
        trainingSessionManager?.clear()
        trainingSessionManager = null

        // Only initialize if enabling guided mode and context is provided
        if (enabled) {
            // Use provided context, or fall back to stored applicationContext
            val contextToUse = context?.applicationContext ?: applicationContext

            if (contextToUse != null) {
                if (_trainingDay.value != null) {
                    Log.d("RecordViewModel", "Initializing guided mode with existing training day")
                    // Create new voice service each time to ensure clean initialization
                    val voiceService = VoiceGuidanceService(contextToUse)
                    trainingSessionManager = TrainingSessionManager(_trainingDay.value!!.session, voiceService)
                } else {
                    Log.d("RecordViewModel", "Fetching current training day for guided mode")
                    // Fetch training day first - will initialize manager when data arrives
                    getCurrentTrainingDay()
                }
            } else {
                Log.e("RecordViewModel", "Cannot initialize guided mode: no context available")
            }
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
        // Store context for future use
        applicationContext = context.applicationContext

        // Reset all data to initial state
        resetAllData()

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

        viewModelScope.launch {
            while (_isRecording.value) {
                updateTime()
                delay(1000)
            }
        }
    }

    // New function to reset all data to initial state
    private fun resetAllData() {
        _time.value = "0:00:00"
        _averagePace.value = "-- min/km"
        _distance.value = "0 km"
        _steps.value = 0
        _routes.value = emptyList()
        _position.value = ""
        _guidedModeStats.value = GuidedModeStats() // Reset guided mode stats
        totalDistance = 0.0
        currentLocation = null
        currentAvgPace = 0.0
    }

    fun stopRecording() {
        if (!_isRecording.value) return

        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(stepListener)
        trainingSessionManager?.clear()
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

            val filteredSpeed = speedFilter.processMeasurement(location.speed.toDouble()).toFloat()
            val speedInKmH = filteredSpeed * 3.6
            val distance = currentLocation?.distanceTo(location) ?: 0f

            if (currentLocation == null) {
                currentLocation = location
            }

            if (distance >= 1) {
                totalDistance += distance.div(1000)
                _distance.value = formatDistance(totalDistance)
                currentLocation = location

                // Calculate current instantaneous pace (min/km)
                val instantPace = if (speedInKmH > 0) 60.0 / speedInKmH else 0.0

                // Calculate overall average pace
                currentAvgPace = calculateAveragePace()

                // Update the UI with the average pace
                val avgPaceMinutes = currentAvgPace.toInt()
                val avgPaceSeconds = ((currentAvgPace - avgPaceMinutes) * 60).toInt()
                _averagePace.value = String.format("%d:%02d min/km", avgPaceMinutes, avgPaceSeconds)

                // Update guided mode statistics
                updateGuidedModeStats()

                // Pass the AVERAGE pace to the training session manager
                if (isGuidedMode && trainingSessionManager != null) {
                    try {
                        Log.d("RecordViewModel", "Updating training session with pace: $currentAvgPace min/km")
                        trainingSessionManager?.update(
                            currentAvgPace,  // Using the average pace instead of instantaneous
                            totalDistance,
                            SystemClock.elapsedRealtime() - startTime
                        )
                    } catch (e: Exception) {
                        Log.e("RecordViewModel", "Error updating training session", e)
                    }
                }
            }
        }
    }

    private fun calculateAveragePace(): Double {
        val timeTakenInSeconds = (SystemClock.elapsedRealtime() - startTime) / 1000

        val elapsedTimeInHours = timeTakenInSeconds / 3600.0

        val averageSpeedKmH = if (totalDistance > 0 && elapsedTimeInHours > 0) {
            totalDistance / elapsedTimeInHours
        } else {
            0.0
        }

        return if (averageSpeedKmH > 0) {
            60.0 / averageSpeedKmH
        } else {
            0.0
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateTime() {
        val elapsedTime = (SystemClock.elapsedRealtime() - startTime) / 1000
        val hours = (elapsedTime / 3600).toInt()
        val minutes = ((elapsedTime % 3600) / 60).toInt()
        val seconds = (elapsedTime % 60).toInt()
        _time.value = String.format("%d:%02d:%02d", hours, minutes, seconds)

        // Update average pace if distance has changed
        if (totalDistance > 0) {
            // Calculate average pace using actual elapsed time and distance
            currentAvgPace = calculateAveragePace()
            val avgPaceMinutes = currentAvgPace.toInt()
            val avgPaceSeconds = ((currentAvgPace - avgPaceMinutes) * 60).toInt()
            _averagePace.value = String.format("%d:%02d min/km", avgPaceMinutes, avgPaceSeconds)

            // Update guided mode statistics
            updateGuidedModeStats()
        }
    }

    private fun createRecord(request: CreateRecordRequest) {
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

    fun saveRecordIntoTrainingDay(record: Record) {
        viewModelScope.launch {
            _saveRecordIntoTrainingDay.value = Resource.Loading
            _saveRecordIntoTrainingDay.value = trainingDayRepository.saveRecordIntoTrainingDay(record)
        }
    }

    fun setCurrentTrainingDay(trainingDay: TrainingDay) {
        _trainingDay.value = trainingDay
        Log.d("RecordViewModel", "Training day set: ${trainingDay.session.type} - pace: ${trainingDay.session.pace}")

        if (isGuidedMode) {
            trainingSessionManager?.clear()
            trainingSessionManager = null

            // Use the stored application context instead of trying to get it from trainingSessionManager
            if (applicationContext != null) {
                Log.d("RecordViewModel", "Reinitializing TrainingSessionManager with new training day")
                val voiceService = VoiceGuidanceService(applicationContext!!)
                trainingSessionManager = TrainingSessionManager(trainingDay.session, voiceService)
            } else {
                Log.w("RecordViewModel", "Cannot initialize TrainingSessionManager: no context available")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            trainingSessionManager?.clear()
            trainingSessionManager = null
            // Don't clear applicationContext here, as it may be needed until the ViewModel is completely destroyed
        } catch (e: Exception) {
            Log.e("RecordViewModel", "Error clearing training session", e)
        }
    }

    fun getCurrentTrainingDay() {
        viewModelScope.launch {
            _getCurrentTrainingDay.value = Resource.Loading
            val result = trainingDayRepository.getCurrentTrainingDay()
            _getCurrentTrainingDay.value = result

            if (isGuidedMode && result is Resource.Success) {
                setCurrentTrainingDay(result.value)
            }
        }
    }

    private fun updateGuidedModeStats() {
        if (!isGuidedMode || _trainingDay.value == null) return

        val trainingDay = _trainingDay.value!!
        val targetPace = trainingDay.session.pace
        val targetDistance = trainingDay.session.distance

        val progress = if (targetDistance > 0) (totalDistance / targetDistance) * 100 else 0.0
        val paceDifference = currentAvgPace - targetPace
        val toleranceThreshold = 0.5 // 30 seconds tolerance

        val paceStatus = when {
            currentAvgPace == 0.0 -> PaceStatus.NOT_STARTED
            kotlin.math.abs(paceDifference) <= toleranceThreshold -> PaceStatus.ON_TARGET
            paceDifference > toleranceThreshold -> PaceStatus.TOO_SLOW
            else -> PaceStatus.TOO_FAST
        }

        val stats = GuidedModeStats(
            currentPace = currentAvgPace,
            targetPace = targetPace,
            currentDistance = totalDistance,
            targetDistance = targetDistance,
            progressPercentage = progress.coerceAtMost(100.0),
            paceStatus = paceStatus,
            isOnTrack = paceStatus == PaceStatus.ON_TARGET
        )

        _guidedModeStats.value = stats
    }

    fun getPerformanceFeedback(): String {
        if (!isGuidedMode || _trainingDay.value == null) return ""

        val stats = _guidedModeStats.value
        return when (stats.paceStatus) {
            PaceStatus.NOT_STARTED -> "Bắt đầu chạy để theo dõi tiến độ"
            PaceStatus.ON_TARGET -> when {
                stats.progressPercentage >= 100 -> "Xuất sắc! Bạn đã hoàn thành mục tiêu!"
                stats.progressPercentage >= 75 -> "Tuyệt vời! Sắp hoàn thành rồi!"
                else -> "Tuyệt vời! Duy trì nhịp độ này để đạt mục tiêu"
            }
            PaceStatus.TOO_SLOW -> "Tăng tốc một chút để đạt được nhịp độ mục tiêu"
            PaceStatus.TOO_FAST -> "Giảm tốc một chút để tiết kiệm năng lượng"
        }
    }

    // Add function to get pace comparison text
    fun getPaceComparisonText(): String {
        if (!isGuidedMode || _trainingDay.value == null || currentAvgPace == 0.0) return "Chưa bắt đầu"

        val targetPace = _trainingDay.value!!.session.pace
        val paceDifference = currentAvgPace - targetPace
        val toleranceThreshold = 0.5

        return when {
            kotlin.math.abs(paceDifference) <= toleranceThreshold -> "Đúng mục tiêu!"
            paceDifference > toleranceThreshold -> {
                val diff = (paceDifference * 60).toInt()
                "Chậm hơn ${diff}s"
            }
            else -> {
                val diff = (kotlin.math.abs(paceDifference) * 60).toInt()
                "Nhanh hơn ${diff}s"
            }
        }
    }
}