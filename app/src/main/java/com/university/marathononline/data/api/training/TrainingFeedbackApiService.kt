package com.university.marathononline.data.api.training

import com.university.marathononline.data.models.TrainingFeedback
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TrainingFeedbackApiService {
    @POST("/api/v1/training-feedback/{trainingDayId}")
    suspend fun submitFeedback(@Path("trainingDayId") trainingDayId: Long,
                               @Body feedback: TrainingFeedback): TrainingFeedback

    @GET("/api/v1/training-feedback/{trainingDayId}")
    suspend fun getFeedback(@Path("trainingDayId") trainingDayId: Long): TrainingFeedback
}