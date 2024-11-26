package com.university.marathononline.data.models

import java.io.Serializable
import java.time.LocalDateTime

data class Race(
    var id: Long,
    var user: User,
    var distance: Double,
    var timeTaken: Long,
    var avgSpeed: Double,
    var timestamp: LocalDateTime
): Serializable
