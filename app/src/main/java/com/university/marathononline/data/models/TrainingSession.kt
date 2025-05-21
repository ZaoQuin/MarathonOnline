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

enum class ETrainingSessionType(val value: String) {
    LONG_RUN("Chạy dài"),
    RECOVERY_RUN("Chạy hồi phục"),
    SPEED_WORK("Tập tốc độ"),
    REST("Nghỉ");
    override fun toString(): String = value
}
