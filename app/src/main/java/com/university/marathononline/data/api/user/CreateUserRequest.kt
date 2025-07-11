package com.university.marathononline.data.api.user

import com.university.marathononline.data.models.EGender
import com.university.marathononline.data.models.ERole

data class CreateUserRequest (
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val address: String,
    val gender: EGender,
    val birthday: String,
    val username: String,
    val password: String,
    val role: ERole,
    val isVerified: Boolean = false
)