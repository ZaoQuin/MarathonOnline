package com.university.marathononline.data.models

import java.io.Serializable

data class TrainingDay (
    var id: Long,
    var week: Int,
    var dayOfWeek: Int,
    var session: TrainingSession,
    var records: List<Record>,
    var status: ETrainingDayStatus,
    var dateTime: String,
    var trainingFeedback: TrainingFeedback?= null,
    var completionPercentage: Double
): Serializable

enum class ETrainingDayStatus(val value: String) {
    ACTIVE("Đang thực hiện"),
    COMPLETED("Hoàn thành"),
    PARTIALLY_COMPLETED("Hoàn thành một phần"),
    SKIPPED("Bỏ qua"),
    MISSED("Bỏ lỡ");
    override fun toString(): String = value
}