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
import com.example.persona.PersonaId
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
        initEngine()
    }

    private fun initEngine() {
        val googleEngine = "com.google.android.tts"
        try {
            val dummy = TextToSpeech(context.applicationContext, null)
            val installedEngines = dummy.engines?.map { it.name } ?: emptyList()
            dummy.shutdown()

            if (installedEngines.contains(googleEngine)) {
                Log.i(TAG, "Initializing with official Google Speech Services engine ($googleEngine) for natural human voice")
                tts = TextToSpeech(context.applicationContext, this, googleEngine)
            } else {
                Log.i(TAG, "Google Speech Services not directly available, initializing with system default engine")
                tts = TextToSpeech(context.applicationContext, this)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to query TTS engines, fallback to default", e)
            tts = TextToSpeech(context.applicationContext, this)
        }
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

        // Clean text of markdown characters, code blocks, URLs, and bullet points before speaking for non-robotic audio
        val cleanSpeech = text
            .replace(Regex("```[\\s\\S]*?```"), " ")
            .replace(Regex("`[^`]*`"), " ")
            .replace(Regex("https?://\\S+"), " ")
            .replace(Regex("[*#_~>|\\[\\]()•]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleanSpeech.isBlank()) return

        // Detect if text contains Hindi / Devanagari script
        val isHindi = cleanSpeech.any { it.code in 0x0900..0x097F }
        runCatching {
            if (isHindi) {
                tts?.language = Locale("hi", "IN")
            } else {
                tts?.language = Locale.US
            }
            applyVoiceSettings(currentSettings, isHindi)
        }

        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID)
            // Enable high-quality network/neural synthesis to remove robotic tone
            putString(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS, "true")
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
        val mappedPersona = when (persona.id) {
            PersonaId.GEMINI -> VoicePersona.GEMINI_LIVE
            PersonaId.AOEDE -> VoicePersona.AOEDE
            PersonaId.CHARON -> VoicePersona.CHARON
            PersonaId.PUCK -> VoicePersona.PUCK
            PersonaId.FENRIR -> VoicePersona.FENRIR
            PersonaId.KORE -> VoicePersona.KORE
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

    fun testVoice(sampleText: String? = null) {
        if (!isInitialized) return
        stop()
        val tempMute = currentSettings.muteSpokenReplies
        if (tempMute) {
            applyVoiceSettings(currentSettings.copy(muteSpokenReplies = false))
        }
        val textToSpeak = sampleText ?: when (currentSettings.persona) {
            VoicePersona.CHARON -> "Hello! I am Charon, Google's deep male voice model. Confident, calm, and ready to assist you."
            VoicePersona.PUCK -> "Hey! I am Puck, Google's upbeat and energetic male voice model. Let's solve your doubts and have fun!"
            VoicePersona.FENRIR -> "Hello! I am Fenrir, Google's warm and grounded male voice model. Always here to guide you smoothly."
            VoicePersona.AOEDE -> "Hello! I am Aoede, Google's bright and clear voice model. Ready to explore ideas with you."
            VoicePersona.KORE -> "Hello! I am Kore, Google's soothing and gentle voice model. How can I assist you today?"
            VoicePersona.MALE -> "Hello! I am your male AI assistant. Systems are online and ready."
            VoicePersona.FEMALE -> "Hello! I am your female AI assistant. How can I help you today?"
            else -> "Hello! I am ALIC-AI, your smart personal assistant. Ready to help you learn, solve problems, and achieve your goals."
        }
        speak(textToSpeak, flush = true)
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

    fun isFemaleVoice(voice: Voice): Boolean {
        val name = voice.name.lowercase(Locale.ROOT)
        val features = voice.features?.map { it.lowercase(Locale.ROOT) } ?: emptyList()
        if (features.any { it.contains("gender:female") || it.contains("female") }) return true

        val femalePatterns = listOf(
            "female", "fem", "-f-", "#female", "_female", "woman", "girl",
            "sfg#female", "iog", "iof", "iob",
            "ena", "enb",
            "hia", "hic"
        )
        return femalePatterns.any { name.contains(it) }
    }

    fun isMaleVoice(voice: Voice): Boolean {
        val name = voice.name.lowercase(Locale.ROOT)
        val features = voice.features?.map { it.lowercase(Locale.ROOT) } ?: emptyList()
        if (features.any { it.contains("gender:male") || it.contains("male") }) return true

        val malePatterns = listOf(
            "male", "-m-", "#male", "_male", "boy", "man", "guy", "masculine",
            "iom", "iol", "tpd", // Google US English male voices
            "enc", "end", "ene", // Google Indian English male voices
            "en-gb-x-rjs", "en-gb-x-gda", // Google UK male voices
            "en-au-x-afh", // Google AU male voices
            "hid", "hie" // Google Hindi male voices
        )
        return malePatterns.any { name.contains(it) }
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
                ?: pool.firstOrNull { !isFemaleVoice(it) }
                ?: pool.firstOrNull()
            PersonaGender.NEUTRAL -> pool.firstOrNull { isNeuralOrHighQualityVoice(it) }
                ?: pool.firstOrNull()
        }

        if (target != null) {
            runCatching { tts?.voice = target }
        }
    }

    fun applyVoiceSettings(settings: VoiceSettings, isHindi: Boolean = false) {
        if (!isInitialized) return

        // Set distinctive pitch matching male vs female frequency characteristics
        val effectivePitch = when (settings.persona) {
            VoicePersona.CHARON -> 0.78f // Deep masculine tone
            VoicePersona.FENRIR -> 0.82f // Warm grounded male tone
            VoicePersona.PUCK -> 0.86f   // Playful energetic male tone
            VoicePersona.MALE -> 0.80f   // Distinctive male tone
            VoicePersona.AOEDE -> 1.04f  // Bright female tone
            VoicePersona.KORE -> 1.02f   // Gentle female tone
            VoicePersona.FEMALE -> 1.04f // Female tone
            else -> settings.pitch.coerceIn(0.7f, 1.4f)
        }
        tts?.setPitch(effectivePitch)
        tts?.setSpeechRate(settings.speechRate.coerceIn(0.6f, 1.6f))

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

        val langCode = if (isHindi) "hi" else "en"
        val langVoices = allVoices.filter { it.locale.language.startsWith(langCode, ignoreCase = true) }
        val pool = if (langVoices.isNotEmpty()) langVoices else allVoices

        // Prefer high quality / natural neural voices matching Google Voice models
        val targetVoice = when (settings.persona) {
            VoicePersona.GEMINI_LIVE, VoicePersona.NEUTRAL -> {
                pool.firstOrNull { isNeuralOrHighQualityVoice(it) && !isFemaleVoice(it) && !isMaleVoice(it) }
                    ?: pool.firstOrNull { isNeuralOrHighQualityVoice(it) }
                    ?: pool.firstOrNull()
            }
            VoicePersona.AOEDE, VoicePersona.FEMALE -> {
                pool.firstOrNull { isFemaleVoice(it) && isNeuralOrHighQualityVoice(it) }
                    ?: pool.firstOrNull { isFemaleVoice(it) }
                    ?: pool.firstOrNull()
            }
            VoicePersona.CHARON, VoicePersona.MALE -> {
                pool.firstOrNull { isMaleVoice(it) && isNeuralOrHighQualityVoice(it) }
                    ?: pool.firstOrNull { isMaleVoice(it) }
                    ?: pool.firstOrNull { !isFemaleVoice(it) }
                    ?: pool.firstOrNull()
            }
            VoicePersona.PUCK -> {
                pool.firstOrNull { isMaleVoice(it) && isNeuralOrHighQualityVoice(it) }
                    ?: pool.firstOrNull { isMaleVoice(it) }
                    ?: pool.firstOrNull { !isFemaleVoice(it) }
                    ?: pool.firstOrNull()
            }
            VoicePersona.FENRIR -> {
                pool.firstOrNull { isMaleVoice(it) && isNeuralOrHighQualityVoice(it) }
                    ?: pool.firstOrNull { isMaleVoice(it) }
                    ?: pool.firstOrNull { !isFemaleVoice(it) }
                    ?: pool.firstOrNull()
            }
            VoicePersona.KORE -> {
                pool.firstOrNull { isFemaleVoice(it) && isNeuralOrHighQualityVoice(it) }
                    ?: pool.firstOrNull { isFemaleVoice(it) }
                    ?: pool.firstOrNull()
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
