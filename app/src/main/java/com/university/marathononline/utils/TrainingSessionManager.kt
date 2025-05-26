package com.university.marathononline.utils

import android.util.Log
import com.university.marathononline.data.models.ETrainingSessionType
import com.university.marathononline.data.models.TrainingSession

class TrainingSessionManager(
    private val session: TrainingSession,
    val voiceGuidanceService: VoiceGuidanceService
) {
    companion object {
        private const val TAG = "TrainingSessionManager"
        private const val PACE_BUFFER_SIZE = 5

        private const val SPEED_WORK_INTERVAL = 25_000L
        private const val DEFAULT_INTERVAL = 55_000L
    }

    private var lastGuidanceTime: Long = System.currentTimeMillis()
    private var guidanceInterval: Long = when (session.type) {
        ETrainingSessionType.SPEED_WORK -> SPEED_WORK_INTERVAL
        else -> DEFAULT_INTERVAL
    }
    private val paceBuffer = mutableListOf<Double>()

    private var pendingGuidance: String? = null
    private var isProcessingGuidance = false
    private var retryAttempts = 0
    private val maxRetryAttempts = 3

    init {
        Log.i(TAG, "Initialized with session type: ${session.type}, target pace: ${session.pace}, distance: ${session.distance}")
        Log.d(TAG, "Guidance interval set to: ${guidanceInterval}ms")

        // Add callback for speech completion
        voiceGuidanceService.setOnSpeechCompletedListener {
            synchronized(this) {
                isProcessingGuidance = false
                // Check if there's pending guidance that needs to be delivered
                pendingGuidance?.let {
                    if (retryAttempts < maxRetryAttempts) {
                        retryAttempts++
                        Log.d(TAG, "Retrying pending guidance (attempt $retryAttempts): $it")
                        deliverGuidance(it)
                    } else {
                        Log.w(TAG, "Max retry attempts reached, discarding guidance: $it")
                        pendingGuidance = null
                        retryAttempts = 0
                    }
                }
            }
        }
    }

    fun update(currentPace: Double, currentDistance: Double, elapsedTime: Long) {
        Log.v(TAG, "update() called: pace=$currentPace, distance=$currentDistance, elapsedTime=$elapsedTime")

        // Add pace to buffer for smoothing
        synchronized(paceBuffer) {
            paceBuffer.add(currentPace)
            if (paceBuffer.size > PACE_BUFFER_SIZE) paceBuffer.removeAt(0) // Keep 5 most recent values
            val smoothedPace = paceBuffer.average()
            Log.d(TAG, "Smoothed pace: $smoothedPace (buffer size: ${paceBuffer.size})")

            // Check if it's time to provide guidance
            val currentTime = System.currentTimeMillis()
            val timeSinceLastGuidance = currentTime - lastGuidanceTime
            val isNearCompletion = currentDistance >= session.distance * 0.98

            // Only provide guidance if enough time has passed or nearing completion
            synchronized(this) {
                if ((timeSinceLastGuidance < guidanceInterval && !isNearCompletion) || isProcessingGuidance) {
                    Log.v(TAG, "Skipping guidance. Time since last: ${timeSinceLastGuidance}ms, interval: ${guidanceInterval}ms, isProcessing: $isProcessingGuidance")
                    return
                }

                // Reset retry counter for new guidance
                retryAttempts = 0

                // Mark that we're about to provide guidance and update last guidance time
                isProcessingGuidance = true
                lastGuidanceTime = currentTime

                Log.i(TAG, "Preparing guidance - Session type: ${session.type}, Pace: $smoothedPace vs ${session.pace}, Distance: $currentDistance/${session.distance}")

                val guidance = when (session.type) {
                    ETrainingSessionType.LONG_RUN -> {
                        generateLongRunGuidance(smoothedPace, currentDistance)
                    }
                    ETrainingSessionType.RECOVERY_RUN -> {
                        generateRecoveryRunGuidance(smoothedPace)
                    }
                    ETrainingSessionType.SPEED_WORK -> {
                        generateSpeedWorkGuidance(smoothedPace, elapsedTime)
                    }
                    ETrainingSessionType.REST -> {
                        Log.i(TAG, "REST day guidance provided")
                        "Hôm nay là ngày nghỉ. Hãy thư giãn!"
                    }
                }

                deliverGuidance(guidance)
            }
        }
    }

    private fun deliverGuidance(message: String) {
        synchronized(this) {
            if (voiceGuidanceService.isSpeaking()) {
                // If already speaking, save this guidance for later
                pendingGuidance = message
                Log.d(TAG, "Voice service speaking, setting pending guidance: $message")
            } else {
                pendingGuidance = null
                Log.i(TAG, "Delivering guidance: $message")
                voiceGuidanceService.speak(message)
            }
        }
    }

    private fun generateLongRunGuidance(smoothedPace: Double, currentDistance: Double): String {
        val paceTolerance = 0.3
        val distanceTolerance = session.distance * 0.02

        return if (currentDistance >= session.distance - distanceTolerance) {
            Log.i(TAG, "LONG_RUN completion detected. Distance: $currentDistance/${session.distance}")
            "Chúc mừng! Bạn đã hoàn thành quãng đường."
        } else {
            val paceDifference = smoothedPace - session.pace
            Log.d(TAG, "LONG_RUN pace difference: $paceDifference (tolerance: $paceTolerance)")

            when {
                paceDifference > paceTolerance -> {
                    Log.d(TAG, "LONG_RUN guidance: Too slow")
                    "Chạy nhanh hơn một chút để đạt mục tiêu!"
                }
                paceDifference < -paceTolerance -> {
                    Log.d(TAG, "LONG_RUN guidance: Too fast")
                    "Chậm lại để giữ sức!"
                }
                else -> {
                    Log.d(TAG, "LONG_RUN guidance: Good pace")
                    "Tốc độ tốt, cứ giữ nhịp này!"
                }
            }
        }
    }

    private fun generateRecoveryRunGuidance(smoothedPace: Double): String {
        val paceTolerance = 0.5
        val paceDifference = smoothedPace - session.pace

        Log.d(TAG, "RECOVERY_RUN pace difference: $paceDifference (tolerance: $paceTolerance)")

        return if (smoothedPace < session.pace - paceTolerance) {
            Log.d(TAG, "RECOVERY_RUN guidance: Too fast")
            "Chạy chậm lại, đây là bài chạy hồi phục!"
        } else {
            Log.d(TAG, "RECOVERY_RUN guidance: Good pace")
            "Giữ nhịp nhẹ nhàng, bạn đang làm tốt!"
        }
    }

    private fun generateSpeedWorkGuidance(smoothedPace: Double, elapsedTime: Long): String {
        val paceTolerance = 0.2
        val cycleTime = (elapsedTime / 1000) % 60

        Log.d(TAG, "SPEED_WORK cycle time: ${cycleTime}s, pace: $smoothedPace vs ${session.pace}")

        return if (cycleTime < 30) {
            // First 30 seconds: High intensity
            if (smoothedPace < session.pace - paceTolerance) {
                Log.d(TAG, "SPEED_WORK guidance (high intensity): Too slow")
                "Tăng tốc ngay, chạy nhanh hơn!"
            } else {
                Log.d(TAG, "SPEED_WORK guidance (high intensity): Good pace")
                "Tốc độ tốt, tiếp tục chạy nhanh!"
            }
        } else {
            // Last 30 seconds: Recovery
            Log.d(TAG, "SPEED_WORK guidance (recovery phase)")
            "Chạy chậm lại để hồi phục."
        }
    }

    fun clear() {
        Log.i(TAG, "Clearing session data and resources")
        synchronized(this) {
            paceBuffer.clear()
            lastGuidanceTime = System.currentTimeMillis()
            isProcessingGuidance = false
            pendingGuidance = null
            retryAttempts = 0
        }
        voiceGuidanceService.shutdown()
    }
}