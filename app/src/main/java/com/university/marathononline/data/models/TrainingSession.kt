package com.university.marathononline.data.models

import java.io.Serializable

data class TrainingSession(
    var id: Long,
    var name: String,
    var type: ETrainingSessionType,
    var distance: Double,
    var pace: Double,
    var notes: String
): Serializable

enum class ETrainingSessionType(val value: String, val maxRestMinutes: Long) {
    LONG_RUN("Chạy dài", 5),
    RECOVERY_RUN("Chạy hồi phục", 10),
    SPEED_WORK("Tập tốc độ", 3),
    REST("Nghỉ", Long.MAX_VALUE);
    override fun toString(): String = value
    fun canRestWithin(minutes: Long): Boolean {
        return minutes <= maxRestMinutes
    }
}
