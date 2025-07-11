package com.university.marathononline.data.api.contest

import com.university.marathononline.data.models.EContestStatus
import com.university.marathononline.data.models.Reward
import com.university.marathononline.data.models.Rule
import java.math.BigDecimal

data class CreateContestRequest(
    var rules: List<Rule>? = null,
    var rewards: List<Reward>? = null,
    var name: String? = null,
    var description: String? = null,
    var distance: Double? = null,
    var startDate: String? = null,
    var endDate: String? = null,
    var fee: BigDecimal? = null,
    var maxMembers: Int? = null,
    var status: EContestStatus? = null,
    var registrationDeadline: String? = null
)
