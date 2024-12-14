package com.university.marathononline.data.response

data class CheckContestNameResponse(
    val exists: Boolean,
    val message: String? = null
)
