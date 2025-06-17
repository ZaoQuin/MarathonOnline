package com.university.marathononline.data.api.user

data class CheckEmailResponse(
    var exists: Boolean,
    var message: String
)
