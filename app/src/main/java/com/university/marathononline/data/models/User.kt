package com.university.marathononline.data.models

import java.io.Serializable
import java.util.Date

data class User(
    val id: Long,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val gender: EGender,
    val birthday: Date,
    val address: String,
    val username: String,
    val role: ERole,
    val refreshToken: String,
    val isVerified: Boolean,
    val avatarUrl: String
): Serializable

enum class EGender(val value: String) {
    MALE("Nam"), FEMALE("Nữ")
}

enum class ERole(val value: String){
    RUNNER("Vận dộnng viên"), ORGANIZER("Nhà tổ chức"), ADMIN("Quản trị")
}
