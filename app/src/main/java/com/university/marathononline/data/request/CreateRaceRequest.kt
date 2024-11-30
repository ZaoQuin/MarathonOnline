package com.university.marathononline.data.request

data class CreateRaceRequest(
    val steps: Int,
    val distance: Double,
    val timeTaken: Long,
    val avgSpeed: Double,
    val timestamp: String
)