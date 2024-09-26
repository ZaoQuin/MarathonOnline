package com.university.marathononline.entity

data class User(
    val id: Long,
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val gender: String,
    val username: String,
    val password: String
)
