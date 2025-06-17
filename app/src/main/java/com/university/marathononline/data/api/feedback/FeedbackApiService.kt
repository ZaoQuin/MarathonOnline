package com.university.marathononline.data.api.feedback

import com.university.marathononline.data.models.Feedback
import com.university.marathononline.data.api.share.StringResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FeedbackApiService {
    @POST("api/v1/feedback/record/{recordId}")
    suspend fun createFeedback(
        @Path("recordId") recordId: Long,
        @Body request: CreateFeedbackRequest
    ): Feedback

    @GET("api/v1/feedback/record/{recordId}")
    suspend fun getFeedbacksByRecord(
        @Path("recordId") recordId: Long
    ): List<Feedback>

    @DELETE("api/v1/feedback/{feedbackId}")
    suspend fun deleteFeedback(
        @Path("feedbackId") feedbackId: Long
    ): StringResponse

    @GET("api/v1/feedback/my-feedbacks")
    suspend fun getMyFeedbacks(): List<Feedback>

    @GET("api/v1/feedback/{feedbackId}")
    suspend fun getById(@Path("feedbackId") feedbackId: Long): Feedback

    @GET("api/v1/feedback/registration/{registrationId}")
    suspend fun getFeedbacksByRegistration(@Path("registrationId") registrationId: Long): List<Feedback>

    @POST("api/v1/feedback/registration/{registrationId}")
    suspend fun createRegistrationFeedback(
        @Path("registrationId") registrationId: Long,
        @Body createFeedbackRequest: CreateFeedbackRequest
    ): Feedback
}