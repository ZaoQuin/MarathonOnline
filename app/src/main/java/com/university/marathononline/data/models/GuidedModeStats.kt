package com.university.marathononline.data.models

data class GuidedModeStats(
    var currentPace: Double = 0.0,
    var targetPace: Double = 0.0,
    var currentDistance: Double = 0.0,
    var targetDistance: Double = 0.0,
    var progressPercentage: Double = 0.0,
    var paceStatus: PaceStatus = PaceStatus.NOT_STARTED,
    var isOnTrack: Boolean = false
)

enum class PaceStatus {
    NOT_STARTED,
    ON_TARGET,
    TOO_SLOW,
    TOO_FAST
}