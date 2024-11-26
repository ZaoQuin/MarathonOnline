package com.university.marathononline.data.models

import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

data class Contest(
    var id: Long? = null,
    var organizer: User? = null,
    var rules: List<Rule>? = null,
    var rewards: List<Reward>? = null,
    var registrations: List<Registration>? = null,
    var name: String? = null,
    var description: String? = null,
    var distance: Double? = null,
    var startDate: LocalDateTime? = null,
    var endDate: LocalDateTime? = null,
    var fee: BigDecimal? = null,
    var maxMembers: Int? = null,
    var status: EContestStatus? = null,
    var createDate: LocalDateTime? = null,
    var registrationDeadline: LocalDateTime? = null
): Serializable

enum class EContestStatus {
    PENDING, ONGOING, FINISHED, CANCELLED, NOT_APPROVAL, DELETED
}
