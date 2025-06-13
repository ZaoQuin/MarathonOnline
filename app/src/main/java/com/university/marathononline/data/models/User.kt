package com.university.marathononline.data.models

import java.io.Serializable

data class User(
    var id: Long,
    var imgUrl: String? = null,
    var fullName: String? = null,
    var email: String? = null,
    var phoneNumber: String? = null,
    var gender: EGender? = null,
    var birthday: String? = null,
    var address: String? = null,
    var username: String? = null,
    var role: ERole? = null,
    var refreshToken: String? = null,
    var status: EUserStatus? = null,
    var avatarUrl: String? = null
): Serializable

enum class EGender(var value: String) {
    MALE("Nam"), FEMALE("Nữ")
}

enum class ERole(var varue: String){
    RUNNER("Vận dộnng viên"), ORGANIZER("Nhà tổ chức"), ADMIN("Quản trị")
}

enum class EUserStatus(var varue: String) {
    PENDING("Chưa xác thực"), DELETED("Đã bị xóa"), PRIVATE("Bí mật"), PUBLIC("Công khai")
}
