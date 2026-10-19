package com.example.voice

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.example.data.VoicePersona
import com.example.data.VoiceSettings
import java.util.Locale

class TtsManager(
    private val context: Context,
    private val onSpeakingStateChanged: (Boolean) -> Unit
) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "TtsManager"
        private const val PREFS_NAME = "alic_tts_prefs"
        private const val KEY_PERSONA = "tts_persona"
        private const val KEY_PITCH = "tts_pitch"
        private const val KEY_RATE = "tts_rate"
        private const val KEY_MUTE = "tts_mute"
        private const val KEY_VOICE_NAME = "tts_voice_name"
        private const val UTTERANCE_ID = "ALIC_VOICE_UTTERANCE"
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var currentSettings = loadSettings()
        private set

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeakingStateChanged(true)
                }

                override fun onDone(utteranceId: String?) {
                    onSpeakingStateChanged(false)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onSpeakingStateChanged(false)
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.w(TAG, "TTS utterance error: $errorCode")
                    onSpeakingStateChanged(false)
                }
            })

            // Set language
            val localeResult = tts?.setLanguage(Locale.US)
            if (localeResult == TextToSpeech.LANG_MISSING_DATA || localeResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.getDefault()
            }

            applyVoiceSettings(currentSettings)
            Log.d(TAG, "TTS successfully initialized.")
        } else {
            Log.e(TAG, "TTS initialization failed with status: $status")
        }
    }

    fun speak(text: String, flush: Boolean = true) {
        if (!isInitialized || currentSettings.muteSpokenReplies) {
            return
        }

        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID)
        }

        tts?.speak(text, queueMode, params, UTTERANCE_ID)
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
            onSpeakingStateChanged(false)
        }
    }

    fun isSpeaking(): Boolean {
        return tts?.isSpeaking == true
    }

    fun updateSettings(newSettings: VoiceSettings) {
        currentSettings = newSettings
        saveSettings(newSettings)
        applyVoiceSettings(newSettings)
    }

    fun testVoice(sampleText: String = "Hello, I am Alic AI. My voice is calm, clear, and ready to assist you.") {
        if (!isInitialized) return
        stop()
        val tempMute = currentSettings.muteSpokenReplies
        if (tempMute) {
            // Unmute temporarily for the explicit test button
            applyVoiceSettings(currentSettings.copy(muteSpokenReplies = false))
        }
        speak(sampleText, flush = true)
    }

    /**
     * Finds voices matching the requested Persona (Neutral, Male, Female).
     */
    fun getAvailableVoices(): List<Voice> {
        if (!isInitialized) return emptyList()
        return try {
            tts?.voices?.toList()?.filter { voice ->
                !voice.isNetworkConnectionRequired &&
                        (voice.locale.language == Locale.getDefault().language || voice.locale.language == "en")
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Checks whether the device engine provides explicit male/female options
     */
    fun hasExplicitGenderVoices(): Boolean {
        val voices = getAvailableVoices()
        return voices.any { isFemaleVoice(it) } && voices.any { isMaleVoice(it) }
    }

    private fun isFemaleVoice(voice: Voice): Boolean {
        val name = voice.name.lowercase(Locale.ROOT)
        return name.contains("female") || name.contains("fem") || name.contains("-f-") ||
                voice.features?.any { it.contains("gender:female", ignoreCase = true) } == true
    }

    private fun isMaleVoice(voice: Voice): Boolean {
        val name = voice.name.lowercase(Locale.ROOT)
        return name.contains("male") || name.contains("-m-") ||
                voice.features?.any { it.contains("gender:male", ignoreCase = true) } == true
    }

    private fun applyVoiceSettings(settings: VoiceSettings) {
        if (!isInitialized) return

        tts?.setPitch(settings.pitch)
        tts?.setSpeechRate(settings.speechRate)

        val allVoices = getAvailableVoices()
        if (allVoices.isEmpty()) return

        val targetVoice = when (settings.persona) {
            VoicePersona.NEUTRAL -> {
                // Neutral: voice that isn't strongly tagged male/female, or first balanced voice
                allVoices.firstOrNull { !isFemaleVoice(it) && !isMaleVoice(it) }
                    ?: allVoices.firstOrNull()
            }
            VoicePersona.FEMALE -> {
                allVoices.firstOrNull { isFemaleVoice(it) } ?: allVoices.firstOrNull()
            }
            VoicePersona.MALE -> {
                allVoices.firstOrNull { isMaleVoice(it) } ?: allVoices.firstOrNull()
            }
        }

        if (targetVoice != null) {
            try {
                tts?.voice = targetVoice
            } catch (e: Exception) {
                Log.w(TAG, "Could not set custom voice: ${e.message}")
            }
        }
    }

    private fun loadSettings(): VoiceSettings {
        val personaName = prefs.getString(KEY_PERSONA, VoicePersona.NEUTRAL.name)
        val persona = runCatching { VoicePersona.valueOf(personaName ?: "NEUTRAL") }.getOrDefault(VoicePersona.NEUTRAL)
        val pitch = prefs.getFloat(KEY_PITCH, 1.0f)
        val rate = prefs.getFloat(KEY_RATE, 1.0f)
        val mute = prefs.getBoolean(KEY_MUTE, false)
        val voiceName = prefs.getString(KEY_VOICE_NAME, null)

        return VoiceSettings(
            persona = persona,
            pitch = pitch,
            speechRate = rate,
            muteSpokenReplies = mute,
            selectedVoiceName = voiceName
        )
    }

    private fun saveSettings(settings: VoiceSettings) {
        prefs.edit()
            .putString(KEY_PERSONA, settings.persona.name)
            .putFloat(KEY_PITCH, settings.pitch)
            .putFloat(KEY_RATE, settings.speechRate)
            .putBoolean(KEY_MUTE, settings.muteSpokenReplies)
            .putString(KEY_VOICE_NAME, settings.selectedVoiceName)
            .apply()
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS: ${e.message}")
        }
        tts = null
        isInitialized = false
    }
}
