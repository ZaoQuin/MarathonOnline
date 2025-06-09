package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.feedback.CreateFeedbackRequest
import com.university.marathononline.data.api.feedback.FeedbackApiService

class FeedbackRepository(
    private val api: FeedbackApiService
): BaseRepository() {
    suspend fun createFeedback(recordId: Long, message: String) = safeApiCall {
        api.createFeedback(recordId, CreateFeedbackRequest(message))
    }

    suspend fun getFeedbacksByRecord(recordId: Long) = safeApiCall {
        api.getFeedbacksByRecord(recordId)
    }

    suspend fun deleteFeedback(feedbackId: Long) = safeApiCall {
        api.deleteFeedback(feedbackId)
    }

    suspend fun getMyFeedbacks() = safeApiCall {
        api.getMyFeedbacks()
    }

    suspend fun getById(id: Long) = safeApiCall {
        api.getById(id)
    }
}