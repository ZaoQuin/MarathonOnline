package com.university.marathononline.data.models

import java.io.Serializable

data class Reward(
    var id: Long,
    var name: String,
    var description: String,
    var rewardRank: Int,
    var type: ERewardType
): Serializable

enum class ERewardType(value: String) {
    PHYSICAL("Vật lý"), VIRTUAL("Ảo")
}
