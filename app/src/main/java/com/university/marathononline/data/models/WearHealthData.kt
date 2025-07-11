package com.university.marathononline.data.models

data class WearHealthData(
    val heartRate: Double = 0.0,
    val steps: Int = 0,
    val distance: Double = 0.0,
    val speed: Double = 0.0,
    val calories: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val isRecording: Boolean = false
) {
    companion object {
        const val DATA_PATH = "/health_data"
        const val START_RECORDING_PATH = "/start_recording"
        const val STOP_RECORDING_PATH = "/stop_recording"
    }
}