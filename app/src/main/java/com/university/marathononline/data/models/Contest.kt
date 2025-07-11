package com.university.marathononline.data.models

import java.io.Serializable
import java.math.BigDecimal

data class Contest(
    var id: Long,
    var imgUrl: String? = null,
    var organizer: User,
    var rules: List<Rule>,
    var rewards: List<Reward>,
    var registrations: List<Registration>,
    var name: String,
    var description: String,
    var distance: Double,
    var startDate: String,
    var endDate: String,
    var fee: BigDecimal,
    var maxMembers: Int,
    var status: EContestStatus,
    var createDate: String,
    var registrationDeadline: String
): Serializable

enum class EContestStatus(val value: String){
    PENDING("Chờ duyệt"),
    ACTIVE("Đang hoạt động"),
    FINISHED("Đã kết thúc"),
    CANCELLED("Đã hủy"),
    NOT_APPROVAL("Không chấp nhận"),
    DELETED("Đã xóa"),
    COMPLETED("Hoàn thành")
}

