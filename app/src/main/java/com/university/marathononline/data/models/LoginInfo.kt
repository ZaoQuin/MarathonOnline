package com.university.marathononline.data.models

import java.io.Serializable

data class LoginInfo(
    val email: String,
    val password: String,
    val remember: Boolean
): Serializable
