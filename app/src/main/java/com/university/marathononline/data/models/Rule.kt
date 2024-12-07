package com.university.marathononline.data.models

import java.io.Serializable
import java.time.LocalDateTime

data class Rule(
    var id: Long,
    var icon: String,
    var name: String,
    var description: String,
    var updateDate: String
): Serializable
