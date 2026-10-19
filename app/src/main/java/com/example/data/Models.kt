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
        description = "High intelligence, fastest response time, and low latency."
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
    FEMALE
}

data class VoiceSettings(
    val persona: VoicePersona = VoicePersona.NEUTRAL,
    val pitch: Float = 1.0f,
    val speechRate: Float = 1.0f,
    val muteSpokenReplies: Boolean = false,
    val selectedVoiceName: String? = null
)
