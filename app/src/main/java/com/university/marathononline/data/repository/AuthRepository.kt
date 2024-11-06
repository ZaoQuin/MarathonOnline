package com.university.marathononline.data.repository

import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.request.AuthRequest
import com.university.marathononline.data.request.RefreshTokenRequest
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.response.UserPreferences

class AuthRepository(
    private var api: AuthApiService,
    private val preferences: UserPreferences
): BaseRepository() {

    suspend fun getUser() = safeApiCall {
        api.getUser()
    }

    suspend fun authenticate(authRequest: AuthRequest) = safeApiCall {
        api.authenticate(authRequest)
    }

    suspend fun refreshAccessToken(refreshRequest: RefreshTokenRequest) = safeApiCall {
        api.refreshAccessToken(refreshRequest)
    }

    suspend fun logout() = safeApiCall {
        api.logout()
    }

    suspend fun saveAuthToken(token: String){
        preferences.saveAuthToken(token)
    }
}