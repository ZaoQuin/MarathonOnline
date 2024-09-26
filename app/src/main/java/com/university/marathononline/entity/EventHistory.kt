package com.university.marathononline.entity

data class EventHistory(
    val id: Long,
    val userId: Long,
    val eventId: Long,
    val raceResults: List<RaceResult>
)
