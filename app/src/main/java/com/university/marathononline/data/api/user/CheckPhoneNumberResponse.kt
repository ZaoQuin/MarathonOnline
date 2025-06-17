package com.university.marathononline.data.api.user

data class CheckPhoneNumberResponse (
    var exists: Boolean,
    var message: String? = null
)
