package com.university.marathononline.data.models

import java.time.LocalDateTime

data class Rule(
    var id: Long,
    var icon: String,
    var name: String,
    var description: String,
    var updateDate: LocalDateTime,
    var contestId: Long
)
