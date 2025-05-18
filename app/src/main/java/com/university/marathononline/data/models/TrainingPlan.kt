package com.university.marathononline.data.models

data class TrainingPlan(
    var id: Long,
    var name: String,
    var input: TrainingPlanInput,
    var startDate: String,
    var endDate: String,
    var status: ETrainingPlanStatus,
    var trainingDays: List<TrainingDay>,
    var completedDays: Int,
    var remainingDays: Int,
    var totalDistance: Double,
    var progress: Double
)

enum class ETrainingPlanStatus(val value: String) {
    ACTIVE("Đang thực hiện"),
    COMPLETED("Hoàn thành"),
    ARCHIVED("Lưu trữ");
    override fun toString(): String = value
}