package com.university.marathononline.data.models

import java.util.Date

data class Contest(
    val id: Long,
    val title: String,
    val startDate: Date,
    val endDate: Date,
    val desc: String,
    val distance: Double,
    val registrationFee: Double
)
