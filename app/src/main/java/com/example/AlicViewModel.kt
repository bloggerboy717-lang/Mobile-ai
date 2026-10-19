package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ApiKeyStorage
import com.example.data.AssistantState
import com.example.data.ChatMessage
import com.example.data.ConnectionStatus
import com.example.data.GeminiApiService
import com.example.data.MessageSender
import com.example.data.VoiceSettings
import com.example.voice.SpeechManager
import com.example.voice.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlicViewModel(application: Application) : AndroidViewModel(application) {

    private val apiKeyStorage = ApiKeyStorage(application)
    private val geminiApiService = GeminiApiService()

    // Assistant States
    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Untested)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _hasConfiguredKey = MutableStateFlow(apiKeyStorage.hasApiKey())
    val hasConfiguredKey: StateFlow<Boolean> = _hasConfiguredKey.asStateFlow()

    private val _maskedApiKey = MutableStateFlow(apiKeyStorage.getMaskedApiKey())
    val maskedApiKey: StateFlow<String> = _maskedApiKey.asStateFlow()

    private val _selectedModel = MutableStateFlow(apiKeyStorage.getSelectedModel())
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _recognizedLiveText = MutableStateFlow("")
    val recognizedLiveText: StateFlow<String> = _recognizedLiveText.asStateFlow()

    private val _activeAudioRms = MutableStateFlow(0f)
    val activeAudioRms: StateFlow<Float> = _activeAudioRms.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _showSetupDialog = MutableStateFlow(!apiKeyStorage.hasApiKey())
    val showSetupDialog: StateFlow<Boolean> = _showSetupDialog.asStateFlow()

    // Chat History
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.ALIC,
                text = "Hello! How can I help you? I am Alic AI, your voice assistant developed by Rohit Raj."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // TTS & Voice
    lateinit var ttsManager: TtsManager
        private set

    private val _voiceSettings = MutableStateFlow(VoiceSettings())
    val voiceSettings: StateFlow<VoiceSettings> = _voiceSettings.asStateFlow()

    private val _hasExplicitGenderVoices = MutableStateFlow(false)
    val hasExplicitGenderVoices: StateFlow<Boolean> = _hasExplicitGenderVoices.asStateFlow()

    // Speech Recognition
    private val speechManager: SpeechManager

    init {
        ttsManager = TtsManager(application) { isSpeaking ->
            if (isSpeaking) {
                _assistantState.value = AssistantState.SPEAKING
            } else if (_assistantState.value == AssistantState.SPEAKING) {
                _assistantState.value = AssistantState.IDLE
            }
        }
        _voiceSettings.value = ttsManager.currentSettings
        _hasExplicitGenderVoices.value = ttsManager.hasExplicitGenderVoices()

        speechManager = SpeechManager(
            context = application,
            onStateChanged = { state ->
                when (state) {
                    SpeechManager.SpeechState.Idle -> {
                        if (_assistantState.value == AssistantState.LISTENING) {
                            _assistantState.value = AssistantState.IDLE
                        }
                    }
                    SpeechManager.SpeechState.Ready,
                    SpeechManager.SpeechState.Listening -> {
                        _assistantState.value = AssistantState.LISTENING
                    }
                    SpeechManager.SpeechState.Processing -> {
                        _assistantState.value = AssistantState.THINKING
                    }
                }
            },
            onPartialResult = { partial ->
                _recognizedLiveText.value = partial
            },
            onFinalResult = { finalResult ->
                _recognizedLiveText.value = finalResult
                processUserPrompt(finalResult)
            },
            onRmsChangedCallback = { rms ->
                _activeAudioRms.value = rms
            },
            onError = { err ->
                _errorMessage.value = err
                _assistantState.value = AssistantState.IDLE
            }
        )

        // If key is configured, perform quick background test if desired
        if (apiKeyStorage.hasApiKey()) {
            testCurrentConnection()
        }
    }

    fun switchTab(index: Int) {
        _currentTab.value = index
    }

    fun dismissSetupDialog() {
        _showSetupDialog.value = false
    }

    fun openSetupDialog() {
        _showSetupDialog.value = true
    }

    fun onMicClicked() {
        dismissError()
        when (_assistantState.value) {
            AssistantState.LISTENING -> {
                speechManager.stopListening()
                _assistantState.value = AssistantState.IDLE
            }
            AssistantState.SPEAKING -> {
                stopSpeaking()
            }
            AssistantState.THINKING -> {
                // Currently processing
            }
            AssistantState.IDLE -> {
                if (!apiKeyStorage.hasApiKey()) {
                    _errorMessage.value = "Please configure your Gemini API Key first."
                    _showSetupDialog.value = true
                    return
                }
                stopSpeaking()
                _recognizedLiveText.value = ""
                speechManager.startListening()
            }
        }
    }

    fun cancelListening() {
        speechManager.cancel()
        _assistantState.value = AssistantState.IDLE
    }

    fun submitTypedPrompt(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) return

        if (!apiKeyStorage.hasApiKey()) {
            _errorMessage.value = "Please configure your Gemini API Key first."
            _showSetupDialog.value = true
            return
        }

        stopSpeaking()
        speechManager.cancel()
        processUserPrompt(trimmed)
    }

    private fun processUserPrompt(prompt: String) {
        val apiKey = apiKeyStorage.getApiKey()
        if (apiKey.isNullOrBlank()) {
            _errorMessage.value = "Gemini API key is not configured."
            _showSetupDialog.value = true
            return
        }

        // Add user message to conversation
        val userMsg = ChatMessage(sender = MessageSender.USER, text = prompt)
        _chatMessages.value = _chatMessages.value + userMsg

        _assistantState.value = AssistantState.THINKING

        viewModelScope.launch {
            val result = geminiApiService.generateAssistantResponse(
                apiKey = apiKey,
                modelId = _selectedModel.value,
                userPrompt = prompt,
                recentHistory = _chatMessages.value
            )

            result.onSuccess { reply ->
                val alicMsg = ChatMessage(sender = MessageSender.ALIC, text = reply)
                _chatMessages.value = _chatMessages.value + alicMsg
                _assistantState.value = AssistantState.SPEAKING
                ttsManager.speak(reply)
            }.onFailure { error ->
                _assistantState.value = AssistantState.IDLE
                val errorMsg = error.message ?: "Failed to generate response."
                _errorMessage.value = errorMsg
                val errorChatMsg = ChatMessage(
                    sender = MessageSender.ALIC,
                    text = "Sorry, I ran into an issue: $errorMsg",
                    isError = true
                )
                _chatMessages.value = _chatMessages.value + errorChatMsg
            }
        }
    }

    fun stopSpeaking() {
        ttsManager.stop()
        if (_assistantState.value == AssistantState.SPEAKING) {
            _assistantState.value = AssistantState.IDLE
        }
    }

    fun repeatMessage(message: ChatMessage) {
        if (message.sender == MessageSender.ALIC && !message.isError) {
            stopSpeaking()
            ttsManager.speak(message.text)
        }
    }

    fun clearConversation() {
        stopSpeaking()
        _chatMessages.value = listOf(
            ChatMessage(
                sender = MessageSender.ALIC,
                text = "Conversation cleared. Hello! How can I help you today?"
            )
        )
        _recognizedLiveText.value = ""
    }

    fun saveApiKey(rawKey: String, autoTest: Boolean = true): Boolean {
        val success = apiKeyStorage.saveApiKey(rawKey)
        if (success) {
            _hasConfiguredKey.value = true
            _maskedApiKey.value = apiKeyStorage.getMaskedApiKey()
            _showSetupDialog.value = false
            if (autoTest) {
                testCurrentConnection()
            }
        } else {
            _errorMessage.value = "Failed to encrypt and save API key."
        }
        return success
    }

    fun deleteApiKey() {
        apiKeyStorage.deleteApiKey()
        _hasConfiguredKey.value = false
        _maskedApiKey.value = "No key configured"
        _connectionStatus.value = ConnectionStatus.Untested
    }

    fun testCurrentConnection() {
        val key = apiKeyStorage.getApiKey()
        if (key.isNullOrBlank()) {
            _connectionStatus.value = ConnectionStatus.InvalidApiKey("No API key configured.")
            return
        }

        _connectionStatus.value = ConnectionStatus.Testing
        viewModelScope.launch {
            val status = geminiApiService.testConnection(
                apiKey = key,
                modelId = _selectedModel.value
            )
            _connectionStatus.value = status
            if (status is ConnectionStatus.InvalidApiKey) {
                _errorMessage.value = status.message
            }
        }
    }

    fun selectModel(modelId: String) {
        _selectedModel.value = modelId
        apiKeyStorage.setSelectedModel(modelId)
        if (apiKeyStorage.hasApiKey()) {
            testCurrentConnection()
        }
    }

    fun updateVoiceSettings(newSettings: VoiceSettings) {
        _voiceSettings.value = newSettings
        ttsManager.updateSettings(newSettings)
    }

    fun testVoice() {
        ttsManager.testVoice()
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        ttsManager.shutdown()
    }
}
