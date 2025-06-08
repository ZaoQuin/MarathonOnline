package com.university.marathononline.data.models

import java.io.Serializable

data class User(
    val id: Long,
    var fullName: String,
    val email: String,
    val phoneNumber: String,
    val gender: EGender,
    val birthday: String,
    val address: String,
    val username: String,
    val role: ERole,
    val refreshToken: String,
    val status: EUserStatus,
    val avatarUrl: String
): Serializable

enum class EGender(val value: String) {
    MALE("Nam"), FEMALE("Nữ")
}

enum class ERole(val value: String){
    RUNNER("Vận dộnng viên"), ORGANIZER("Nhà tổ chức"), ADMIN("Quản trị")
}

enum class EUserStatus(val value: String) {
    PENDING("Chưa xác thực"), DELETED("Đã bị xóa"), PRIVATE("Bí mật"), PUBLIC("Công khai")
}
