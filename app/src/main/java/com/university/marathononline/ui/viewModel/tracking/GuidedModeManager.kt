package com.university.marathononline.ui.viewModel.tracking

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.university.marathononline.data.models.GuidedModeStats
import com.university.marathononline.data.models.PaceStatus
import com.university.marathononline.data.models.TrainingDay
import com.university.marathononline.utils.TrainingSessionManager
import com.university.marathononline.utils.VoiceGuidanceService

class GuidedModeManager(private val context: Context) {

    private val _guidedModeStats = MutableStateFlow(GuidedModeStats())
    val guidedModeStats: StateFlow<GuidedModeStats> = _guidedModeStats

    private var isGuidedMode: Boolean = false
    private var trainingSessionManager: TrainingSessionManager? = null
    private var currentTrainingDay: TrainingDay? = null

    fun setGuidedMode(enabled: Boolean, trainingDay: TrainingDay? = null) {
        isGuidedMode = enabled

        // Always clean up existing manager if present to avoid resource leaks
        trainingSessionManager?.clear()
        trainingSessionManager = null

        // Only initialize if enabling guided mode
        if (enabled) {
            val dayToUse = trainingDay ?: currentTrainingDay
            if (dayToUse != null) {
                Log.d("GuidedModeManager", "Initializing guided mode with training day")
                val voiceService = VoiceGuidanceService(context)
                trainingSessionManager = TrainingSessionManager(dayToUse.session, voiceService)
            } else {
                Log.d("GuidedModeManager", "Cannot initialize guided mode: no training day available")
            }
        }

        // Reset stats when mode changes
        _guidedModeStats.value = GuidedModeStats()
    }

    fun setTrainingDay(trainingDay: TrainingDay) {
        currentTrainingDay = trainingDay
        Log.d("GuidedModeManager", "Training day set: ${trainingDay.session.type} - pace: ${trainingDay.session.pace}")

        if (isGuidedMode) {
            // Reinitialize manager with new training day
            trainingSessionManager?.clear()
            trainingSessionManager = null

            Log.d("GuidedModeManager", "Reinitializing TrainingSessionManager with new training day")
            val voiceService = VoiceGuidanceService(context)
            trainingSessionManager = TrainingSessionManager(trainingDay.session, voiceService)
        }
    }

    fun updateStats(currentPace: Double, currentDistance: Double, elapsedTime: Long) {
        if (!isGuidedMode || currentTrainingDay == null) return

        val trainingDay = currentTrainingDay!!
        val targetPace = trainingDay.session.pace
        val targetDistance = trainingDay.session.distance

        val progress = if (targetDistance > 0) (currentDistance / targetDistance) * 100 else 0.0
        val paceDifference = currentPace - targetPace
        val toleranceThreshold = 0.5 // 30 seconds tolerance

        val paceStatus = when {
            currentPace == 0.0 -> PaceStatus.NOT_STARTED
            kotlin.math.abs(paceDifference) <= toleranceThreshold -> PaceStatus.ON_TARGET
            paceDifference > toleranceThreshold -> PaceStatus.TOO_SLOW
            else -> PaceStatus.TOO_FAST
        }

        val stats = GuidedModeStats(
            currentPace = currentPace,
            targetPace = targetPace,
            currentDistance = currentDistance,
            targetDistance = targetDistance,
            progressPercentage = progress.coerceAtMost(100.0),
            paceStatus = paceStatus,
            isOnTrack = paceStatus == PaceStatus.ON_TARGET
        )

        _guidedModeStats.value = stats

        // Update training session manager
        trainingSessionManager?.let { manager ->
            try {
                Log.d("GuidedModeManager", "Updating training session with pace: $currentPace min/km")
                manager.update(currentPace, currentDistance, elapsedTime)
            } catch (e: Exception) {
                Log.e("GuidedModeManager", "Error updating training session", e)
            }
        }
    }

    fun clear() {
        try {
            trainingSessionManager?.clear()
            trainingSessionManager = null
            _guidedModeStats.value = GuidedModeStats()
        } catch (e: Exception) {
            Log.e("GuidedModeManager", "Error clearing guided mode manager", e)
        }
    }

    fun isEnabled(): Boolean = isGuidedMode
}