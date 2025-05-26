package com.university.marathononline.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class VoiceGuidanceService(context: Context) {
    private var tts: TextToSpeech? = null
    val context: Context = context
    private var onSpeechCompletedListener: (() -> Unit)? = null
    private val isInitialized = AtomicBoolean(false)
    private val isSpeaking = AtomicBoolean(false)
    private val TAG = "VoiceGuidanceService"
    private val speakQueue = Collections.synchronizedList(mutableListOf<String>())
    private val initLock = Object()

    init {
        Log.d(TAG, "Initializing VoiceGuidanceService")

        synchronized(initLock) {
            // Prevent multiple initializations
            if (isInitialized.get() && tts != null) {
                Log.d(TAG, "TTS already initialized, skipping initialization")
                return@synchronized
            }

            try {
                tts = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        synchronized(initLock) {
                            isInitialized.set(true)

                            // Set Vietnamese language
                            val result = tts?.setLanguage(Locale("vi", "VN"))

                            // Fallback to English if Vietnamese not supported
                            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                                Log.e(TAG, "Vietnamese language not supported, falling back to English")
                                tts?.setLanguage(Locale.US)
                            }

                            // Set utterance progress listener
                            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                                override fun onStart(utteranceId: String?) {
                                    Log.d(TAG, "Speech started: $utteranceId")
                                    isSpeaking.set(true)
                                }

                                override fun onDone(utteranceId: String?) {
                                    Log.d(TAG, "Speech completed: $utteranceId")
                                    isSpeaking.set(false)

                                    // Notify completion
                                    onSpeechCompletedListener?.invoke()

                                    // Process any queued speech
                                    synchronized(speakQueue) {
                                        if (speakQueue.isNotEmpty() && isInitialized.get() && tts != null) {
                                            val nextText = speakQueue.removeAt(0)
                                            speakWithParams(nextText)
                                        }
                                    }
                                }

                                override fun onError(utteranceId: String?) {
                                    Log.e(TAG, "TTS Error for utteranceId: $utteranceId")
                                    isSpeaking.set(false)
                                    onSpeechCompletedListener?.invoke()

                                    // Try to recover and continue with queue
                                    synchronized(speakQueue) {
                                        if (speakQueue.isNotEmpty() && isInitialized.get() && tts != null) {
                                            val nextText = speakQueue.removeAt(0)
                                            speakWithParams(nextText)
                                        }
                                    }
                                }
                            })

                            // Process any queued speech from initialization period
                            synchronized(speakQueue) {
                                if (speakQueue.isNotEmpty() && tts != null) {
                                    val text = speakQueue.removeAt(0)
                                    speakWithParams(text)
                                }
                            }

                            initLock.notifyAll()
                        }
                    } else {
                        Log.e(TAG, "TTS Initialization failed with status: $status")
                        synchronized(initLock) {
                            initLock.notifyAll()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during TTS initialization: ${e.message}", e)
                isInitialized.set(false)
                initLock.notifyAll()
            }
        }
    }

    fun speak(text: String) {
        if (text.isEmpty()) {
            Log.w(TAG, "Attempted to speak empty text, ignoring request")
            return
        }

        // Wait for initialization to complete with timeout
        if (!isInitialized.get()) {
            synchronized(initLock) {
                try {
                    Log.d(TAG, "Waiting for TTS initialization...")
                    initLock.wait(2000) // Wait up to 2 seconds
                } catch (e: InterruptedException) {
                    Log.e(TAG, "Interrupted while waiting for TTS initialization", e)
                }
            }
        }

        synchronized(speakQueue) {
            if (!isInitialized.get() || tts == null) {
                Log.d(TAG, "TTS not ready, adding to queue: $text")
                speakQueue.add(text)
                return
            }

            if (isSpeaking.get()) {
                Log.d(TAG, "Already speaking, adding to queue: $text")
                speakQueue.add(text)
                return
            }

            speakWithParams(text)
        }
    }

    private fun speakWithParams(text: String) {
        // Double-check TTS is available
        if (!isInitialized.get() || tts == null) {
            Log.e(TAG, "Cannot speak: TTS not initialized or null")
            return
        }

        Log.d(TAG, "Speaking: $text")
        val params = HashMap<String, String>()
        params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "messageId-${UUID.randomUUID()}"

        try {
            val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params)
            if (result == TextToSpeech.ERROR) {
                Log.e(TAG, "TTS speak() returned ERROR")
                isSpeaking.set(false)
                // Try to re-initialize if speak fails
                retryInitialization(text)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while speaking: ${e.message}", e)
            isSpeaking.set(false)
            // Try to re-initialize if exception occurs
            retryInitialization(text)
        }
    }

    private fun retryInitialization(pendingText: String) {
        Log.d(TAG, "Attempting to re-initialize TTS engine")
        synchronized(initLock) {
            // Clean up existing TTS instance
            try {
                tts?.shutdown()
            } catch (e: Exception) {
                Log.e(TAG, "Error shutting down failed TTS engine", e)
            }

            isInitialized.set(false)
            isSpeaking.set(false)
            tts = null

            // Add the pending text back to the queue
            synchronized(speakQueue) {
                speakQueue.add(0, pendingText)
            }

            // Re-initialize with a delay to allow system recovery
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                tts = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        Log.d(TAG, "TTS re-initialization successful")
                        synchronized(initLock) {
                            isInitialized.set(true)
                            tts?.setLanguage(Locale("vi", "VN")) ?: tts?.setLanguage(Locale.US)

                            // Set up the listener again (simplified for brevity)
                            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                                override fun onStart(utteranceId: String?) { isSpeaking.set(true) }
                                override fun onDone(utteranceId: String?) {
                                    isSpeaking.set(false)
                                    onSpeechCompletedListener?.invoke()
                                }
                                override fun onError(utteranceId: String?) {
                                    isSpeaking.set(false)
                                    onSpeechCompletedListener?.invoke()
                                }
                            })

                            // Try to process the queue
                            synchronized(speakQueue) {
                                if (speakQueue.isNotEmpty()) {
                                    val text = speakQueue.removeAt(0)
                                    speakWithParams(text)
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "TTS re-initialization failed")
                    }
                }
            }, 500) // Delay re-initialization
        }
    }

    fun setOnSpeechCompletedListener(listener: () -> Unit) {
        onSpeechCompletedListener = listener
    }

    fun shutdown() {
        Log.d(TAG, "Shutting down VoiceGuidanceService")
        synchronized(speakQueue) {
            speakQueue.clear()
        }

        synchronized(initLock) {
            try {
                // Only attempt to stop/shutdown if initialized and not already shut down
                if (isInitialized.get() && tts != null) {
                    Log.d(TAG, "Stopping TTS engine")
                    tts?.stop()
                    Log.d(TAG, "Shutting down TTS engine")
                    tts?.shutdown()
                } else {
                    Log.d(TAG, "TTS already shut down or not initialized, skipping shutdown")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during TTS shutdown: ${e.message}", e)
            } finally {
                isInitialized.set(false)
                isSpeaking.set(false)
                tts = null
            }
        }
    }

    fun isSpeaking(): Boolean {
        return isSpeaking.get()
    }
}