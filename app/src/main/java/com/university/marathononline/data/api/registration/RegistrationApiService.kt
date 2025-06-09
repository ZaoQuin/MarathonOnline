package com.university.marathononline.data.api.registration

import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.Record
import com.university.marathononline.data.models.Registration
import com.university.marathononline.data.response.RegistrationsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RegistrationApiService {
    @POST("/api/v1/registration/record")
    suspend fun saveRecordIntoRegistration(@Body record: Record): RegistrationsResponse

    @POST("/api/v1/registration")
    suspend fun registerForContest(@Body contest: Contest): Registration

    @PUT("/api/v1/registration/block")
    suspend fun block(@Body registration: Registration): Registration

    @PUT("/api/v1/registration/prizes")
    suspend fun prizes(@Body contest: Contest): List<Registration>

    @GET("/api/v1/registration/{id}")
    suspend fun getById(@Path("id") id: Long): Registration
}