package com.university.marathononline.data.response

import com.university.marathononline.data.models.ERole

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val role: ERole,
    val isVerified: Boolean
)