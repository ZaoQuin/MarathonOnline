package com.university.marathononline.data.response

import com.university.marathononline.data.models.ERole
import com.university.marathononline.data.models.EUserStatus

data class AuthResponse(
    val fullName: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String,
    val role: ERole,
    val status: EUserStatus
)