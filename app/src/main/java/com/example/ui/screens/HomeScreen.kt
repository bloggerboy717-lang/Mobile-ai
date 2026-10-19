package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.AlicViewModel
import com.example.agent.AgentAction
import com.example.data.AssistantState
import com.example.data.ChatMessage
import com.example.data.ConnectionStatus
import com.example.data.MessageSender
import com.example.persona.ALL_PERSONAS
import com.example.persona.Persona
import com.example.ui.components.BigMicButton
import com.example.ui.components.GlowingOrb
import com.example.ui.components.OutlinedAnimatedButton
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardElevatedDark
import com.example.ui.theme.CyberSky
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MagentaGlow
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    viewModel: AlicViewModel,
    assistantState: AssistantState,
    recognizedText: String,
    audioRms: Float,
    connectionStatus: ConnectionStatus,
    hasConfiguredKey: Boolean,
    chatMessages: List<ChatMessage>,
    onNavigateToChat: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentPersona by viewModel.currentPersona.collectAsState()
    val activeAction by viewModel.activeTaskAction.collectAsState()
    val pendingConfirmation by viewModel.pendingConfirmationAction.collectAsState()

    var showPermissionRationale by remember { mutableStateOf(false) }
    var showTypeDialog by remember { mutableStateOf(false) }
    var typedInput by remember { mutableStateOf("") }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onMicClicked()
        } else {
            showPermissionRationale = true
        }
    }

    fun handleMicClick() {
        if (!hasConfiguredKey) {
            viewModel.openSetupDialog()
            return
        }

        if (assistantState == AssistantState.SPEAKING) {
            viewModel.stopSpeaking()
            return
        }

        if (assistantState == AssistantState.LISTENING) {
            viewModel.onMicClicked()
            return
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.onMicClicked()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val latestAlicMessage = chatMessages.lastOrNull { it.sender == MessageSender.ALIC }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceDark)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Branding Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Alic AI",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "AI Agent • by Rohit Raj",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = CyberSky
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Connection Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardDark)
                        .border(
                            1.dp,
                            when (connectionStatus) {
                                is ConnectionStatus.Connected -> SuccessGreen.copy(alpha = 0.6f)
                                is ConnectionStatus.Testing -> CyberSky.copy(alpha = 0.6f)
                                is ConnectionStatus.InvalidApiKey, is ConnectionStatus.RateLimit -> ErrorRed.copy(alpha = 0.6f)
                                else -> CardBorderDark
                            },
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onNavigateToSettings() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    when (connectionStatus) {
                                        is ConnectionStatus.Connected -> SuccessGreen
                                        is ConnectionStatus.Testing -> CyberSky
                                        is ConnectionStatus.InvalidApiKey, is ConnectionStatus.RateLimit -> ErrorRed
                                        else -> NeonPurple
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (connectionStatus) {
                                is ConnectionStatus.Connected -> "Online"
                                is ConnectionStatus.Testing -> "Testing"
                                is ConnectionStatus.InvalidApiKey -> "Key Error"
                                is ConnectionStatus.RateLimit -> "Quota"
                                else -> if (hasConfiguredKey) "Ready" else "Set Key"
                            },
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Center Content Scroll
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Greeting & Persona Title
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Hello Rohit!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "I'm ${currentPersona.name}, your AI agent. How can I help you today?",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5 Personas Horizontal Selector Bar
            Text(
                text = "SELECT AI PERSONA",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = CyberSky,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ALL_PERSONAS.forEach { persona ->
                    val isSelected = persona.id == currentPersona.id
                    PersonaAvatarBadge(
                        persona = persona,
                        isSelected = isSelected,
                        onClick = { viewModel.switchPersona(persona) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive 3D-Style Glowing AI Orb
            GlowingOrb(
                state = assistantState,
                audioRms = audioRms,
                size = 200.dp,
                modifier = Modifier.testTag("home_glowing_orb")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Status Indicator Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        when (assistantState) {
                            AssistantState.LISTENING -> CyberSky.copy(alpha = 0.2f)
                            AssistantState.THINKING -> NeonPurple.copy(alpha = 0.22f)
                            AssistantState.SPEAKING -> MagentaGlow.copy(alpha = 0.22f)
                            AssistantState.IDLE -> CardElevatedDark
                        }
                    )
                    .border(
                        1.dp,
                        when (assistantState) {
                            AssistantState.LISTENING -> CyberSky.copy(alpha = 0.6f)
                            AssistantState.THINKING -> NeonPurple.copy(alpha = 0.6f)
                            AssistantState.SPEAKING -> MagentaGlow.copy(alpha = 0.6f)
                            AssistantState.IDLE -> CardBorderDark
                        },
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (assistantState == AssistantState.THINKING) {
                        CircularProgressIndicator(modifier = Modifier.size(13.dp), color = NeonPurple, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = when (assistantState) {
                            AssistantState.IDLE -> "Ready • Tap mic to speak"
                            AssistantState.LISTENING -> "Listening to your voice..."
                            AssistantState.THINKING -> "${currentPersona.name} is thinking..."
                            AssistantState.SPEAKING -> "Speaking response..."
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (assistantState) {
                            AssistantState.LISTENING -> CyberSky
                            AssistantState.THINKING -> NeonPurple
                            AssistantState.SPEAKING -> MagentaGlow
                            AssistantState.IDLE -> TextPrimary
                        }
                    )
                }
            }

            // Pending Action Confirmation Card
            if (pendingConfirmation != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    color = CardElevatedDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSky)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ACTION CONFIRMATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberSky,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Alic AI is prepared to: ${pendingConfirmation!!.title}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = pendingConfirmation!!.description,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedAnimatedButton(
                                text = "Confirm & Run",
                                icon = Icons.Default.Check,
                                onClick = { viewModel.confirmPendingAction() },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedAnimatedButton(
                                text = "Cancel",
                                icon = Icons.Default.Close,
                                onClick = { viewModel.cancelPendingAction() },
                                containerColor = Color(0x22EF4444),
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                    }
                }
            }

            // Active Task Action Indicator
            if (activeAction != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x332563EB))
                        .border(1.dp, CyberSky, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyberSky, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Running: ${activeAction!!.title}...", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Live recognized voice transcription card
            AnimatedVisibility(
                visible = recognizedText.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    color = CardElevatedDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSky.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "HEARD SPEECH", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberSky, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "\"$recognizedText\"", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Latest response card
            if (recognizedText.isEmpty() && latestAlicMessage != null && pendingConfirmation == null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onNavigateToChat() },
                    color = CardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "LATEST RESPONSE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonPurple, letterSpacing = 1.sp)
                            Row {
                                if (assistantState == AssistantState.SPEAKING) {
                                    IconButton(
                                        onClick = { viewModel.stopSpeaking() },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop", tint = MagentaGlow, modifier = Modifier.size(16.dp))
                                    }
                                } else {
                                    IconButton(
                                        onClick = { viewModel.repeatMessage(latestAlicMessage) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Repeat", tint = CyberSky, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = latestAlicMessage.text,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Suggestion Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val suggestions = listOf(
                    "Open YouTube & play trending video",
                    "Turn on flashlight",
                    "What is on my screen?",
                    "Open Google Maps",
                    "Open Wi-Fi settings"
                )
                suggestions.forEach { suggestion ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardDark)
                            .border(1.dp, CardBorderDark, RoundedCornerShape(20.dp))
                            .clickable { viewModel.submitTypedPrompt(suggestion) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(text = suggestion, fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Bottom Controls: Keyboard, Big Mic Button, Chat Shortcut
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { showTypeDialog = true },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(CardDark)
                    .border(1.dp, CardBorderDark, CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Keyboard, contentDescription = "Type", tint = CyberSky, modifier = Modifier.size(22.dp))
            }

            BigMicButton(
                assistantState = assistantState,
                onClick = { handleMicClick() }
            )

            IconButton(
                onClick = {
                    if (assistantState == AssistantState.SPEAKING) {
                        viewModel.stopSpeaking()
                    } else {
                        onNavigateToChat()
                    }
                },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(CardDark)
                    .border(1.dp, CardBorderDark, CircleShape)
            ) {
                Icon(
                    imageVector = if (assistantState == AssistantState.SPEAKING) Icons.Default.Stop else Icons.Default.VolumeUp,
                    contentDescription = "Audio Control",
                    tint = if (assistantState == AssistantState.SPEAKING) MagentaGlow else NeonPurple,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }

    // Typed Question Dialog
    if (showTypeDialog) {
        AlertDialog(
            onDismissRequest = { showTypeDialog = false },
            title = { Text(text = "Ask ${currentPersona.name}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column {
                    Text(text = "Type your query or phone command:", fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = typedInput,
                        onValueChange = { typedInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g. Turn on flashlight, Open Maps...", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberSky,
                            unfocusedBorderColor = CardElevatedDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = CyberSky
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (typedInput.isNotBlank()) {
                            viewModel.submitTypedPrompt(typedInput)
                            typedInput = ""
                            showTypeDialog = false
                        }
                    }
                ) {
                    Text("Send", color = CyberSky, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTypeDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Microphone Permission Dialog
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Microphone Access Required", color = TextPrimary) },
            text = {
                Text(
                    "Alic AI needs microphone permission to recognize your voice. You can also type commands with the keyboard anytime.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationale = false
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                ) {
                    Text("Grant Permission", color = CyberSky)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun PersonaAvatarBadge(
    persona: Persona,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) Brush.radialGradient(listOf(persona.primaryColor, persona.secondaryColor))
                    else Brush.radialGradient(listOf(CardElevatedDark, CardDark))
                )
                .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = if (isSelected) persona.primaryColor else CardBorderDark,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = persona.avatarLetter,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) SpaceDark else TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = persona.name,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) persona.primaryColor else TextSecondary
        )
        Text(
            text = persona.gender.name.lowercase().replaceFirstChar { it.uppercase() },
            fontSize = 9.sp,
            color = TextMuted
        )
    }
}
