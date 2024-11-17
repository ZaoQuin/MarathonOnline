package com.university.marathononline.data.request

data class UpdatePasswordRequest(
    val email: String,
    val password: String
)