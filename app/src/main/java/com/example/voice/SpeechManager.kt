package com.example.voice

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.data.VoiceLanguage
import java.util.Locale

class SpeechManager(
    private val context: Context,
    private val onStateChanged: (SpeechState) -> Unit,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onRmsChangedCallback: (Float) -> Unit,
    private val onError: (String) -> Unit
) {
    sealed interface SpeechState {
        object Idle : SpeechState
        object Ready : SpeechState
        object Listening : SpeechState
        object Processing : SpeechState
    }

    companion object {
        private const val TAG = "SpeechManager"
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    var isListening = false
        private set
    var shouldContinueListening = true
    private var currentLanguage: VoiceLanguage = VoiceLanguage.AUTO

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    private var isMuted = false

    private fun muteSystemSounds() {
        if (isMuted) return
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
            audioManager?.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)
            isMuted = true
        } catch (e: Exception) {
            Log.d(TAG, "Mute stream attempt: ${e.message}")
        }
    }

    private fun unmuteSystemSounds() {
        if (!isMuted) return
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0)
            audioManager?.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0)
            isMuted = false
        } catch (e: Exception) {
            Log.d(TAG, "Unmute stream attempt: ${e.message}")
        }
    }

    fun startListening(language: VoiceLanguage = currentLanguage) {
        currentLanguage = language
        shouldContinueListening = true
        muteSystemSounds()
        startListeningInternal()
    }

    private fun startListeningInternal() {
        mainHandler.post {
            try {
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    unmuteSystemSounds()
                    onError("Speech recognition is not available on this device.")
                    return@post
                }

                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createListener())
                    }
                }

                val primaryLocaleTag = when (currentLanguage) {
                    VoiceLanguage.AUTO -> "hi-IN"
                    VoiceLanguage.BHOJPURI -> "hi-IN"
                    else -> currentLanguage.localeTag
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, primaryLocaleTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "$primaryLocaleTag,hi-IN,en-IN,en-US")
                    putExtra(
                        "android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES",
                        arrayOf("hi-IN", "en-IN", "en-US", "bho-IN", "mr-IN", "te-IN", "ta-IN", "bn-IN", "gu-IN", "kn-IN", "pa-IN")
                    )
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                    // High Sensitivity for Whispers and Low Volume Speech
                    putExtra("android.speech.extra.DICTATION_MODE", true)
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
                    // Fast endpointing: reply within 1 second after user finishes speaking
                    putExtra("android.speech.extras.SPEECH_INPUT_MINIMUM_LENGTH_MILLIS", 400L)
                    putExtra("android.speech.extras.SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS", 750L)
                    putExtra("android.speech.extras.SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS", 600L)
                }

                // Ensure system sounds are muted so no 'trook/tunkud' beeps play
                muteSystemSounds()

                isListening = true
                onStateChanged(SpeechState.Ready)
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                isListening = false
                Log.e(TAG, "Error starting speech recognition: ${e.message}")
                if (shouldContinueListening) {
                    mainHandler.postDelayed({
                        if (shouldContinueListening) {
                            startListeningInternal()
                        }
                    }, 400L)
                } else {
                    unmuteSystemSounds()
                    onError("Failed to start speech recognition: ${e.message}")
                }
            }
        }
    }

    fun stopListening() {
        shouldContinueListening = false
        unmuteSystemSounds()
        mainHandler.post {
            if (isListening) {
                isListening = false
                try {
                    speechRecognizer?.stopListening()
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping recognition: ${e.message}")
                }
                onStateChanged(SpeechState.Idle)
            }
        }
    }

    fun cancel() {
        shouldContinueListening = false
        unmuteSystemSounds()
        mainHandler.post {
            isListening = false
            try {
                speechRecognizer?.cancel()
            } catch (e: Exception) {
                Log.e(TAG, "Error canceling recognition: ${e.message}")
            }
            onStateChanged(SpeechState.Idle)
        }
    }

    fun destroy() {
        shouldContinueListening = false
        unmuteSystemSounds()
        mainHandler.post {
            isListening = false
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.e(TAG, "Error destroying recognizer: ${e.message}")
            }
            speechRecognizer = null
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                onStateChanged(SpeechState.Listening)
            }

            override fun onBeginningOfSpeech() {
                isListening = true
                onStateChanged(SpeechState.Listening)
            }

            override fun onRmsChanged(rmsdB: Float) {
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                onRmsChangedCallback(normalized)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                onStateChanged(SpeechState.Processing)
            }

            override fun onError(error: Int) {
                isListening = false

                // If user activated continuous toggle, keep running endlessly until user toggles off
                if (shouldContinueListening && error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == SpeechRecognizer.ERROR_CLIENT) {
                        try {
                            speechRecognizer?.destroy()
                            speechRecognizer = null
                        } catch (e: Exception) {
                            Log.d(TAG, "Reset recognizer error: ${e.message}")
                        }
                    }
                    mainHandler.postDelayed({
                        if (shouldContinueListening) {
                            startListeningInternal()
                        }
                    }, 250L)
                    return
                }

                unmuteSystemSounds()
                onStateChanged(SpeechState.Idle)
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please verify your microphone."
                    SpeechRecognizer.ERROR_CLIENT -> "Speech recognition client error."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required for voice input."
                    SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out during speech recognition."
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech was detected."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy."
                    SpeechRecognizer.ERROR_SERVER -> "Speech recognition server error."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out."
                    else -> "Speech recognition error ($error)."
                }
                Log.w(TAG, "SpeechRecognizer error: $error ($message)")
                if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    onError(message)
                }
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                onStateChanged(SpeechState.Idle)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull()?.trim()
                if (!recognizedText.isNullOrEmpty()) {
                    onFinalResult(recognizedText)
                } else if (shouldContinueListening) {
                    // Smoothly restart if empty result
                    mainHandler.postDelayed({
                        if (shouldContinueListening) {
                            startListeningInternal()
                        }
                    }, 150L)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull()?.trim()
                if (!partial.isNullOrEmpty()) {
                    onPartialResult(partial)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
