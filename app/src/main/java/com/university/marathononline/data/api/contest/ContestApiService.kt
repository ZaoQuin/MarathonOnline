package com.university.marathononline.data.api.contest

import com.university.marathononline.data.models.Contest
import retrofit2.http.GET

interface ContestApiService {
    @GET("/api/v1/contest")
    suspend fun getContests(): List<Contest>
}