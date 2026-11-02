package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.AlicViewModel
import com.example.data.AssistantState
import com.example.data.ChatMessage
import com.example.data.ConnectionStatus
import com.example.ui.components.OutlinedAnimatedButton
import com.example.ui.components.PlanetVoiceOrb
import com.example.ui.components.ThemeSelectionDialog
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen

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
    val pendingConfirmation by viewModel.pendingConfirmationAction.collectAsState()
    val activeAction by viewModel.activeTaskAction.collectAsState()
    val sosState by viewModel.sosState.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()

    // Dynamic App Theme State
    val themeColors by viewModel.themeColors.collectAsState()
    val currentThemePreset by viewModel.currentThemePreset.collectAsState()
    val customHexColor by viewModel.customHexColor.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }

    var showPermissionRationale by remember { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onMicClicked()
        } else {
            showPermissionRationale = true
        }
    }

    fun handlePlanetClick() {
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        themeColors.background,
                        if (themeColors.isDark) themeColors.cardBackground else themeColors.cardElevated,
                        themeColors.background
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Minimal Top Bar (Title, Connection dot, Quick Theme, Emergency SOS shortcut & Settings icon)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ALIC-AI",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeColors.textPrimary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when (connectionStatus) {
                                is ConnectionStatus.Connected -> SuccessGreen
                                is ConnectionStatus.Testing -> themeColors.primaryAccent
                                else -> themeColors.secondaryAccent
                            }
                        )
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Quick Theme & ORB Selector
                IconButton(
                    onClick = { showThemeDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(themeColors.cardBackground)
                        .border(1.dp, themeColors.cardBorder, CircleShape)
                        .testTag("home_theme_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Theme & ORB Settings",
                        tint = themeColors.primaryAccent,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Quick SOS Emergency trigger button
                IconButton(
                    onClick = { viewModel.triggerSos() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (sosState.isActive) ErrorRed else themeColors.cardBackground)
                        .border(1.dp, if (sosState.isActive) ErrorRed else themeColors.cardBorder, CircleShape)
                        .testTag("home_sos_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "SOS Emergency",
                        tint = if (sosState.isActive) Color.White else ErrorRed,
                        modifier = Modifier.size(19.dp)
                    )
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(themeColors.cardBackground)
                        .border(1.dp, themeColors.cardBorder, CircleShape)
                        .testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = themeColors.textSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        // Center: Planet ORB with 'A' and Dotted Circular Orbits OR Active SOS Alert Mode
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            if (sosState.isActive) {
                // Feature 15: SOS Emergency Active Banner & Reassurance
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("sos_emergency_active_card"),
                    color = Color(0xFF280B10),
                    border = BorderStroke(1.5.dp, ErrorRed)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(ErrorRed.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Emergency",
                                tint = ErrorRed,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "🚨 SOS आपातकालीन मोड सक्रिय",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "मैंने आपके तीनों कॉन्टैक्ट्स को सूचना दे दी है। मैं आपके साथ हूँ।",
                            fontSize = 13.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Location Link Action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1B070B))
                                .clickable {
                                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(sosState.locationLink)).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(mapIntent)
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Color(0xFFFF6B4A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "लाइव लोकेशन लिंक हर 1 मिनट में अपडेट हो रहा है",
                                fontSize = 11.sp,
                                color = Color(0xFFFFB4A2),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Dial 112 button
                            OutlinedAnimatedButton(
                                text = "112 कॉल करें",
                                icon = Icons.Default.Call,
                                onClick = { viewModel.dialPolice112() },
                                containerColor = ErrorRed.copy(alpha = 0.3f),
                                modifier = Modifier.weight(1f),
                                testTag = "sos_dial_112_button"
                            )

                            // Cancel SOS button
                            OutlinedAnimatedButton(
                                text = "मैं सुरक्षित हूँ",
                                icon = Icons.Default.Check,
                                onClick = { viewModel.cancelSos() },
                                containerColor = SuccessGreen.copy(alpha = 0.3f),
                                modifier = Modifier.weight(1f),
                                testTag = "sos_cancel_safe_button"
                            )
                        }
                    }
                }
            } else {
                // Standard Minimalist Planet ORB dynamically themed
                PlanetVoiceOrb(
                    assistantState = assistantState,
                    audioRms = audioRms,
                    onClick = { handlePlanetClick() },
                    themeColors = themeColors,
                    size = 280.dp,
                    modifier = Modifier.testTag("home_planet_voice_orb")
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Minimal Status Pill below Planet
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            when (assistantState) {
                                AssistantState.LISTENING -> themeColors.primaryAccent.copy(alpha = 0.15f)
                                AssistantState.THINKING -> themeColors.secondaryAccent.copy(alpha = 0.18f)
                                AssistantState.SPEAKING -> themeColors.primaryAccent.copy(alpha = 0.18f)
                                AssistantState.IDLE -> themeColors.cardBackground
                            }
                        )
                        .border(
                            1.dp,
                            when (assistantState) {
                                AssistantState.LISTENING -> themeColors.primaryAccent
                                AssistantState.THINKING -> themeColors.secondaryAccent
                                AssistantState.SPEAKING -> themeColors.primaryAccent
                                AssistantState.IDLE -> themeColors.cardBorder
                            },
                            RoundedCornerShape(22.dp)
                        )
                        .clickable { handlePlanetClick() }
                        .padding(horizontal = 20.dp, vertical = 9.dp)
                        .testTag("home_status_pill")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (assistantState == AssistantState.THINKING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = themeColors.secondaryAccent,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else if (assistantState == AssistantState.SPEAKING) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = themeColors.primaryAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = when (assistantState) {
                                AssistantState.IDLE -> {
                                    if (studentProfile.name.isNotBlank())
                                        "नमस्ते ${studentProfile.name}! बोलने के लिए टैप करें"
                                    else
                                        "बोलने के लिए प्लैनेट दबाएँ (Tap to Speak)"
                                }
                                AssistantState.LISTENING -> "सुन रहा हूँ... बोलिए!"
                                AssistantState.THINKING -> "ALIC-AI सोच रहा है..."
                                AssistantState.SPEAKING -> "उत्तर बोल रहा हूँ... (रोकने के लिए दबाएँ)"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = when (assistantState) {
                                AssistantState.LISTENING -> themeColors.primaryAccent
                                AssistantState.THINKING -> themeColors.secondaryAccent
                                AssistantState.SPEAKING -> themeColors.primaryAccent
                                AssistantState.IDLE -> themeColors.textSecondary
                            }
                        )
                    }
                }

                // Live transcription preview if speaking
                AnimatedVisibility(
                    visible = recognizedText.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = themeColors.cardBackground,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, themeColors.cardBorder)
                        ) {
                            Text(
                                text = "\"$recognizedText\"",
                                color = themeColors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Active Action Pill
            if (activeAction != null && !sosState.isActive) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeColors.cardElevated)
                        .border(1.dp, themeColors.primaryAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = themeColors.primaryAccent, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${activeAction!!.title}...",
                            fontSize = 12.sp,
                            color = themeColors.textPrimary
                        )
                    }
                }
            }
        }

        // Action Confirmation card if required
        if (pendingConfirmation != null && !sosState.isActive) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .padding(bottom = 8.dp),
                color = themeColors.cardElevated,
                border = BorderStroke(1.dp, themeColors.primaryAccent)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ACTION CONFIRMATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.primaryAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pendingConfirmation!!.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedAnimatedButton(
                            text = "Confirm",
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
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentPreset = currentThemePreset,
            currentCustomHex = customHexColor,
            themeColors = themeColors,
            onSelectPreset = { preset ->
                viewModel.selectThemePreset(preset)
            },
            onApplyCustomHex = { hex ->
                viewModel.setCustomThemeHex(hex)
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    // Permission Rationale Dialog
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Microphone Permission Required", color = themeColors.textPrimary) },
            text = {
                Text(
                    "ALIC-AI needs microphone access to listen to your voice and answer with AI intelligence.",
                    color = themeColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationale = false
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                ) {
                    Text("Allow Microphone", color = themeColors.primaryAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Dismiss", color = themeColors.textSecondary)
                }
            },
            containerColor = themeColors.cardBackground
        )
    }
}
