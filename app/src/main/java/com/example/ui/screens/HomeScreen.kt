package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.agent.AgentAction
import com.example.data.AssistantState
import com.example.data.ChatMessage
import com.example.data.ConnectionStatus
import com.example.data.MessageSender
import com.example.persona.ALL_PERSONAS
import com.example.persona.Persona
import com.example.service.AlicAccessibilityService
import com.example.ui.components.BigMicButton
import com.example.ui.components.GlowingOrb
import com.example.ui.components.MasterVoiceOrb
import com.example.ui.components.OutlinedAnimatedButton
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardElevatedDark
import com.example.ui.theme.CyberSky
import com.example.ui.theme.DeepNavyBackground
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MagentaGlow
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SoftCyan
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftLilac
import com.example.ui.theme.SoftSkyBlue
import com.example.ui.theme.SoftViolet
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
    val isContinuousMode by viewModel.isContinuousMode.collectAsState()
    val activeAction by viewModel.activeTaskAction.collectAsState()
    val pendingConfirmation by viewModel.pendingConfirmationAction.collectAsState()

    var showPermissionRationale by remember { mutableStateOf(false) }
    var showTypeDialog by remember { mutableStateOf(false) }
    var typedInput by remember { mutableStateOf("") }

    val isAccessibilityActive = AlicAccessibilityService.isServiceEnabled

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

    // Pulsing aura animation for continuous listening
    val infiniteTransition = rememberInfiniteTransition(label = "continuous_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF04060E),
                        SpaceDark,
                        Color(0xFF090D22)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Branding & Status Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(currentPersona.primaryColor, currentPersona.secondaryColor)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentPersona.avatarLetter,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Alic AI",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "GEMINI",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF60A5FA)
                            )
                        }
                    }
                    Text(
                        text = "Developed by Rohit Raj",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = CyberSky
                    )
                }
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
                        .padding(horizontal = 9.dp, vertical = 5.dp)
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
                        Spacer(modifier = Modifier.width(5.dp))
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
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Center Content Scroll
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Accessibility Permission Banner (Critical for auto-play & app control)
            if (!isAccessibilityActive) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.openAccessibilitySettings() },
                    color = Color(0xFF1E1430),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(NeonPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = "Accessibility",
                                tint = NeonPurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "एक्सेसिबिलिटी परमिशन बंद है (Enable Accessibility)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "YouTube में गाना ऑटो-प्ले करने और ऐप खोलने के लिए यहाँ टैप करके ऑन करें।",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonPurple)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "ON करें", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                // Enabled status pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF06281E))
                        .border(1.dp, SuccessGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Active", tint = SuccessGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "एक्सेसिबिलिटी सक्रिय है: YouTube ऑटो-प्ले और ऐप कंट्रोल चालू",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SuccessGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Continuous Conversation Mode Card (Feature requested: no need to tap mic again and again)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isContinuousMode) Color(0xFF0F1E3D) else CardDark
                    )
                    .border(
                        1.dp,
                        if (isContinuousMode) CyberSky.copy(alpha = 0.7f) else CardBorderDark,
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isContinuousMode) CyberSky.copy(alpha = 0.2f) else CardElevatedDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hearing,
                                contentDescription = "Loop",
                                tint = if (isContinuousMode) CyberSky else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isContinuousMode) "निरंतर बातचीत मोड चालू (Continuous Talk)" else "निरंतर बातचीत बंद (Continuous Talk Off)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isContinuousMode) CyberSky else TextSecondary
                            )
                            Text(
                                text = if (isContinuousMode) "माइक 1 बार दबाएँ - फिर लगातार बोलते रहें" else "हर बार माइक दबाना होगा",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                    Switch(
                        checked = isContinuousMode,
                        onCheckedChange = { viewModel.toggleContinuousMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberSky,
                            checkedTrackColor = Color(0xFF1D4ED8),
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CardElevatedDark
                        ),
                        modifier = Modifier.scale(0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Persona Selector Bar (Gemini is first!)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

            Spacer(modifier = Modifier.height(10.dp))

            // Master Voice Orb (Matches PNG photo exactly: gradient disc, mic silhouette, cyan & lavender wave brackets)
            MasterVoiceOrb(
                assistantState = assistantState,
                audioRms = audioRms,
                onClick = { handleMicClick() },
                size = 220.dp,
                modifier = Modifier.testTag("home_master_voice_orb")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status Indicator Pill (Soft Palette from PNG)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        when (assistantState) {
                            AssistantState.LISTENING -> SoftCyan.copy(alpha = 0.20f)
                            AssistantState.THINKING -> SoftLavender.copy(alpha = 0.22f)
                            AssistantState.SPEAKING -> SoftLilac.copy(alpha = 0.24f)
                            AssistantState.IDLE -> CardElevatedDark
                        }
                    )
                    .border(
                        1.2.dp,
                        when (assistantState) {
                            AssistantState.LISTENING -> SoftCyan
                            AssistantState.THINKING -> SoftLavender
                            AssistantState.SPEAKING -> SoftLilac
                            AssistantState.IDLE -> CardBorderDark
                        },
                        RoundedCornerShape(18.dp)
                    )
                    .clickable { handleMicClick() }
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (assistantState == AssistantState.THINKING) {
                        CircularProgressIndicator(modifier = Modifier.size(13.dp), color = SoftLavender, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = when (assistantState) {
                            AssistantState.IDLE -> if (isContinuousMode) "रेडी • माइक 1 बार दबाएँ और बोलते रहें" else "रेडी • माइक या ऑर्ब दबाकर बोलें"
                            AssistantState.LISTENING -> "सुन रहा हूँ... बोलिए!"
                            AssistantState.THINKING -> "${currentPersona.name} सोच रहा है..."
                            AssistantState.SPEAKING -> "जवाब बोल रहा हूँ..."
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when (assistantState) {
                            AssistantState.LISTENING -> SoftCyan
                            AssistantState.THINKING -> SoftLavender
                            AssistantState.SPEAKING -> SoftLilac
                            AssistantState.IDLE -> TextPrimary
                        }
                    )
                }
            }

            // Pending Action Confirmation Card
            if (pendingConfirmation != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    color = CardElevatedDark,
                    border = BorderStroke(1.dp, CyberSky)
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
                        Text(
                            text = "चल रहा है: ${activeAction!!.title}...",
                            fontSize = 13.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Live recognized voice transcription card
            AnimatedVisibility(
                visible = recognizedText.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp)),
                        color = CardElevatedDark,
                        border = BorderStroke(1.dp, CyberSky.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "आपकी आवाज़ (SPEECH HEARD)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberSky, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(text = "\"$recognizedText\"", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Latest response card
            if (recognizedText.isEmpty() && latestAlicMessage != null && pendingConfirmation == null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onNavigateToChat() },
                    color = CardDark,
                    border = BorderStroke(1.dp, CardBorderDark)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${currentPersona.name} का उत्तर",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentPersona.secondaryColor,
                                letterSpacing = 1.sp
                            )
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
                        Spacer(modifier = Modifier.height(3.dp))
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

            Spacer(modifier = Modifier.height(12.dp))

            // Action Suggestion Chips (Hindi & English popular commands)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val suggestions = listOf(
                    "YouTube ओपन करके यो यो हनी सिंह का पहला गाना प्ले करो",
                    "टॉर्च चालू करो",
                    "व्हाट्सएप खोलो",
                    "स्क्रीन पर क्या है?",
                    "अलार्म सेट करो",
                    "टॉर्च बंद करो"
                )
                suggestions.forEach { suggestion ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardDark)
                            .border(1.dp, CardBorderDark, RoundedCornerShape(20.dp))
                            .clickable { viewModel.submitTypedPrompt(suggestion) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(text = suggestion, fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Bottom Controls: Keyboard, Big Mic Button (Loop Aware), Stop Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
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
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = "Type",
                    tint = CyberSky,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Big Mic with Loop Aura
            BigMicButton(
                assistantState = assistantState,
                onClick = { handleMicClick() }
            )

            // Stop / Cancel button for speech or loop
            IconButton(
                onClick = {
                    if (assistantState == AssistantState.SPEAKING || assistantState == AssistantState.LISTENING) {
                        viewModel.stopConversationLoop()
                    } else {
                        onNavigateToChat()
                    }
                },
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(CardDark)
                    .border(
                        1.dp,
                        if (assistantState == AssistantState.SPEAKING || assistantState == AssistantState.LISTENING)
                            MagentaGlow.copy(alpha = 0.6f)
                        else CardBorderDark,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (assistantState == AssistantState.SPEAKING || assistantState == AssistantState.LISTENING)
                        Icons.Default.Stop
                    else Icons.Default.AutoAwesome,
                    contentDescription = "Control",
                    tint = if (assistantState == AssistantState.SPEAKING || assistantState == AssistantState.LISTENING)
                        MagentaGlow
                    else CyberSky,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }

    // Typed Query Dialog
    if (showTypeDialog) {
        AlertDialog(
            onDismissRequest = { showTypeDialog = false },
            title = {
                Text(
                    text = "Type your command or question",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = typedInput,
                        onValueChange = { typedInput = it },
                        label = { Text("Ask ${currentPersona.name} in Hindi or English") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("typed_prompt_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberSky,
                            unfocusedBorderColor = CardBorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = false,
                        maxLines = 4
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toSend = typedInput.trim()
                        if (toSend.isNotEmpty()) {
                            showTypeDialog = false
                            typedInput = ""
                            viewModel.submitTypedPrompt(toSend)
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
            containerColor = CardElevatedDark
        )
    }

    // Permission Rationale Dialog
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Microphone Permission Required", color = TextPrimary) },
            text = {
                Text(
                    "Alic AI needs microphone access to listen to your voice and execute agentic commands. Please grant microphone access in device settings.",
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
                    Text("Try Again", color = CyberSky)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Dismiss", color = TextSecondary)
                }
            },
            containerColor = CardElevatedDark
        )
    }
}

@Composable
fun PersonaAvatarBadge(
    persona: Persona,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) CardElevatedDark else CardDark)
            .border(
                1.5.dp,
                if (isSelected) persona.primaryColor else CardBorderDark,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(persona.primaryColor, persona.secondaryColor))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = persona.avatarLetter,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = persona.name,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) TextPrimary else TextSecondary
                )
                Text(
                    text = persona.subtitle.take(16),
                    fontSize = 9.sp,
                    color = if (isSelected) persona.primaryColor else TextMuted
                )
            }
        }
    }
}
