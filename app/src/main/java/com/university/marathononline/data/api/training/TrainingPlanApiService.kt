package com.university.marathononline.data.api.training

import com.university.marathononline.data.models.SingleTrainingPlan
import com.university.marathononline.data.models.TrainingPlan
import com.university.marathononline.data.request.InputTrainingPlanRequest
import com.university.marathononline.data.response.PageResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TrainingPlanApiService {
    @GET("/api/v1/training-plan")
    suspend fun getCurrentTrainingPlan(): TrainingPlan

    @POST("/api/v1/training-plan/generate")
    suspend fun generateTrainingPlan(
        @Body input: InputTrainingPlanRequest
    ): TrainingPlan

    @GET("/api/v1/training-plan/user/{userId}")
    suspend fun getUserTrainingPlans(
        @Path("userId") userId: Long
    ): List<TrainingPlan>

    @GET("/api/v1/training-plan/{planId}")
    suspend fun getTrainingPlanById(
        @Path("planId") planId: Long
    ): TrainingPlan

    @GET("/api/v1/training-plan/completed")
    suspend fun getCompletedPlans(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("startDate") startDate: String? = null, // ISO-8601 string
        @Query("endDate") endDate: String? = null
    ): PageResponse<SingleTrainingPlan>

    @GET("/api/v1/training-plan/archived")
    suspend fun getArchivedPlans(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): PageResponse<SingleTrainingPlan>
}