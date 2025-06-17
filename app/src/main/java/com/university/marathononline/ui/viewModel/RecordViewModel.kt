package com.university.marathononline.ui.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.university.marathononline.base.BaseViewModel
import com.university.marathononline.data.api.Resource
import com.university.marathononline.data.models.GuidedModeStats
import com.university.marathononline.data.models.Record
import com.university.marathononline.data.models.TrainingDay
import com.university.marathononline.data.models.WearHealthData
import com.university.marathononline.data.repository.RegistrationRepository
import com.university.marathononline.data.repository.RecordRepository
import com.university.marathononline.data.repository.TrainingDayRepository
import com.university.marathononline.data.request.CreateRecordRequest
import com.university.marathononline.data.response.RegistrationsResponse
import com.university.marathononline.ui.viewModel.tracking.GuidedModeManager
import com.university.marathononline.ui.viewModel.tracking.LocationTracker
import com.university.marathononline.ui.viewModel.tracking.RecordingManager
import com.university.marathononline.ui.viewModel.tracking.StepCounter
import com.university.marathononline.ui.viewModel.tracking.WearIntegrationManager
import com.google.android.gms.maps.model.LatLng
import com.university.marathononline.data.models.ERecordSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private lateinit var locationTracker: LocationTracker
    private lateinit var wearIntegrationManager: WearIntegrationManager
    private lateinit var recordingManager: RecordingManager
    private lateinit var stepCounter: StepCounter
    private lateinit var guidedModeManager: GuidedModeManager

    private var applicationContext: Context? = null

    private var isUsingWearForTracking = false

    private val _routesOverride = MutableStateFlow<List<LatLng>>(emptyList())

    val time: StateFlow<String> get() = recordingManager.time
    val averagePace: StateFlow<String> get() = recordingManager.averagePace
    val distance: StateFlow<String> get() = recordingManager.distance
    val steps: StateFlow<Int> get() = stepCounter.steps
    val isRecording: StateFlow<Boolean> get() = recordingManager.isRecording
    val position: StateFlow<String> get() = locationTracker.position
    private var startTime: LocalDateTime? = null
    private var latestHeartRate: Double = 0.0

    val routes: StateFlow<List<LatLng>> get() =
        if (isUsingWearForTracking) _routesOverride else locationTracker.routes

    val guidedModeStats: StateFlow<GuidedModeStats> get() = guidedModeManager.guidedModeStats
    val wearHealthData: StateFlow<WearHealthData?> get() = wearIntegrationManager.wearHealthData
    val isWearConnected: StateFlow<Boolean> get() = wearIntegrationManager.isWearConnected

    fun initializeLocationTracking(context: Context) {
        applicationContext = context.applicationContext
        locationTracker = LocationTracker(context)
        stepCounter = StepCounter(context)
        guidedModeManager = GuidedModeManager(context)
        recordingManager = RecordingManager()
        wearIntegrationManager = WearIntegrationManager(context, viewModelScope)

        _trainingDay.value?.let { trainingDay ->
            guidedModeManager.setTrainingDay(trainingDay)
            Log.d("RecordViewModel", "Set pending training day after initialization")
        }

        locationTracker.onLocationUpdate = { _, distanceInKm ->
            if (!isUsingWearForTracking) {
                recordingManager.updateDistance(distanceInKm)
                if (guidedModeManager.isEnabled()) {
                    guidedModeManager.updateStats(
                        currentPace = recordingManager.currentAvgPace,
                        currentDistance = recordingManager.totalDistance,
                        elapsedTime = recordingManager.getElapsedTimeInSeconds() * 1000
                    )
                }
            }
        }

        viewModelScope.launch {
            locationTracker.isGPSEnabled.collect { isEnabled ->
                _isGPSEnabled.postValue(isEnabled)
            }
        }

        viewModelScope.launch {
            wearIntegrationManager.isWearConnected.collect { isConnected ->
                handleWearConnectionChange(isConnected)
            }
        }
    }

    private fun handleWearConnectionChange(isConnected: Boolean) {
        if (isConnected) {
            Log.d("RecordViewModel", "Wear connected - switching to wear tracking mode")
            isUsingWearForTracking = true

            if (recordingManager.isRecording.value) {
                locationTracker.stopLocationUpdates()
                stepCounter.stopCounting()
            }

            _routesOverride.value = emptyList()

        } else {
            Log.d("RecordViewModel", "Wear disconnected - switching to phone tracking mode")
            isUsingWearForTracking = false

            if (recordingManager.isRecording.value) {
                locationTracker.startLocationUpdates()
                stepCounter.startCounting()
            }
        }
    }

    fun initializeWearIntegration() {
        wearIntegrationManager.initialize()
        wearIntegrationManager.onStartRecording = {
            applicationContext?.let { startRecording(it) }
        }
        wearIntegrationManager.onStopRecording = {
            stopRecording()
        }
        wearIntegrationManager.onHealthDataUpdate = { wearData ->
            latestHeartRate = wearData.heartRate
            if (isRecording.value && isUsingWearForTracking) {
                updateUIFromWearData(wearData)
            }
        }
    }

    fun setGuidedMode(enabled: Boolean, context: Context? = null) {
        val contextToUse = context?.applicationContext ?: applicationContext
        if (contextToUse == null) {
            Log.e("RecordViewModel", "Cannot set guided mode: no context available")
            return
        }

        if (enabled && _trainingDay.value == null) {
            getCurrentTrainingDay()
        } else {
            guidedModeManager.setGuidedMode(enabled, _trainingDay.value)
        }
    }

    fun startRecording(context: Context) {
        applicationContext = context.applicationContext

        startTime = LocalDateTime.now()

        recordingManager.startRecording()

        if (isUsingWearForTracking) {
            Log.d("RecordViewModel", "Using wear data for tracking - GPS disabled")
            locationTracker.stopLocationUpdates()
            stepCounter.stopCounting()
            _routesOverride.value = emptyList()
        } else {
            Log.d("RecordViewModel", "Using phone sensors for tracking")
            if (!locationTracker.startLocationUpdates()) {
                Log.w("RecordViewModel", "Cannot start recording: location permissions not granted")
                return
            }
            stepCounter.startCounting()
        }

        viewModelScope.launch {
            while (recordingManager.isRecording.value) {
                recordingManager.updateTime()
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun updateUIFromWearData(wearData: WearHealthData) {
        if (!isUsingWearForTracking) return

        Log.d("RecordViewModel", "Updating UI from Wear data: distance=${wearData.distance}, steps=${wearData.steps}, speed=${wearData.speed}")

        latestHeartRate = wearData.heartRate

        if (wearData.distance > 0) {
            recordingManager.setDistance(wearData.distance)
        }
        if (wearData.steps > 0) {
            stepCounter.setSteps(wearData.steps)
        }
        if (wearData.speed > 0) {
            recordingManager.currentAvgPace = if (wearData.speed > 0) 60.0 / wearData.speed else 0.0
            recordingManager.updateTime()
        }
        if (guidedModeManager.isEnabled()) {
            guidedModeManager.updateStats(
                currentPace = recordingManager.currentAvgPace,
                currentDistance = recordingManager.totalDistance,
                elapsedTime = recordingManager.getElapsedTimeInSeconds() * 1000
            )
        }
    }

    fun stopRecording() {
        if (!recordingManager.isRecording.value) return

        val heartRateToUse = if (isUsingWearForTracking) latestHeartRate else 0.0

        locationTracker.stopLocationUpdates()
        stepCounter.stopCounting()
        recordingManager.stopRecording()

        val createRecordRequest = CreateRecordRequest(
            steps = stepCounter.steps.value,
            distance = recordingManager.totalDistance,
            avgSpeed = recordingManager.getAverageSpeed(),
            heartRate = heartRateToUse,
            startTime = startTime?.toString() ?: LocalDateTime.now().toString(),
            endTime = LocalDateTime.now().toString(),
            source = ERecordSource.DEVICE
        )


        Log.d("RecordViewModel", "Recording stopped:")
        Log.d("RecordViewModel", "Start Time: ${createRecordRequest.startTime}")
        Log.d("RecordViewModel", "End Time: ${createRecordRequest.endTime}")
        Log.d("RecordViewModel", "Distance: ${createRecordRequest.distance} km")
        Log.d("RecordViewModel", "Speed: ${createRecordRequest.avgSpeed} km/h")
        Log.d("RecordViewModel", "Steps: ${createRecordRequest.steps}")
        Log.d("RecordViewModel", "Data source: ${if (isUsingWearForTracking) "Wear OS" else "Phone sensors"}")

        createRecord(createRecordRequest)
    }

    fun createRecord(request: CreateRecordRequest) {
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

        if (::guidedModeManager.isInitialized) {
            guidedModeManager.setTrainingDay(trainingDay)
        } else {
            Log.w("RecordViewModel", "GuidedModeManager not initialized yet, will set training day when initialized")
        }
    }

    fun getCurrentTrainingDay() {
        viewModelScope.launch {
            _getCurrentTrainingDay.value = Resource.Loading
            val result = trainingDayRepository.getCurrentTrainingDay()
            _getCurrentTrainingDay.value = result
            if (result is Resource.Success) {
                setCurrentTrainingDay(result.value)
            }
        }
    }

    fun refreshWearConnection() {
        wearIntegrationManager.refreshConnection()
    }

    fun isUsingWearTracking(): Boolean = isUsingWearForTracking

    override fun onCleared() {
        super.onCleared()

        if (::guidedModeManager.isInitialized) {
            guidedModeManager.clear()
        }

        if (::locationTracker.isInitialized) {
            locationTracker.reset()
        }

        if (::stepCounter.isInitialized) {
            stepCounter.reset()
        }

        if (::wearIntegrationManager.isInitialized) {
        }

        if (::recordingManager.isInitialized) {
        }
    }
}