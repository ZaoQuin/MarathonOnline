package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.user.UserApiService
import com.university.marathononline.data.models.User
import com.university.marathononline.data.request.CheckEmailRequest
import com.university.marathononline.data.request.CheckPhoneNumberRequest
import com.university.marathononline.data.request.CheckUsernameRequest
import com.university.marathononline.data.request.CreateUserRequest
import com.university.marathononline.data.request.UpdatePasswordRequest

class UserRepository (
    private val api: UserApiService
): BaseRepository() {

    suspend fun createUser(newUser: CreateUserRequest) = safeApiCall {
        api.createUser(newUser)
    }

    suspend fun updateUser(user: User) = safeApiCall {
        api.updateUser(user)
    }

    suspend fun getUser(uid: Long) = safeApiCall {
        api.getUser(uid)
    }

    suspend fun getUsers() = safeApiCall {
        api.getUsers()
    }

    suspend fun checkEmail(email: CheckEmailRequest) = safeApiCall {
        api.checkMail(email)
    }

    suspend fun checkUsername(username: CheckUsernameRequest) = safeApiCall {
        api.checkUsername(username)
    }

    suspend fun checkPhoneNumber(phoneNumber: CheckPhoneNumberRequest) = safeApiCall {
        api.checkPhoneNumber(phoneNumber)
    }

    suspend fun updatePassword(updateRequest: UpdatePasswordRequest) = safeApiCall {
        api.updatePassword(updateRequest)
    }
}