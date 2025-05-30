package com.university.marathononline.data.models

import java.io.Serializable

data class Record(
    var id: Long,
    var user: User,
    var steps: Int,
    var distance: Double,
    var timeTaken: Long,
    var avgSpeed: Double,
    var timestamp: String,
    var heartRate: Double
): Serializable
