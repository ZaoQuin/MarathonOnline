package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.training.TrainingPlanApiService
import com.university.marathononline.data.api.training.InputTrainingPlanRequest

class TrainingPlanRepository(
    private val api: TrainingPlanApiService
) : BaseRepository() {
    suspend fun getCurrentTrainingPlan() = safeApiCall {
        api.getCurrentTrainingPlan()
    }

    suspend fun generateTrainingPlan(input: InputTrainingPlanRequest) = safeApiCall {
        api.generateTrainingPlan(input)
    }

    suspend fun getUserTrainingPlans(userId: Long) = safeApiCall {
        api.getUserTrainingPlans(userId)
    }

    suspend fun getTrainingPlanById(planId: Long) = safeApiCall {
        api.getTrainingPlanById(planId)
    }

    suspend fun getCompletedPlans(
        page: Int,
        size: Int,
        startDate: String? = null,
        endDate: String? = null
    ) = safeApiCall {
        api.getCompletedPlans(page, size, startDate, endDate)
    }

    suspend fun getArchivedPlans(
        page: Int,
        size: Int,
        startDate: String? = null,
        endDate: String? = null
    ) = safeApiCall {
        api.getArchivedPlans(page, size, startDate, endDate)
    }
}