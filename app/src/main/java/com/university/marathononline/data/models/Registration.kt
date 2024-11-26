package com.university.marathononline.data.models

import java.time.LocalDateTime

data class Registration(
    var id: Long,
    var runner: User,
    var payment: Payment,
    var registrationDate: LocalDateTime,
    var completedDate: LocalDateTime,
    var registrationRank: Int,
    var raceResults: List<Race>,
    var rewards: List<Reward>? = null,
    var status: ERegistrationStatus? = null
)

enum class ERegistrationStatus {
    PENDING, COMPLETED, REJECTED, CANCELLED
}

