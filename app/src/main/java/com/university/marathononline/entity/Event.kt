package com.university.marathononline.entity

import java.util.Date

data class Event(
    val id: Long,
    val title: String,
    val startDate: Date,
    val endDate: Date,
    val desc: String,
    val distance: Double,
    val registrationFee: Double
)
