package com.university.marathononline.ui.viewModel.tracking

import android.annotation.SuppressLint
import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.university.marathononline.utils.formatDistance

class RecordingManager {

    private val _time = MutableStateFlow("0:00:00")
    val time: StateFlow<String> = _time

    private val _averagePace = MutableStateFlow("-- min/km")
    val averagePace: StateFlow<String> = _averagePace

    private val _distance = MutableStateFlow("0 km")
    val distance: StateFlow<String> = _distance

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    var startTime: Long = 0
        private set

    var totalDistance: Double = 0.0
        private set

    var currentAvgPace: Double = 0.0

    fun startRecording() {
        reset()
        startTime = SystemClock.elapsedRealtime()
        _isRecording.value = true
    }

    fun stopRecording() {
        _isRecording.value = false
    }

    fun updateDistance(additionalDistance: Double) {
        totalDistance += additionalDistance
        _distance.value = formatDistance(totalDistance)

        // Recalculate average pace
        updateAveragePace()
    }

    @SuppressLint("DefaultLocale")
    fun updateTime() {
        val elapsedTime = (SystemClock.elapsedRealtime() - startTime) / 1000
        val hours = (elapsedTime / 3600).toInt()
        val minutes = ((elapsedTime % 3600) / 60).toInt()
        val seconds = (elapsedTime % 60).toInt()
        _time.value = String.format("%d:%02d:%02d", hours, minutes, seconds)

        // Update average pace if distance has changed
        if (totalDistance > 0) {
            updateAveragePace()
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateAveragePace() {
        currentAvgPace = calculateAveragePace()
        val avgPaceMinutes = currentAvgPace.toInt()
        val avgPaceSeconds = ((currentAvgPace - avgPaceMinutes) * 60).toInt()
        _averagePace.value = String.format("%d:%02d min/km", avgPaceMinutes, avgPaceSeconds)
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

    fun getElapsedTimeInSeconds(): Long {
        return (SystemClock.elapsedRealtime() - startTime) / 1000
    }

    fun getAverageSpeed(): Double {
        val timeTakenInSeconds = getElapsedTimeInSeconds()
        return if (totalDistance > 0) {
            totalDistance / (timeTakenInSeconds / 3600f)
        } else {
            0.0
        }
    }

    fun setDistance(distance: Double) {
        totalDistance = distance
        _distance.value = formatDistance(totalDistance)
        updateAveragePace()
    }

    private fun reset() {
        _time.value = "0:00:00"
        _averagePace.value = "-- min/km"
        _distance.value = "0 km"
        totalDistance = 0.0
        currentAvgPace = 0.0
    }
}