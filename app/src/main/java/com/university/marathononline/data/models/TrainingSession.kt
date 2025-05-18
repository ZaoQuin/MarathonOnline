package com.university.marathononline.data.models

data class TrainingSession(
    var id: Long,
    var name: String,
    var type: ETrainingSessionType,
    var distance: Double,
    var pace: Double,
    var notes: String,
    var dateTime: String
)

enum class ETrainingSessionType(val value: String) {
    LONG_RUN("Chạy dài"),
    RECOVERY_RUN("Chạy hồi phục"),
    SPEED_WORK("Tập tốc độ"),
    REST("Nghỉ");
    override fun toString(): String = value
}
