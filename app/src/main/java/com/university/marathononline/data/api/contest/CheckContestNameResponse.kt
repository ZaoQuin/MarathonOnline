package com.university.marathononline.data.api.contest

data class CheckContestNameResponse(
    val exists: Boolean,
    val message: String? = null
)
