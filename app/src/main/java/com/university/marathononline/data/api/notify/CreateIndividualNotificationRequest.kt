package com.university.marathononline.data.api.notify

import com.university.marathononline.data.models.ENotificationType
import com.university.marathononline.data.models.User

data class CreateIndividualNotificationRequest(
    var objectId: Long? = null,
    var title: String? = null,
    var content: String? = null,
    var type: ENotificationType? = null,
    var receiver: User? = null
)