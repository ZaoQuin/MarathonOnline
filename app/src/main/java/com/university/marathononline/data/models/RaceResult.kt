package com.university.marathononline.data.models

import java.io.Serializable
import java.util.Date

data class RaceResult(
    val id: Long,
    val userId: Long,
    val eventId: Long,
    val distance: Float,
    val time: Long,
    val avgSpeed: Float,
    val date: Date
): Serializable
