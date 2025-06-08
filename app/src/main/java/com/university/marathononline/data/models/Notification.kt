package com.university.marathononline.data.models

import java.io.Serializable

data class Notification(
    var id: Long? = null,
    var receiver: User? = null,
    var objectId: Long? = null,
    var title: String? = null,
    var content: String? = null,
    var createAt: String? = null,
    var isRead: Boolean? = null,
    var type: ENotificationType? = null
): Serializable

enum class ENotificationType(val value: String) {
    REWARD("Giải thưởng"),
    NEW_CONTEST("Cuộc thi mới"),
    BLOCK_CONTEST("Bị chặn khỏi cuộc thi"),
    ACCEPT_CONTEST("Cuộc thi được duyệt"),
    NOT_APPROVAL_CONTEST("Cuộc thi không được duyệt"),
    REJECTED_RECORD("Phát hiện gian lận")
}
