package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agent.ActionStatus
import com.example.agent.ActionType
import com.example.agent.AgentAction
import com.example.agent.AgentActionEngine
import com.example.codebuilder.CodeBuildStep
import com.example.codebuilder.CodeBuilderEngine
import com.example.codebuilder.GeneratedProject
import com.example.data.ApiKeyStorage
import com.example.data.AssistantState
import com.example.data.ChatMessage
import com.example.data.ConnectionStatus
import com.example.data.EmergencyContact
import com.example.data.EmergencyStorage
import com.example.data.GeminiApiService
import com.example.data.MessageSender
import com.example.data.StudentProfile
import com.example.data.StudentProfileStorage
import com.example.data.ThemePreset
import com.example.data.ThemeStorage
import com.example.data.VoiceSettings
import com.example.ui.theme.ThemeColors
import com.example.ui.theme.resolveThemeColors
import com.example.persona.ALL_PERSONAS
import com.example.persona.Persona
import com.example.service.AlicAccessibilityService
import com.example.sos.EmergencySosManager
import com.example.sos.SosState
import com.example.vision.ScreenCaptureService
import com.example.voice.GeminiAudioPlayer
import com.example.voice.SpeechManager
import com.example.voice.TtsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlicViewModel(application: Application) : AndroidViewModel(application) {

    private val apiKeyStorage = ApiKeyStorage(application)
    private val geminiApiService = GeminiApiService()
    private val actionEngine = AgentActionEngine(application)

    // Current Persona (Default: Gemini)
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

    // Continuous Conversation Loop Mode (Enabled by default so users don't have to keep pressing mic)
    private val _isContinuousMode = MutableStateFlow(true)
    val isContinuousMode: StateFlow<Boolean> = _isContinuousMode.asStateFlow()
    private var userCancelledLoop = false

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
                text = ALL_PERSONAS.first().defaultGreeting
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Student Profile & Personal Memory
    val studentProfileStorage = StudentProfileStorage(application)
    private val _studentProfile = MutableStateFlow(studentProfileStorage.getProfile())
    val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

    fun updateStudentProfile(profile: StudentProfile) {
        studentProfileStorage.saveProfile(profile)
        _studentProfile.value = profile
    }

    // Dynamic App Theme & ORB Configuration
    val themeStorage = ThemeStorage(application)
    private val _currentThemePreset = MutableStateFlow(themeStorage.getThemePreset())
    val currentThemePreset: StateFlow<ThemePreset> = _currentThemePreset.asStateFlow()

    private val _customHexColor = MutableStateFlow(themeStorage.getCustomHex())
    val customHexColor: StateFlow<String> = _customHexColor.asStateFlow()

    private val _themeColors = MutableStateFlow(
        resolveThemeColors(themeStorage.getThemePreset(), themeStorage.getCustomHex())
    )
    val themeColors: StateFlow<ThemeColors> = _themeColors.asStateFlow()

    fun selectThemePreset(preset: ThemePreset) {
        themeStorage.saveThemePreset(preset)
        _currentThemePreset.value = preset
        _themeColors.value = resolveThemeColors(preset, _customHexColor.value)
    }

    fun setCustomThemeHex(hex: String) {
        themeStorage.saveCustomHex(hex)
        themeStorage.saveThemePreset(ThemePreset.CUSTOM)
        _customHexColor.value = hex
        _currentThemePreset.value = ThemePreset.CUSTOM
        _themeColors.value = resolveThemeColors(ThemePreset.CUSTOM, hex)
    }

    // SOS Emergency Alert System (Feature 15)
    val emergencyStorage = EmergencyStorage(application)
    lateinit var sosManager: EmergencySosManager
        private set

    val sosState: StateFlow<SosState> get() = sosManager.sosState

    fun triggerSos() {
        sosManager.triggerSos()
    }

    fun cancelSos() {
        sosManager.cancelSos()
    }

    fun dialPolice112() {
        sosManager.dialPolice112()
    }

    // Code Builder Engine (Feature 16)
    val codeBuilderEngine = CodeBuilderEngine(application)
    private val _isBuildingCode = MutableStateFlow(false)
    val isBuildingCode: StateFlow<Boolean> = _isBuildingCode.asStateFlow()

    private val _codeBuildStep = MutableStateFlow(CodeBuildStep.IDLE)
    val codeBuildStep: StateFlow<CodeBuildStep> = _codeBuildStep.asStateFlow()

    private val _codeBuildStatusText = MutableStateFlow("")
    val codeBuildStatusText: StateFlow<String> = _codeBuildStatusText.asStateFlow()

    private val _activeGeneratedProject = MutableStateFlow<GeneratedProject?>(null)
    val activeGeneratedProject: StateFlow<GeneratedProject?> = _activeGeneratedProject.asStateFlow()

    fun dismissProjectPreview() {
        _activeGeneratedProject.value = null
    }

    // TTS & Voice
    lateinit var ttsManager: TtsManager
        private set

    lateinit var geminiAudioPlayer: GeminiAudioPlayer
        private set

    private val _voiceSettings = MutableStateFlow(VoiceSettings())
    val voiceSettings: StateFlow<VoiceSettings> = _voiceSettings.asStateFlow()

    private val _hasExplicitGenderVoices = MutableStateFlow(false)
    val hasExplicitGenderVoices: StateFlow<Boolean> = _hasExplicitGenderVoices.asStateFlow()

    // Speech Recognition
    private val speechManager: SpeechManager

    init {
        geminiAudioPlayer = GeminiAudioPlayer(application) { isPlaying ->
            if (isPlaying) {
                _assistantState.value = AssistantState.SPEAKING
            } else if (_assistantState.value == AssistantState.SPEAKING) {
                _assistantState.value = AssistantState.IDLE
                if (_isContinuousMode.value && !userCancelledLoop) {
                    viewModelScope.launch {
                        delay(450)
                        if (_assistantState.value == AssistantState.IDLE && _isContinuousMode.value && !userCancelledLoop) {
                            _recognizedLiveText.value = ""
                            speechManager.startListening()
                        }
                    }
                }
            }
        }

        sosManager = EmergencySosManager(application, emergencyStorage, studentProfileStorage) { reassurance ->
            ttsManager.speak(reassurance)
        }

        ttsManager = TtsManager(application) { isSpeaking ->
            if (isSpeaking) {
                _assistantState.value = AssistantState.SPEAKING
            } else if (_assistantState.value == AssistantState.SPEAKING) {
                _assistantState.value = AssistantState.IDLE

                // CONTINUOUS CONVERSATION LOOP:
                // Once assistant finishes speaking, automatically restart listening so user never has to press mic again
                if (_isContinuousMode.value && !userCancelledLoop) {
                    viewModelScope.launch {
                        delay(450)
                        if (_assistantState.value == AssistantState.IDLE && _isContinuousMode.value && !userCancelledLoop) {
                            _recognizedLiveText.value = ""
                            speechManager.startListening()
                        }
                    }
                }
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

                // If in continuous mode and timed out or had no match, softly re-listen after pause
                if (_isContinuousMode.value && !userCancelledLoop &&
                    (err.contains("timeout", ignoreCase = true) || err.contains("No speech", ignoreCase = true))) {
                    viewModelScope.launch {
                        delay(900)
                        if (_assistantState.value == AssistantState.IDLE && _isContinuousMode.value && !userCancelledLoop) {
                            speechManager.startListening()
                        }
                    }
                }
            }
        )

        if (apiKeyStorage.hasApiKey()) {
            testCurrentConnection()
        }
    }

    fun isAccessibilityEnabled(): Boolean {
        return AlicAccessibilityService.isServiceEnabled
    }

    fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            _errorMessage.value = "Could not open accessibility settings: ${e.message}"
        }
    }

    fun toggleContinuousMode() {
        _isContinuousMode.value = !_isContinuousMode.value
        if (!_isContinuousMode.value && _assistantState.value == AssistantState.LISTENING) {
            speechManager.stopListening()
        }
    }

    fun stopConversationLoop() {
        userCancelledLoop = true
        stopSpeaking()
        speechManager.cancel()
        _assistantState.value = AssistantState.IDLE
    }

    fun switchTab(index: Int) {
        _currentTab.value = index
    }

    fun switchPersona(persona: Persona) {
        _currentPersona.value = persona
        ttsManager.applyPersonaVoice(persona)
        _voiceSettings.value = ttsManager.currentSettings
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
        userCancelledLoop = false
        when (_assistantState.value) {
            AssistantState.LISTENING -> {
                userCancelledLoop = true
                speechManager.stopListening()
                _assistantState.value = AssistantState.IDLE
            }
            AssistantState.SPEAKING -> {
                userCancelledLoop = true
                stopSpeaking()
            }
            AssistantState.THINKING -> {
                userCancelledLoop = true
                _assistantState.value = AssistantState.IDLE
            }
            AssistantState.IDLE -> {
                if (!apiKeyStorage.hasApiKey()) {
                    _errorMessage.value = "Please configure your Gemini API Key first."
                    _showSetupDialog.value = true
                    return
                }
                userCancelledLoop = false
                stopSpeaking()
                _recognizedLiveText.value = ""
                speechManager.startListening()
            }
        }
    }

    fun cancelListening() {
        userCancelledLoop = true
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

        userCancelledLoop = false
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

        // 1. Check for SOS Emergency Alert Cancellation (Feature 15)
        if (sosManager.isSosCancelTrigger(prompt) && sosState.value.isActive) {
            sosManager.cancelSos()
            val safeReply = "खुशी हुई जानकर कि आप सुरक्षित हैं। मैंने आपके तीनों कॉन्टैक्ट्स को 'सब ठीक है' का संदेश भेज दिया है।"
            _chatMessages.value = _chatMessages.value + ChatMessage(sender = MessageSender.ALIC, text = safeReply)
            return
        }

        // 2. Check for SOS Emergency Alert Trigger (Feature 15: "बचाओ बचाओ बचाओ")
        if (sosManager.isSosTrigger(prompt)) {
            sosManager.triggerSos()
            val sosReply = "🚨 SOS आपातकालीन मोड सक्रिय हो गया है! आपके 3 इमरजेंसी कॉन्टैक्ट्स को तुरंत लाइव लोकेशन लिंक भेज दिया गया है और कॉल किया जा रहा है। 112 डायल करने का विकल्प उपलब्ध है। मैं आपके साथ हूँ।"
            _chatMessages.value = _chatMessages.value + ChatMessage(sender = MessageSender.ALIC, text = sosReply)
            return
        }

        // 3. Check for Code Builder Request (Feature 16: Code, Website, App building)
        if (CodeBuilderEngine.isCodeBuildRequest(prompt)) {
            _isBuildingCode.value = true
            _assistantState.value = AssistantState.THINKING
            viewModelScope.launch {
                try {
                    val project = codeBuilderEngine.generateProject(
                        userPrompt = prompt,
                        apiKey = apiKey,
                        modelId = _selectedModel.value,
                        geminiService = geminiApiService,
                        onStepUpdate = { step, statusText ->
                            _codeBuildStep.value = step
                            _codeBuildStatusText.value = statusText
                        }
                    )
                    _activeGeneratedProject.value = project
                    _isBuildingCode.value = false
                    val alicReply = "${project.explanationHindi}\n\n📁 प्रोजेक्ट फ़ोल्डर: ${project.folderPath}\n📦 ZIP फ़ाइल: ${project.zipPath}\n\n${project.followUpQuestion}"
                    _chatMessages.value = _chatMessages.value + ChatMessage(sender = MessageSender.ALIC, text = alicReply)
                    _assistantState.value = AssistantState.SPEAKING
                    ttsManager.speak("मैंने आपके लिए प्रोजेक्ट तैयार कर दिया है। यह Downloads फ़ोल्डर में सहेज दिया गया है। क्या मैं समझा दूँ कि यह कैसे काम करता है?")
                } catch (e: Exception) {
                    _isBuildingCode.value = false
                    _codeBuildStep.value = CodeBuildStep.FAILED
                    val err = "प्रोजेक्ट बनाने में समस्या आई: ${e.message}"
                    _errorMessage.value = err
                    _chatMessages.value = _chatMessages.value + ChatMessage(sender = MessageSender.ALIC, text = err, isError = true)
                    _assistantState.value = AssistantState.IDLE
                }
            }
            return
        }

        // 4. Check if user request is an actionable phone intent
        val parsedAction = actionEngine.parseUserIntent(prompt)
        if (parsedAction != null) {
            if (parsedAction.type == ActionType.ANALYZE_SCREEN) {
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

        // 5. Standard ALIC-AI conversational reasoning with Student Profile Memory
        _assistantState.value = AssistantState.THINKING

        val profile = _studentProfile.value
        val memoryContext = if (profile.isConfigured) {
            "STUDENT MEMORY: Student Name is '${profile.name}', Class/Course is '${profile.gradeOrCourse}', Goal is '${profile.goal}', Weakest Subject is '${profile.weakSubject}'. Address student warmly by name and personalize all explanations, doubt solutions, study planner, and quiz questions."
        } else {
            "STUDENT PROFILE STATUS: First time conversation. Warmly ask the student for their Name, Class/Course, Goal, and Weakest Subject so you can serve as their personal Jarvis."
        }
        val enrichedInstructions = "${_currentPersona.value.systemPromptInstructions}\n\n$memoryContext"

        val voiceName = when (_currentPersona.value.id) {
            com.example.persona.PersonaId.AOEDE -> "Aoede"
            com.example.persona.PersonaId.CHARON -> "Charon"
            com.example.persona.PersonaId.PUCK -> "Puck"
            com.example.persona.PersonaId.FENRIR -> "Fenrir"
            com.example.persona.PersonaId.KORE -> "Kore"
            else -> "Aoede"
        }

        viewModelScope.launch {
            val result = geminiApiService.generateAssistantVoiceResponse(
                apiKey = apiKey,
                modelId = _selectedModel.value,
                userPrompt = prompt,
                voiceName = voiceName,
                systemInstructions = enrichedInstructions,
                recentHistory = _chatMessages.value
            )

            result.onSuccess { voiceResult ->
                val alicMsg = ChatMessage(sender = MessageSender.ALIC, text = voiceResult.text)
                _chatMessages.value = _chatMessages.value + alicMsg
                if (!_voiceSettings.value.muteSpokenReplies) {
                    _assistantState.value = AssistantState.SPEAKING
                    if (voiceResult.audioBase64 != null) {
                        val played = geminiAudioPlayer.playBase64Audio(
                            base64Data = voiceResult.audioBase64,
                            mimeType = voiceResult.mimeType
                        )
                        if (!played) {
                            ttsManager.speak(voiceResult.text)
                        }
                    } else {
                        ttsManager.speak(voiceResult.text)
                    }
                } else {
                    _assistantState.value = AssistantState.IDLE
                }
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

        // Announce action
        val statusText = if (action.type == ActionType.SEARCH_YOUTUBE) {
            val query = action.params["query"] ?: ""
            if (isAccessibilityEnabled()) {
                "YouTube पर $query का पहला गाना प्ले कर रहा हूँ..."
            } else {
                "YouTube खोल रहा हूँ। ऑटो-प्ले के लिए Accessibility परमिशन ऑन करें।"
            }
        } else {
            action.description
        }

        _chatMessages.value = _chatMessages.value + ChatMessage(sender = MessageSender.ALIC, text = statusText)
        ttsManager.speak(statusText)

        viewModelScope.launch {
            val success = actionEngine.executeAction(action)
            _actionAuditLog.value = _actionAuditLog.value + action
            _activeTaskAction.value = null

            val resultSpeech = if (success) {
                action.resultMessage ?: "Action completed."
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
            _errorMessage.value = "Failed to securely encrypt and save the API key."
        }
        return success
    }

    fun deleteApiKey() {
        apiKeyStorage.deleteApiKey()
        _hasConfiguredKey.value = false
        _maskedApiKey.value = ""
        _connectionStatus.value = ConnectionStatus.Untested
        _showSetupDialog.value = true
    }

    fun testCurrentConnection() {
        val apiKey = apiKeyStorage.getApiKey()
        if (apiKey.isNullOrBlank()) {
            _connectionStatus.value = ConnectionStatus.InvalidApiKey("No API key configured.")
            return
        }

        _connectionStatus.value = ConnectionStatus.Testing
        viewModelScope.launch {
            val status = geminiApiService.testConnection(apiKey, _selectedModel.value)
            _connectionStatus.value = status
            if (status is ConnectionStatus.InvalidApiKey || status is ConnectionStatus.RateLimit || status is ConnectionStatus.NetworkError) {
                _errorMessage.value = status.displayText()
            }
        }
    }

    fun selectModel(modelId: String) {
        apiKeyStorage.setSelectedModel(modelId)
        _selectedModel.value = modelId
        if (apiKeyStorage.hasApiKey()) {
            testCurrentConnection()
        }
    }

    fun updateVoiceSettings(settings: VoiceSettings) {
        ttsManager.updateSettings(settings)
        _voiceSettings.value = settings
    }

    fun testVoice() {
        ttsManager.testVoice()
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        speechManager.destroy()
    }
}
