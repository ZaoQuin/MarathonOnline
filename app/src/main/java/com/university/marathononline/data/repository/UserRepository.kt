package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.user.UserApiService

class UserRepository (
    private val api: UserApiService
): BaseRepository() {

    suspend fun getUser(uid: Long) = safeApiCall {
        api.getUser(uid)
    }

    suspend fun getUsers() = safeApiCall {
        api.getUsers()
    }
}