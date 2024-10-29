package com.university.marathononline.data.models

import java.util.Date

data class User(
    val id: Long,
    val fullName: String,
    val phoneNumber: String,
    val birthday: Date,
    val email: String,
    val gender: String,
    val username: String,
    val password: String
)
