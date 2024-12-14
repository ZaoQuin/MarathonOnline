package com.university.marathononline.data.request

import com.university.marathononline.data.models.Contest
import com.university.marathononline.data.models.ENotificationType
import com.university.marathononline.data.models.User

data class CreateIndividualNotificationRequest(
    var contest: Contest? = null,
    var title: String? = null,
    var content: String? = null,
    var type: ENotificationType? = null,
    var receiver: User? = null
)