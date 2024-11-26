package com.university.marathononline.data.response

import com.university.marathononline.data.models.ERole

data class AuthResponse(
    val fullName: String,
    val accessToken: String,
    val refreshToken: String,
    val role: ERole,
    val isVerified: Boolean,
    val isDeleted: Boolean
)