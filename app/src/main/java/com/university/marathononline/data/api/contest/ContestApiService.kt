package com.university.marathononline.data.api.contest

import com.university.marathononline.data.response.GetContestsResponse
import retrofit2.http.GET

interface ContestApiService {
    @GET("/api/v1/contest")
    suspend fun getContests(): GetContestsResponse

    @GET("/api/v1/contest/home")
    suspend fun getHomeContests(): GetContestsResponse

    @GET("/api/v1/contest/runner")
    suspend fun getByRunner(): GetContestsResponse
}