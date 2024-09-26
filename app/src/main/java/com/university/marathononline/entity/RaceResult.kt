package com.university.marathononline.entity

import java.util.Date

data class RaceResult(
    val id: Long,
    val userId: Long,
    val eventId: Long,
    val distance: Float,
    val time: Long,
    val avgSpeed: Float,
    val date: Date
)
