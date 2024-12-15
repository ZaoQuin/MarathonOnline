package com.university.marathononline.data.request

import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.ENotificationType

data class CreateAllNotificationRequest(
    var contest: Contest? = null,
    var title: String? = null,
    var content: String? = null,
    var type: ENotificationType? = null
)