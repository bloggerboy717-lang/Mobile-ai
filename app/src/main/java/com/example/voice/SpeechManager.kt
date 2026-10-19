package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
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
    private var isListening = false

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening() {
        mainHandler.post {
            try {
                if (isListening) {
                    stopListening()
                }

                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    onError("Speech recognition is not available on this device.")
                    return@post
                }

                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createListener())
                    }
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }

                isListening = true
                onStateChanged(SpeechState.Ready)
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                isListening = false
                Log.e(TAG, "Error starting speech recognition: ${e.message}")
                onError("Failed to start speech recognition: ${e.message}")
            }
        }
    }

    fun stopListening() {
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
                onStateChanged(SpeechState.Listening)
            }

            override fun onRmsChanged(rmsdB: Float) {
                // rmsdB ranges typically from -2 to 10
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
                onStateChanged(SpeechState.Idle)
                val message = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please verify your microphone."
                    SpeechRecognizer.ERROR_CLIENT -> "Client speech recognition error."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required for voice input."
                    SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out during speech recognition."
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech was detected. Please try again."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Please try again."
                    SpeechRecognizer.ERROR_SERVER -> "Speech recognition server error."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out with no speech input."
                    else -> "Speech recognition error ($error)."
                }
                Log.w(TAG, "SpeechRecognizer error: $error ($message)")
                onError(message)
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                onStateChanged(SpeechState.Idle)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull()?.trim()
                if (!recognizedText.isNullOrEmpty()) {
                    onFinalResult(recognizedText)
                } else {
                    onError("Could not understand speech. Please try again.")
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
