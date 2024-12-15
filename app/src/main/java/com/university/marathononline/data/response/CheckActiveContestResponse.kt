package com.university.marathononline.data.response

data class CheckActiveContestResponse(
    val exists: Boolean,
    val message: String? = null
)
