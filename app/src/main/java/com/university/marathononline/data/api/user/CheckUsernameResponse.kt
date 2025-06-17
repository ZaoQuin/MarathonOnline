package com.university.marathononline.data.api.user

data class CheckUsernameResponse(
    val exists: Boolean,
    val message: String? = null
)
