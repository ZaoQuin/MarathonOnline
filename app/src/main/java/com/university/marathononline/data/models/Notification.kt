package com.university.marathononline.data.models

import java.io.Serializable
import java.time.LocalDateTime

data class Notification(
    var id: Long,
    var receiver: User,
    var contest: Contest,
    var title: String,
    var content: String,
    var createAt: LocalDateTime,
    var isRead: Boolean,
    var type: ENotificationType
): Serializable

enum class ENotificationType {
    REWARD, NEW_NOTIFICATION
}
