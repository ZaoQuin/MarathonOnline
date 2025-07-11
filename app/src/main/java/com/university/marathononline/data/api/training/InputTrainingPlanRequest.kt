package com.university.marathononline.data.api.training

import com.university.marathononline.data.models.ETrainingPlanInputGoal
import com.university.marathononline.data.models.ETrainingPlanInputLevel

data class InputTrainingPlanRequest(
    var level: ETrainingPlanInputLevel,
    var goal: ETrainingPlanInputGoal,
    var trainingWeeks: Int
)
