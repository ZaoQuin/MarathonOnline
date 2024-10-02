package com.university.marathononline.entity

import java.util.Date

data class Notify(
    val id: Long,
    val title: String,
    val content: String,
    val timeStamp: Date
)
