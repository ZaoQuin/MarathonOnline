package com.university.marathononline.data.models

import java.io.Serializable

data class Feedback (
    var id: Long,
    var sender: User,
    var message: String,
    var sentAt: String
): Serializable