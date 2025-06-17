package com.university.marathononline.data.api.contest

import com.university.marathononline.data.models.ENotificationType

data class CreateAllNotificationRequest(
    var objectId: Long? = null,
    var title: String? = null,
    var content: String? = null,
    var type: ENotificationType? = null
)