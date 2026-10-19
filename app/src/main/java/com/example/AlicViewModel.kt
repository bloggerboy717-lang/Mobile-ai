package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agent.ActionStatus
import com.example.agent.ActionType
import com.example.agent.AgentAction
import com.example.agent.AgentActionEngine
import com.example.agent.TaskPlan
import com.example.data.ApiKeyStorage
import com.example.data.AssistantState
import com.example.data.ChatMessage
import com.example.data.ConnectionStatus
import com.example.data.GeminiApiService
import com.example.data.MessageSender
import com.example.data.VoiceSettings
import com.example.persona.ALL_PERSONAS
import com.example.persona.Persona
import com.example.persona.PersonaId
import com.example.vision.ScreenCaptureService
import com.example.voice.SpeechManager
import com.example.voice.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlicViewModel(application: Application) : AndroidViewModel(application) {

    private val apiKeyStorage = ApiKeyStorage(application)
    private val geminiApiService = GeminiApiService()
    private val actionEngine = AgentActionEngine(application)

    // Current Persona
    private val _currentPersona = MutableStateFlow(ALL_PERSONAS.first())
    val currentPersona: StateFlow<Persona> = _currentPersona.asStateFlow()

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

    // Navigation: 0=Home, 1=Chat, 2=Vision, 3=Apps, 4=Settings
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    private val _showSetupDialog = MutableStateFlow(!apiKeyStorage.hasApiKey())
    val showSetupDialog: StateFlow<Boolean> = _showSetupDialog.asStateFlow()

    // Agentic Actions & Task Engine
    private val _activeTaskAction = MutableStateFlow<AgentAction?>(null)
    val activeTaskAction: StateFlow<AgentAction?> = _activeTaskAction.asStateFlow()

    private val _pendingConfirmationAction = MutableStateFlow<AgentAction?>(null)
    val pendingConfirmationAction: StateFlow<AgentAction?> = _pendingConfirmationAction.asStateFlow()

    private val _actionAuditLog = MutableStateFlow<List<AgentAction>>(emptyList())
    val actionAuditLog: StateFlow<List<AgentAction>> = _actionAuditLog.asStateFlow()

    // Vision Mode
    private val _isVisionActive = MutableStateFlow(false)
    val isVisionActive: StateFlow<Boolean> = _isVisionActive.asStateFlow()

    private val _visionFrame = MutableStateFlow<Bitmap?>(null)
    val visionFrame: StateFlow<Bitmap?> = _visionFrame.asStateFlow()

    private val _isAnalyzingVision = MutableStateFlow(false)
    val isAnalyzingVision: StateFlow<Boolean> = _isAnalyzingVision.asStateFlow()

    private val _visionResult = MutableStateFlow<String?>(null)
    val visionResult: StateFlow<String?> = _visionResult.asStateFlow()

    // Chat History
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.ALIC,
                text = "Hello Rohit! I'm your AI agent. How can I help you today?"
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

        // Sync ScreenCapture frame listener
        ScreenCaptureService.onFrameCapturedListener = { bitmap ->
            _visionFrame.value = bitmap
        }

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

        if (apiKeyStorage.hasApiKey()) {
            testCurrentConnection()
        }
    }

    fun switchTab(index: Int) {
        _currentTab.value = index
    }

    fun switchPersona(persona: Persona) {
        _currentPersona.value = persona
        ttsManager.applyPersonaVoice(persona)
        _chatMessages.value = _chatMessages.value + ChatMessage(
            sender = MessageSender.ALIC,
            text = persona.defaultGreeting
        )
        ttsManager.speak(persona.defaultGreeting)
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
                // In progress
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

        val userMsg = ChatMessage(sender = MessageSender.USER, text = prompt)
        _chatMessages.value = _chatMessages.value + userMsg

        // Check if user request is an actionable phone intent
        val parsedAction = actionEngine.parseUserIntent(prompt)
        if (parsedAction != null) {
            if (parsedAction.type == ActionType.ANALYZE_SCREEN) {
                switchTab(2) // Switch to Vision
                analyzeCurrentScreen(prompt)
                return
            }

            if (parsedAction.requiresConfirmation) {
                _pendingConfirmationAction.value = parsedAction
                val askConfirmationText = "I prepared to ${parsedAction.title}. Do you want me to proceed?"
                _chatMessages.value = _chatMessages.value + ChatMessage(sender = MessageSender.ALIC, text = askConfirmationText)
                ttsManager.speak(askConfirmationText)
                return
            } else {
                executeAgentAction(parsedAction)
                return
            }
        }

        // Standard Gemini conversational reasoning with persona instructions
        _assistantState.value = AssistantState.THINKING

        viewModelScope.launch {
            val result = geminiApiService.generateAssistantResponse(
                apiKey = apiKey,
                modelId = _selectedModel.value,
                userPrompt = prompt,
                systemInstructions = _currentPersona.value.systemPromptInstructions,
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

    fun executeAgentAction(action: AgentAction) {
        _activeTaskAction.value = action
        val statusText = "Executing: ${action.title}..."
        _chatMessages.value = _chatMessages.value + ChatMessage(sender = MessageSender.ALIC, text = statusText)
        ttsManager.speak(statusText)

        viewModelScope.launch {
            val success = actionEngine.executeAction(action)
            _actionAuditLog.value = _actionAuditLog.value + action
            _activeTaskAction.value = null

            val resultSpeech = if (success) {
                "Done! ${action.resultMessage ?: "Action completed."}"
            } else {
                "Could not complete ${action.title}. ${action.resultMessage ?: ""}"
            }

            _chatMessages.value = _chatMessages.value + ChatMessage(
                sender = MessageSender.ALIC,
                text = resultSpeech,
                isError = !success
            )
            ttsManager.speak(resultSpeech)
        }
    }

    fun confirmPendingAction() {
        val action = _pendingConfirmationAction.value ?: return
        _pendingConfirmationAction.value = null
        executeAgentAction(action)
    }

    fun cancelPendingAction() {
        val action = _pendingConfirmationAction.value ?: return
        action.status = ActionStatus.CANCELLED
        _pendingConfirmationAction.value = null
        val cancelMsg = "Cancelled action: ${action.title}."
        _chatMessages.value = _chatMessages.value + ChatMessage(sender = MessageSender.ALIC, text = cancelMsg)
        ttsManager.speak(cancelMsg)
    }

    // Vision Screen Understanding
    fun startScreenVision(resultCode: Int, data: Intent) {
        val serviceIntent = Intent(getApplication(), ScreenCaptureService::class.java).apply {
            putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, resultCode)
            putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, data)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplication<Application>().startForegroundService(serviceIntent)
        } else {
            getApplication<Application>().startService(serviceIntent)
        }
        _isVisionActive.value = true
    }

    fun stopScreenVision() {
        val serviceIntent = Intent(getApplication(), ScreenCaptureService::class.java)
        getApplication<Application>().stopService(serviceIntent)
        _isVisionActive.value = false
        _visionFrame.value = null
    }

    fun analyzeCurrentScreen(query: String = "") {
        val frame = _visionFrame.value ?: ScreenCaptureService.latestCapturedFrame
        if (frame == null) {
            _errorMessage.value = "Screen Vision is not active. Please tap 'Start Vision' first."
            return
        }

        val apiKey = apiKeyStorage.getApiKey()
        if (apiKey.isNullOrBlank()) {
            _errorMessage.value = "Gemini API key is not configured."
            _showSetupDialog.value = true
            return
        }

        _isAnalyzingVision.value = true
        _assistantState.value = AssistantState.THINKING
        ttsManager.speak("Analyzing your screen...")

        viewModelScope.launch {
            val result = geminiApiService.analyzeScreenVision(
                apiKey = apiKey,
                modelId = _selectedModel.value,
                bitmap = frame,
                userQuery = query,
                systemInstructions = _currentPersona.value.systemPromptInstructions
            )

            _isAnalyzingVision.value = false

            result.onSuccess { explanation ->
                _visionResult.value = explanation
                _chatMessages.value = _chatMessages.value + ChatMessage(
                    sender = MessageSender.ALIC,
                    text = "Screen Analysis: $explanation"
                )
                _assistantState.value = AssistantState.SPEAKING
                ttsManager.speak(explanation)
            }.onFailure { err ->
                _assistantState.value = AssistantState.IDLE
                val errText = err.message ?: "Failed to analyze screen."
                _errorMessage.value = errText
                _visionResult.value = "Error: $errText"
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
                text = _currentPersona.value.defaultGreeting
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
        ttsManager.testVoice(_currentPersona.value.defaultGreeting)
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        ttsManager.shutdown()
        stopScreenVision()
    }
}
