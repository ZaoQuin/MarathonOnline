package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.user.UserApiService
import com.university.marathononline.data.request.CreateUserRequest

class UserRepository (
    private val api: UserApiService
): BaseRepository() {

    suspend fun createUser(newUser: CreateUserRequest) = safeApiCall {
        api.createUser(newUser)
    }

    suspend fun getUser(uid: Long) = safeApiCall {
        api.getUser(uid)
    }

    suspend fun getUsers() = safeApiCall {
        api.getUsers()
    }
}