package com.university.marathononline.data.api.contest

import com.university.marathononline.data.models.Contest

data class GetContestsResponse(
    val contests: List<Contest>
)
