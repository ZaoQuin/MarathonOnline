package com.university.marathononline.data.response

data class CheckUsernameResponse(
    val exists: Boolean,
    val message: String? = null
)
