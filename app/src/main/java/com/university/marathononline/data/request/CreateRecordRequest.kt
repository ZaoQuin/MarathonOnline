package com.university.marathononline.data.request

data class CreateRecordRequest(
    val steps: Int,
    val distance: Double,
    val timeTaken: Long,
    val avgSpeed: Double,
    val timestamp: String
)