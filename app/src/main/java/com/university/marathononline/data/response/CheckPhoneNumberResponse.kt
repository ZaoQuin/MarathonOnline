package com.university.marathononline.data.response

data class CheckPhoneNumberResponse (
    var exists: Boolean,
    var message: String? = null
)
