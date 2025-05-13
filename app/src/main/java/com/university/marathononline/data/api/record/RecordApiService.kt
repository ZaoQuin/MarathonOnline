package com.university.marathononline.data.api.record

import com.university.marathononline.data.models.Record
import com.university.marathononline.data.request.CreateRecordRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RecordApiService {
    @POST("/api/v1/record")
    suspend fun addRecordAndSaveIntoRegistration(@Body race: CreateRecordRequest): Record

    @GET("/api/v1/record/runner")
    suspend fun getByRunner(): List<Record>
}