package com.university.marathononline.data.models

import java.io.Serializable
import java.time.LocalDateTime

data class Notification(
    var id: Long? = null,
    var receiver: User? = null,
    var contest: Contest? = null,
    var title: String? = null,
    var content: String? = null,
    var createAt: String? = null,
    var isRead: Boolean? = null,
    var type: ENotificationType? = null
): Serializable

enum class ENotificationType {
    REWARD, NEW_NOTIFICATION
}
