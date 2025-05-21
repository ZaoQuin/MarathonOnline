package com.university.marathononline.data.api.trainingDay

import com.university.marathononline.data.models.Record
import com.university.marathononline.data.models.TrainingDay
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TrainingDayApiService {
    @GET("/api/v1/training-day")
    suspend fun getCurrentTrainingDay(): TrainingDay

    @POST("/api/v1/training-day/record")
    suspend fun saveRecordIntoTrainingDay(
        @Body record: Record
    ): TrainingDay

    @POST("/api/v1/training-day/reset")
    suspend fun resetTrainingDay(): String
}