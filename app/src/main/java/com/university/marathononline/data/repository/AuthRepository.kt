package com.university.marathononline.data.repository

import androidx.lifecycle.viewModelScope
import com.university.marathononline.data.api.auth.AuthApiService
import com.university.marathononline.data.request.AuthRequest
import com.university.marathononline.data.request.RefreshTokenRequest
import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.models.LoginInfo
import com.university.marathononline.data.response.AuthResponse
import com.university.marathononline.data.response.UserPreferences
import kotlinx.coroutines.flow.first

class AuthRepository(
    private var api: AuthApiService,
    private val preferences: UserPreferences
): BaseRepository() {

    suspend fun getUser() = safeApiCall {
        api.getUser()
    }

    suspend fun verifyAccount() = safeApiCall {
        api.verifyAccount()
    }

    suspend fun authenticate(authRequest: AuthRequest) = safeApiCall {
        api.authenticate(authRequest)
    }

    suspend fun refreshAccessToken(refreshRequest: RefreshTokenRequest) = safeApiCall {
        api.refreshAccessToken(refreshRequest)
    }

    suspend fun saveAuthenticatedUser(authResponse: AuthResponse) {
        preferences.saveAuthToken(authResponse.accessToken)
        preferences.saveRoleUser(authResponse.role)
        preferences.saveStatusUser(authResponse.isVerified)
        preferences.saveFullName(authResponse.fullName)
    }

    suspend fun saveAuthToken(newToken: String) {
        preferences.saveAuthToken(newToken)
    }

    suspend fun updateStatusUser(verify: Boolean) {
        preferences.clearStatusUser(verify)
        preferences.saveStatusUser(verify)
    }

    suspend fun getLoginInfo(): LoginInfo {
        val email = preferences.email.first() ?: ""
        val password = preferences.password.first() ?: ""
        val remember = preferences.remember.first() ?: false
        return LoginInfo(email, password, remember)
    }

    suspend fun saveLoginInfo(email: String, password: String) {
        preferences.saveLoginInfo(email, password)
    }

    suspend fun clearLoginInfo() {
        preferences.clearLoginInfo()
    }
}