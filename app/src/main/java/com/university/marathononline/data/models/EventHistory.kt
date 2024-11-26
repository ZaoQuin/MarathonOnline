package com.university.marathononline.data.models

import java.io.Serializable

data class EventHistory(
    val id: Long,
    val userId: Long,
    val eventId: Long,
    val raceResults: List<RaceResult>
): Serializable
