package com.university.marathononline.data.models

data class GuidedModeStats(
    val currentPace: Double = 0.0,
    val targetPace: Double = 0.0,
    val currentDistance: Double = 0.0,
    val targetDistance: Double = 0.0,
    val progressPercentage: Double = 0.0,
    val paceStatus: PaceStatus = PaceStatus.NOT_STARTED,
    val isOnTrack: Boolean = false
)

enum class PaceStatus {
    NOT_STARTED,
    ON_TARGET,
    TOO_SLOW,
    TOO_FAST
}