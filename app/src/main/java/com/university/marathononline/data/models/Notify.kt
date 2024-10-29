package com.university.marathononline.data.models

import java.util.Date

data class Notify(
    val id: Long,
    val title: String,
    val content: String,
    val timeStamp: Date
)
