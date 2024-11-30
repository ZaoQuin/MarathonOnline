package com.university.marathononline.data.models

import java.io.Serializable

data class Registration(
    var id: Long,
    var runner: User,
    var payment: Payment,
    var registrationDate: String,
    var completedDate: String,
    var registrationRank: Int,
    var raceResults: List<Race>,
    var rewards: List<Reward>? = null,
    var status: ERegistrationStatus? = null
): Serializable

enum class ERegistrationStatus {
    PENDING, COMPLETED, REJECTED, CANCELLED
}

