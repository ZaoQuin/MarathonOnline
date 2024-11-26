package com.university.marathononline.data.models

import java.io.Serializable

data class Reward(
    var id: Long,
    var name: String,
    var description: String,
    var rewardRank: Int,
    var type: ERewardType,
    var isClaim: Boolean
): Serializable

enum class ERewardType {
    PHYSICAL, VIRTUAL
}
