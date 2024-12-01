package com.university.marathononline.data.models

import java.io.Serializable

data class ContestHistory(
    val id: Long,
    val user: User,
    val contest: Contest,
    val raceResults: List<RaceResult>
): Serializable
