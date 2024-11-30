package com.university.marathononline.data.response

import com.university.marathononline.data.models.Contest

data class GetContestsResponse(
    val contests: List<Contest>
)
