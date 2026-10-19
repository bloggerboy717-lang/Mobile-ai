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
import com.example.persona.Persona
import com.example.persona.PersonaGender
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
    var isInitialized = false
        private set
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

            // Set default natural language
            val localeResult = tts?.setLanguage(Locale.getDefault())
            if (localeResult == TextToSpeech.LANG_MISSING_DATA || localeResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.US
            }

            applyVoiceSettings(currentSettings)
            Log.d(TAG, "High-fidelity natural TTS initialized.")
        } else {
            Log.e(TAG, "TTS initialization failed with status: $status")
        }
    }

    /**
     * Speaks naturally, dynamically detecting Hindi vs English to select the best non-robotic pronunciation
     */
    fun speak(text: String, flush: Boolean = true) {
        if (!isInitialized || currentSettings.muteSpokenReplies) {
            return
        }

        // Clean text of markdown characters before speaking for non-robotic audio
        val cleanSpeech = text
            .replace(Regex("[*#_`~>|\\[\\]]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleanSpeech.isBlank()) return

        // Detect if text contains Hindi / Devanagari script
        val isHindi = cleanSpeech.any { it.code in 0x0900..0x097F }
        if (isHindi) {
            runCatching {
                tts?.language = Locale("hi", "IN")
                selectBestNaturalVoice(PersonaGender.NEUTRAL, "hi")
            }
        } else {
            runCatching {
                applyVoiceSettings(currentSettings)
            }
        }

        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID)
        }

        tts?.speak(cleanSpeech, queueMode, params, UTTERANCE_ID)
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

    fun applyPersonaVoice(persona: Persona) {
        val mappedPersona = when (persona.gender) {
            PersonaGender.FEMALE -> VoicePersona.FEMALE
            PersonaGender.MALE -> VoicePersona.MALE
            PersonaGender.NEUTRAL -> VoicePersona.NEUTRAL
        }
        val newSettings = currentSettings.copy(
            persona = mappedPersona,
            pitch = persona.defaultPitch,
            speechRate = persona.defaultSpeechRate
        )
        updateSettings(newSettings)
    }

    fun updateSettings(newSettings: VoiceSettings) {
        currentSettings = newSettings
        saveSettings(newSettings)
        applyVoiceSettings(newSettings)
    }

    fun testVoice(sampleText: String = "नमस्ते रोहित! मैं हूँ आपका Gemini AI असिस्टेंट। मेरी आवाज़ पूरी तरह नेचुरल और बिना किसी रोबोटिक टोन के है।") {
        if (!isInitialized) return
        stop()
        val tempMute = currentSettings.muteSpokenReplies
        if (tempMute) {
            applyVoiceSettings(currentSettings.copy(muteSpokenReplies = false))
        }
        speak(sampleText, flush = true)
    }

    /**
     * Prioritizes high quality neural/wavenet voices available on the device
     */
    fun getAvailableVoices(): List<Voice> {
        if (!isInitialized) return emptyList()
        return try {
            val voices = tts?.voices?.toList() ?: emptyList()
            // Sort by quality: highest quality first, prioritizing neural/network voices for realism
            voices.sortedWith(
                compareByDescending<Voice> { it.quality }
                    .thenByDescending { isNeuralOrHighQualityVoice(it) }
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun isNeuralOrHighQualityVoice(voice: Voice): Boolean {
        val name = voice.name.lowercase(Locale.ROOT)
        return name.contains("network") ||
                name.contains("neural") ||
                name.contains("wavenet") ||
                name.contains("premium") ||
                name.contains("natural") ||
                name.contains("enhanced") ||
                (voice.quality >= Voice.QUALITY_HIGH)
    }

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

    private fun selectBestNaturalVoice(gender: PersonaGender, lang: String = "") {
        val allVoices = getAvailableVoices()
        val filteredByLang = if (lang.isNotEmpty()) {
            allVoices.filter { it.locale.language.startsWith(lang, ignoreCase = true) }
        } else allVoices

        val pool = if (filteredByLang.isNotEmpty()) filteredByLang else allVoices

        val target = when (gender) {
            PersonaGender.FEMALE -> pool.firstOrNull { isFemaleVoice(it) && isNeuralOrHighQualityVoice(it) }
                ?: pool.firstOrNull { isFemaleVoice(it) }
                ?: pool.firstOrNull()
            PersonaGender.MALE -> pool.firstOrNull { isMaleVoice(it) && isNeuralOrHighQualityVoice(it) }
                ?: pool.firstOrNull { isMaleVoice(it) }
                ?: pool.firstOrNull()
            PersonaGender.NEUTRAL -> pool.firstOrNull { isNeuralOrHighQualityVoice(it) }
                ?: pool.firstOrNull()
        }

        if (target != null) {
            runCatching { tts?.voice = target }
        }
    }

    private fun applyVoiceSettings(settings: VoiceSettings) {
        if (!isInitialized) return

        tts?.setPitch(settings.pitch)
        tts?.setSpeechRate(settings.speechRate)

        val allVoices = getAvailableVoices()
        if (allVoices.isEmpty()) return

        // If user manually chose a voice by name:
        if (!settings.selectedVoiceName.isNullOrEmpty()) {
            val matching = allVoices.firstOrNull { it.name == settings.selectedVoiceName }
            if (matching != null) {
                runCatching { tts?.voice = matching }
                return
            }
        }

        // Prefer high quality / natural neural voices
        val targetVoice = when (settings.persona) {
            VoicePersona.NEUTRAL -> {
                allVoices.firstOrNull { isNeuralOrHighQualityVoice(it) && !isFemaleVoice(it) && !isMaleVoice(it) }
                    ?: allVoices.firstOrNull { isNeuralOrHighQualityVoice(it) }
                    ?: allVoices.firstOrNull()
            }
            VoicePersona.FEMALE -> {
                allVoices.firstOrNull { isFemaleVoice(it) && isNeuralOrHighQualityVoice(it) }
                    ?: allVoices.firstOrNull { isFemaleVoice(it) }
                    ?: allVoices.firstOrNull()
            }
            VoicePersona.MALE -> {
                allVoices.firstOrNull { isMaleVoice(it) && isNeuralOrHighQualityVoice(it) }
                    ?: allVoices.firstOrNull { isMaleVoice(it) }
                    ?: allVoices.firstOrNull()
            }
        }

        if (targetVoice != null) {
            runCatching { tts?.voice = targetVoice }
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
