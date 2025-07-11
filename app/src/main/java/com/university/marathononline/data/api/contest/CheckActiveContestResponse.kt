package com.university.marathononline.data.api.contest

data class CheckActiveContestResponse(
    val exists: Boolean,
    val message: String? = null
)
