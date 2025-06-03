package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.training.TrainingFeedbackApiService
import com.university.marathononline.data.models.TrainingFeedback

class TrainingFeedbackRepository(
    private val api: TrainingFeedbackApiService
) : BaseRepository() {

    suspend fun submitFeedback(trainingDayId: Long, feedback: TrainingFeedback) = safeApiCall {
        api.submitFeedback(trainingDayId, feedback)
    }

    suspend fun getFeedback(trainingDayId: Long) = safeApiCall {
        api.getFeedback(trainingDayId)
    }
}