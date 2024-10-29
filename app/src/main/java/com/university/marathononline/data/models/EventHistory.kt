package com.university.marathononline.data.models

data class EventHistory(
    val id: Long,
    val userId: Long,
    val eventId: Long,
    val raceResults: List<RaceResult>
)
