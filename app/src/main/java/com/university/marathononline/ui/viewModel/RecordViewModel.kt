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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
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

    // Expose StateFlows from managers
    val time: StateFlow<String> get() = recordingManager.time
    val averagePace: StateFlow<String> get() = recordingManager.averagePace
    val distance: StateFlow<String> get() = recordingManager.distance
    val steps: StateFlow<Int> get() = stepCounter.steps
    val isRecording: StateFlow<Boolean> get() = recordingManager.isRecording
    val position: StateFlow<String> get() = locationTracker.position
    val routes: StateFlow<List<LatLng>> get() = locationTracker.routes
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

        // Set up location update callback
        locationTracker.onLocationUpdate = { _, distanceInKm ->
            recordingManager.updateDistance(distanceInKm)
            if (guidedModeManager.isEnabled()) {
                guidedModeManager.updateStats(
                    currentPace = recordingManager.currentAvgPace,
                    currentDistance = recordingManager.totalDistance,
                    elapsedTime = recordingManager.getElapsedTimeInSeconds() * 1000
                )
            }
        }

        // Observe GPS status
        viewModelScope.launch {
            locationTracker.isGPSEnabled.collect { isEnabled ->
                _isGPSEnabled.postValue(isEnabled)
            }
        }
    }

    fun initializeWearIntegration() {
        wearIntegrationManager.initialize()
        // Set up callbacks for wear integration
        wearIntegrationManager.onStartRecording = {
            applicationContext?.let { startRecording(it) }
        }
        wearIntegrationManager.onStopRecording = {
            stopRecording()
        }
        wearIntegrationManager.onHealthDataUpdate = { wearData ->
            if (isRecording.value) {
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
        if (!locationTracker.startLocationUpdates()) {
            Log.w("RecordViewModel", "Cannot start recording: location permissions not granted")
            return
        }

        if (wearIntegrationManager.isWearConnected.value) {
            // Chỉ dựa vào dữ liệu từ đồng hồ
            Log.d("RecordViewModel", "Using wear data, disabling phone sensors")
            locationTracker.stopLocationUpdates()
            stepCounter.stopCounting()
        } else if (!locationTracker.startLocationUpdates()) {
            Log.w("RecordViewModel", "Cannot start recording: location permissions not granted")
            return
        }

        recordingManager.startRecording()
        stepCounter.startCounting()

        // Start time updater
        viewModelScope.launch {
            while (recordingManager.isRecording.value) {
                recordingManager.updateTime()
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun updateUIFromWearData(wearData: WearHealthData) {
        if (wearData.distance > 0) {
            recordingManager.setDistance(maxOf(recordingManager.totalDistance, wearData.distance))
        }
        if (wearData.steps > 0) {
            stepCounter.setSteps(maxOf(stepCounter.steps.value, wearData.steps))
        }
        if (wearData.speed > 0) {
            recordingManager.currentAvgPace = 60.0 / wearData.speed
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

        locationTracker.stopLocationUpdates()
        stepCounter.stopCounting()
        recordingManager.stopRecording()

        val createRecordRequest = CreateRecordRequest(
            steps = stepCounter.steps.value,
            distance = recordingManager.totalDistance,
            timeTaken = recordingManager.getElapsedTimeInSeconds(),
            avgSpeed = recordingManager.getAverageSpeed(),
            timestamp = LocalDateTime.now().toString()
        )

        Log.d("RecordViewModel", "Recording stopped:")
        Log.d("RecordViewModel", "Time: ${createRecordRequest.timeTaken} seconds")
        Log.d("RecordViewModel", "Distance: ${createRecordRequest.distance} km")
        Log.d("RecordViewModel", "Speed: ${createRecordRequest.avgSpeed} km/h")
        Log.d("RecordViewModel", "Steps: ${createRecordRequest.steps}")

        createRecord(createRecordRequest)
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
        guidedModeManager.setTrainingDay(trainingDay)
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

    override fun onCleared() {
        super.onCleared()
        guidedModeManager.clear()
        locationTracker.reset()
        stepCounter.reset()
    }
}