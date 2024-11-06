package com.university.marathononline.data.api.user

import com.university.marathononline.data.models.User
import retrofit2.http.GET
import retrofit2.http.Path

interface UserApiService {

    @GET("/api/v1/user")
    suspend fun getUsers(): List<User>

    @GET("/api/v1/user/{id}")
    suspend fun getUser(@Path("id") id: Long): User
}