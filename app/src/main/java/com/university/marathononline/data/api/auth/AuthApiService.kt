package com.university.marathononline.data.api.auth

import com.university.marathononline.data.models.User
import com.university.marathononline.data.request.AuthRequest
import com.university.marathononline.data.request.RefreshTokenRequest
import com.university.marathononline.data.response.AuthResponse
import com.university.marathononline.data.response.TokenResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST


interface AuthApiService {
    @GET("/api/v1/auth")
    suspend fun getUser(): User

    @PATCH("/api/v1/auth")
    suspend fun verifyAccount(): User

    @POST("/api/v1/auth")
    suspend fun authenticate(@Body authRequest: AuthRequest): AuthResponse

    @POST("/api/v1/auth/refresh")
    suspend fun refreshAccessToken(@Body request: RefreshTokenRequest): TokenResponse

    @POST("/api/v1/auth/logout")
    suspend fun logout(): String

    @DELETE("/api/v1/auth")
    suspend fun deleteAccount(): User
}