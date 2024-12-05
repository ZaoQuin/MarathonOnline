package com.university.marathononline.data.api.contest

import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.response.GetContestsResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ContestApiService {
    @GET("/api/v1/contest")
    suspend fun getContests(): GetContestsResponse

    @GET("/api/v1/contest/home")
    suspend fun getHomeContests(): GetContestsResponse

    @GET("/api/v1/contest/runner")
    suspend fun getByRunner(): GetContestsResponse

    @GET("/api/v1/contest/{id}")
    suspend fun getById(@Path("id") id: Long): Contest

}