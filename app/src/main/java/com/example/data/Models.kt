package com.example.data

import java.util.UUID

enum class AssistantState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

enum class MessageSender {
    USER,
    ALIC
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

sealed interface ConnectionStatus {
    object Untested : ConnectionStatus
    object Testing : ConnectionStatus
    data class Connected(val model: String, val latencyMs: Long) : ConnectionStatus
    data class InvalidApiKey(val message: String) : ConnectionStatus
    data class RateLimit(val message: String) : ConnectionStatus
    data class NetworkError(val message: String) : ConnectionStatus
    data class GeneralError(val message: String) : ConnectionStatus

    fun displayText(): String = when (this) {
        is Connected -> "Connected ($model, ${latencyMs}ms)"
        is InvalidApiKey -> "Invalid API Key: $message"
        is RateLimit -> "Rate Limit: $message"
        is NetworkError -> "Network Error: $message"
        is GeneralError -> "Error: $message"
        is Testing -> "Testing connection..."
        is Untested -> "Untested"
    }
}

data class GeminiModelOption(
    val id: String,
    val displayName: String,
    val description: String
)

val AVAILABLE_GEMINI_MODELS = listOf(
    GeminiModelOption(
        id = "gemini-2.5-flash",
        displayName = "Gemini 2.5 Flash (Recommended)",
        description = "Ultra-fast neural model with low latency, ideal for voice conversations."
    ),
    GeminiModelOption(
        id = "gemini-2.5-pro",
        displayName = "Gemini 2.5 Pro",
        description = "Most capable neural model for complex reasoning and multimodal tasks."
    ),
    GeminiModelOption(
        id = "gemini-2.0-flash",
        displayName = "Gemini 2.0 Flash",
        description = "Next-gen multimodal performance with fast generation."
    ),
    GeminiModelOption(
        id = "gemini-1.5-flash",
        displayName = "Gemini 1.5 Flash",
        description = "Versatile speed and cost efficiency."
    )
)

enum class VoicePersona {
    NEUTRAL,
    MALE,
    FEMALE,
    GEMINI_LIVE,
    AOEDE,
    CHARON,
    PUCK,
    FENRIR,
    KORE
}

enum class VoiceLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val localeTag: String,
    val systemPromptDirective: String
) {
    AUTO(
        code = "auto",
        displayName = "Auto Detect",
        nativeName = "स्वचालित",
        localeTag = "en-IN",
        systemPromptDirective = "Detect the user's language automatically and reply fluently in that exact same language (Bhojpuri, Hindi, Marathi, Telugu, Tamil, Bengali, or English)."
    ),
    BHOJPURI(
        code = "bho",
        displayName = "Bhojpuri",
        nativeName = "भोजपुरी",
        localeTag = "hi-IN",
        systemPromptDirective = "You MUST reply strictly in authentic, sweet, and conversational BHOJPURI (भोजपुरी) using Devanagari script (e.g. 'हम रउवा मदद करे खातिर तइयार बानी। बताईं का परेशानी बा?'). Never switch to English unless code is requested."
    ),
    HINDI(
        code = "hi",
        displayName = "Hindi",
        nativeName = "हिंदी",
        localeTag = "hi-IN",
        systemPromptDirective = "You MUST reply strictly in natural, polite, and fluent HINDI (हिंदी) using clean Devanagari script. Never switch to English unless technical code is requested."
    ),
    ENGLISH(
        code = "en",
        displayName = "English",
        nativeName = "English",
        localeTag = "en-US",
        systemPromptDirective = "You MUST reply strictly in fluent, natural, conversational ENGLISH."
    ),
    MARATHI(
        code = "mr",
        displayName = "Marathi",
        nativeName = "मराठी",
        localeTag = "mr-IN",
        systemPromptDirective = "You MUST reply strictly in natural, fluent MARATHI (मराठी) using Devanagari script."
    ),
    TELUGU(
        code = "te",
        displayName = "Telugu",
        nativeName = "తెలుగు",
        localeTag = "te-IN",
        systemPromptDirective = "You MUST reply strictly in natural, fluent TELUGU (తెలుగు) using Telugu script."
    ),
    TAMIL(
        code = "ta",
        displayName = "Tamil",
        nativeName = "தமிழ்",
        localeTag = "ta-IN",
        systemPromptDirective = "You MUST reply strictly in natural, fluent TAMIL (தமிழ்) using Tamil script."
    ),
    BENGALI(
        code = "bn",
        displayName = "Bengali",
        nativeName = "বাংলা",
        localeTag = "bn-IN",
        systemPromptDirective = "You MUST reply strictly in natural, fluent BENGALI (বাংলা) using Bengali script."
    ),
    GUJARATI(
        code = "gu",
        displayName = "Gujarati",
        nativeName = "ગુજરાતી",
        localeTag = "gu-IN",
        systemPromptDirective = "You MUST reply strictly in natural, fluent GUJARATI (ગુજરાતી) using Gujarati script."
    ),
    KANNADA(
        code = "kn",
        displayName = "Kannada",
        nativeName = "ಕನ್ನಡ",
        localeTag = "kn-IN",
        systemPromptDirective = "You MUST reply strictly in natural, fluent KANNADA (ಕನ್ನಡ) using Kannada script."
    ),
    PUNJABI(
        code = "pa",
        displayName = "Punjabi",
        nativeName = "ਪੰਜਾਬੀ",
        localeTag = "pa-IN",
        systemPromptDirective = "You MUST reply strictly in natural, fluent PUNJABI (ਪੰਜਾਬੀ) using Gurmukhi script."
    )
}

data class VoiceSettings(
    val persona: VoicePersona = VoicePersona.NEUTRAL,
    val language: VoiceLanguage = VoiceLanguage.AUTO,
    val pitch: Float = 1.0f,
    val speechRate: Float = 1.0f,
    val muteSpokenReplies: Boolean = false,
    val selectedVoiceName: String? = null,
    val backgroundListeningEnabled: Boolean = false
)

data class GeminiVoiceResponse(
    val text: String,
    val audioBase64: String? = null,
    val mimeType: String = "audio/wav"
)
