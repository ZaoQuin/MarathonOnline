package com.university.marathononline.data.models

data class SingleTrainingPlan(
    var id: Long,
    var name: String,
    var startDate: String,
    var endDate: String,
    var status: ETrainingPlanStatus
)