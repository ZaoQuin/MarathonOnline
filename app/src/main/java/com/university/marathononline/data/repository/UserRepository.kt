package com.university.marathononline.data.repository

import com.university.marathononline.base.BaseRepository
import com.university.marathononline.data.api.user.UserApiService
import com.university.marathononline.data.models.User
import com.university.marathononline.data.api.user.CheckEmailRequest
import com.university.marathononline.data.api.user.CheckPhoneNumberRequest
import com.university.marathononline.data.api.user.CheckUsernameRequest
import com.university.marathononline.data.api.user.CreateUserRequest
import com.university.marathononline.data.api.user.UpdatePasswordRequest
import okhttp3.MultipartBody

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

    suspend fun uploadAvatar(id: Long, file: MultipartBody.Part)= safeApiCall  {
        api.uploadAvatar(id, file)
    }
}