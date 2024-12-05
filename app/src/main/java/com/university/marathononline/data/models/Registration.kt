package com.university.marathononline.data.models

import java.io.Serializable

data class Registration(
    var id: Long,
    var runner: User,
    var payment: Payment,
    var registrationDate: String,
    var completedDate: String,
    var status: ERegistrationStatus,
    var registrationRank: Int,
    var raceResults: List<Race>,
    var rewards: List<Reward>? = null
): Serializable

enum class ERegistrationStatus {
    PENDING,ACTIVE, COMPLETED
}

