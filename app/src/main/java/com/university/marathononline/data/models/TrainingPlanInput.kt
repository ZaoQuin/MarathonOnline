package com.university.marathononline.data.models

import java.io.Serializable

data class TrainingPlanInput (
    var level: ETrainingPlanInputLevel,
    var goal: ETrainingPlanInputGoal,
    var maxDistance: Double,
    var averagePace: Double,
    var trainingWeeks: Int
): Serializable

enum class ETrainingPlanInputLevel(val value: String) {
    BEGINNER("Người mới bắt đầu"),
    INTERMEDIATE("Trung bình"),
    ADVANCED("Nâng cao");
    override fun toString(): String = value
}

enum class ETrainingPlanInputGoal(val value: String) {
    MARATHON_FINISH("Về đích Marathon"),
    MARATHON_TIME("Phá kỷ lục thời gian Marathon"),
    HALF_MARATHON_FINISH("Về đích Half Marathon"),
    HALF_MARATHON_TIME("Phá kỷ lục thời gian Half Marathon"),
    TEN_KM_FINISH("Hoàn thành 10 km"),
    TEN_KM_TIME("Phá kỷ lục thời gian 10 km"),
    FIVE_KM_FINISH("Hoàn thành 5 km"),
    FIVE_KM_TIME("Phá kỷ lục thời gian 5 km"),
    OTHER("Mục tiêu khác");
    override fun toString(): String = value
}