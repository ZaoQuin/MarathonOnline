package com.university.marathononline.data.models

import java.io.Serializable

data class LoginInfo(
    var email: String,
    var password: String,
    var remember: Boolean
): Serializable
