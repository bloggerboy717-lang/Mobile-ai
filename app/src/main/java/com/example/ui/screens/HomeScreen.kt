package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.data.AssistantState
import com.example.data.ChatMessage
import com.example.data.ConnectionStatus
import com.example.data.MessageSender
import com.example.ui.components.GlowingOrb
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Alic AI",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CardElevatedDark)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "by Rohit Raj",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = CyberSky
                        )
                    }
                }
            }

            // Connection Badge / Settings Shortcut
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardDark)
                    .border(
                        1.dp,
                        when (connectionStatus) {
                            is ConnectionStatus.Connected -> SuccessGreen.copy(alpha = 0.5f)
                            is ConnectionStatus.Testing -> CyberSky.copy(alpha = 0.5f)
                            is ConnectionStatus.InvalidApiKey, is ConnectionStatus.RateLimit -> ErrorRed.copy(alpha = 0.5f)
                            else -> NeonPurple.copy(alpha = 0.4f)
                        },
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onNavigateToSettings() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("home_connection_badge")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
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
                            is ConnectionStatus.Testing -> "Testing..."
                            is ConnectionStatus.InvalidApiKey -> "Key Invalid"
                            is ConnectionStatus.RateLimit -> "Rate Limit"
                            else -> if (hasConfiguredKey) "Configured" else "Setup Key"
                        },
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Center Content with Scroll for smaller screens
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        when (assistantState) {
                            AssistantState.LISTENING -> CyberSky.copy(alpha = 0.18f)
                            AssistantState.THINKING -> NeonPurple.copy(alpha = 0.20f)
                            AssistantState.SPEAKING -> MagentaGlow.copy(alpha = 0.20f)
                            AssistantState.IDLE -> CardElevatedDark
                        }
                    )
                    .border(
                        1.dp,
                        when (assistantState) {
                            AssistantState.LISTENING -> CyberSky.copy(alpha = 0.5f)
                            AssistantState.THINKING -> NeonPurple.copy(alpha = 0.5f)
                            AssistantState.SPEAKING -> MagentaGlow.copy(alpha = 0.5f)
                            AssistantState.IDLE -> CardDark
                        },
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 8.dp)
                    .testTag("assistant_status_pill")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (assistantState == AssistantState.THINKING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = NeonPurple,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = when (assistantState) {
                            AssistantState.IDLE -> "Hello! How can I help you?"
                            AssistantState.LISTENING -> "Listening to your voice..."
                            AssistantState.THINKING -> "Alic is thinking..."
                            AssistantState.SPEAKING -> "Speaking response..."
                        },
                        fontSize = 14.sp,
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

            Spacer(modifier = Modifier.height(24.dp))

            // Glowing Animated Orb
            GlowingOrb(
                state = assistantState,
                audioRms = audioRms,
                size = 230.dp,
                modifier = Modifier.testTag("home_glowing_orb")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Live recognized voice transcription card
            AnimatedVisibility(
                visible = recognizedText.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = CardElevatedDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSky.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "HEARD SPEECH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberSky,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"$recognizedText\"",
                            fontSize = 15.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Latest response preview card (if not currently listening and speech text is empty)
            if (recognizedText.isEmpty() && latestAlicMessage != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToChat() },
                    color = CardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardElevatedDark)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LATEST RESPONSE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonPurple,
                                letterSpacing = 1.sp
                            )
                            Row {
                                if (assistantState == AssistantState.SPEAKING) {
                                    IconButton(
                                        onClick = { viewModel.stopSpeaking() },
                                        modifier = Modifier.size(28.dp).testTag("home_stop_speaking_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = "Stop Speaking",
                                            tint = MagentaGlow,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = { viewModel.repeatMessage(latestAlicMessage) },
                                        modifier = Modifier.size(28.dp).testTag("home_repeat_speaking_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Listen Again",
                                            tint = CyberSky,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = latestAlicMessage.text,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Suggestion chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val suggestions = listOf(
                    "What can you do?",
                    "Tell me an interesting science fact",
                    "Explain quantum computing simply",
                    "Tell me a fun tech joke",
                    "Write a short haiku about stars"
                )
                suggestions.forEach { suggestion ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardDark)
                            .border(1.dp, CardElevatedDark, RoundedCornerShape(20.dp))
                            .clickable { viewModel.submitTypedPrompt(suggestion) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("suggestion_chip_${suggestion.take(8).lowercase().replace(' ', '_')}")
                    ) {
                        Text(
                            text = suggestion,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Animated Microphone & Action Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type Question Button
            IconButton(
                onClick = { showTypeDialog = true },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(CardDark)
                    .border(1.dp, CardElevatedDark, CircleShape)
                    .testTag("home_type_input_toggle_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = "Type Question",
                    tint = CyberSky,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Big Central Animated Mic Button
            BigMicButton(
                assistantState = assistantState,
                onClick = { handleMicClick() }
            )

            // Stop Speaking / Clear or Chat shortcut
            IconButton(
                onClick = {
                    if (assistantState == AssistantState.SPEAKING) {
                        viewModel.stopSpeaking()
                    } else {
                        onNavigateToChat()
                    }
                },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(CardDark)
                    .border(1.dp, CardElevatedDark, CircleShape)
                    .testTag("home_chat_or_stop_button")
            ) {
                Icon(
                    imageVector = if (assistantState == AssistantState.SPEAKING) Icons.Default.Stop else Icons.Default.VolumeUp,
                    contentDescription = if (assistantState == AssistantState.SPEAKING) "Stop Speaking" else "Open Chat",
                    tint = if (assistantState == AssistantState.SPEAKING) MagentaGlow else NeonPurple,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    // Typed Question Dialog
    if (showTypeDialog) {
        AlertDialog(
            onDismissRequest = { showTypeDialog = false },
            title = {
                Text(
                    text = "Ask Alic AI",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Type your question below. Alic AI will respond in chat and speak the response.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = typedInput,
                        onValueChange = { typedInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("typed_prompt_input_field"),
                        placeholder = { Text("What would you like to know?", color = TextMuted) },
                        maxLines = 4,
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
                    },
                    modifier = Modifier.testTag("submit_typed_prompt_button")
                ) {
                    Text("Ask", color = CyberSky, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTypeDialog = false }
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Microphone Permission Rationale Dialog
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Microphone Access Required", color = TextPrimary) },
            text = {
                Text(
                    "Alic AI needs microphone permission to hear your questions and convert your voice to text. You can also type questions using the keyboard button anytime.",
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
                    Text("Type Instead", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun BigMicButton(
    assistantState: AssistantState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MicWaveTransition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MicPulseScale"
    )

    val isListening = assistantState == AssistantState.LISTENING

    Box(
        modifier = modifier.size(92.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing Wave Ring when listening
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(CyberSky.copy(alpha = 0.22f))
            )
        }

        // Central Microphone Button
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    when (assistantState) {
                        AssistantState.LISTENING -> Brush.linearGradient(listOf(CyberSky, NeonPurple))
                        AssistantState.SPEAKING -> Brush.linearGradient(listOf(MagentaGlow, NeonPurple))
                        AssistantState.THINKING -> Brush.linearGradient(listOf(NeonPurple, CyberSky))
                        AssistantState.IDLE -> Brush.linearGradient(listOf(CyberSky, NeonPurple))
                    }
                )
                .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                .clickable { onClick() }
                .testTag("big_mic_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (assistantState) {
                    AssistantState.SPEAKING -> Icons.Default.Stop
                    AssistantState.THINKING -> Icons.Default.Refresh
                    else -> Icons.Default.Mic
                },
                contentDescription = "Voice Input",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
