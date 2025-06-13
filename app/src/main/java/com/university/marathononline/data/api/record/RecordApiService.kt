package com.university.marathononline.data.api.record

import com.university.marathononline.data.models.Record
import com.university.marathononline.data.request.CreateRecordRequest
import com.university.marathononline.data.response.StringResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface RecordApiService {
    @POST("/api/v1/record")
    suspend fun addRecordAndSaveIntoRegistration(@Body record: CreateRecordRequest): Record

    @GET("/api/v1/record/runner")
    suspend fun getByRunner(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): List<Record>

    @POST("/api/v1/record/sync")
    suspend fun sync(@Body records: List<CreateRecordRequest>): StringResponse

    @GET("/api/v1/record/{recordId}")
    suspend fun getById(@Path("recordId") id: Long): Record
}