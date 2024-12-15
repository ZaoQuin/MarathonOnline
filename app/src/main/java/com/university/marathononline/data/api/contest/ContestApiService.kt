package com.university.marathononline.data.api.contest

import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.request.CheckContestNameRequest
import com.university.marathononline.data.request.CreateContestRequest
import com.university.marathononline.data.response.CheckActiveContestResponse
import com.university.marathononline.data.response.CheckContestNameResponse
import com.university.marathononline.data.response.DeleteResponse
import com.university.marathononline.data.response.GetContestsResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ContestApiService {
    @GET("/api/v1/contest/active-and-finish")
    suspend fun getContests(): GetContestsResponse

    @GET("/api/v1/contest/home")
    suspend fun getHomeContests(): GetContestsResponse

    @GET("/api/v1/contest/runner")
    suspend fun getByRunner(): GetContestsResponse

    @GET("/api/v1/contest/{id}")
    suspend fun getById(@Path("id") id: Long): Contest

    @POST("/api/v1/contest")
    suspend fun addContest(@Body createContestRequest: CreateContestRequest): Contest

    @PUT("/api/v1/contest")
    suspend fun updateContest(@Body contest:Contest): Contest

    @GET("/api/v1/contest/jwt")
    suspend fun getContestsByJwt(): List<Contest>

    @DELETE("/api/v1/contest/{id}")
    suspend fun deleteById(@Path("id") contestId: Long): DeleteResponse

    @PUT("/api/v1/contest/cancel")
    suspend fun cancel(@Body contest:Contest): Contest

    @PUT("/api/v1/contest/prizes")
    suspend fun completed(@Body contest: Contest): Contest

    @POST("/api/v1/contest/check-name")
    suspend fun isExistName(@Body request: CheckContestNameRequest): CheckContestNameResponse

    @POST("/api/v1/contest/check-active")
    suspend fun checkActiveContest(): CheckActiveContestResponse
}