package com.university.marathononline.data.api.user

import com.university.marathononline.data.models.*
import com.university.marathononline.data.request.*
import com.university.marathononline.data.response.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface UserApiService {

    @POST("/api/v1/user")
    suspend fun createUser(@Body newUser: CreateUserRequest): User

    @PUT("/api/v1/user")
    suspend fun updateUser(@Body newUser: User): User

    @GET("/api/v1/user")
    suspend fun getUsers(): List<User>

    @GET("/api/v1/user/{id}")
    suspend fun getUser(@Path("id") id: Long): User

    @POST("/api/v1/user/check-email")
    suspend fun checkMail(@Body email: CheckEmailRequest): CheckEmailResponse

    @POST("/api/v1/user/check-username")
    suspend fun checkUsername(@Body username: CheckUsernameRequest): CheckUsernameResponse

    @POST("/api/v1/user/check-phoneNumber")
    suspend fun checkPhoneNumber(@Body phoneNumber: CheckPhoneNumberRequest): CheckPhoneNumberResponse

    @POST("/api/v1/user/update-password")
    suspend fun updatePassword(@Body email: UpdatePasswordRequest): UpdatePasswordResponse

    @Multipart
    @POST("/api/v1/user/{id}/avatar-mobile")
    suspend fun uploadAvatar(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part
    ): StringResponse
}