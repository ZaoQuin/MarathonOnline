package com.university.marathononline.data.api.user

data class UpdatePasswordRequest(
    val email: String,
    val password: String
)