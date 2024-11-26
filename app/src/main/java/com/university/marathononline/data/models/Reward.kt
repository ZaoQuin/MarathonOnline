package com.university.marathononline.data.models

data class Reward(
    var id: Long,
    var name: String,
    var description: String,
    var rewardRank: Int,
    var type: ERewardType,
    var isClaim: Boolean
)

enum class ERewardType {
    PHYSICAL, VIRTUAL
}
