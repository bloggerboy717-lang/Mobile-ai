package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AlicViewModel
import com.example.data.AVAILABLE_GEMINI_MODELS
import com.example.data.ConnectionStatus
import com.example.data.VoiceSettings
import com.example.persona.ALL_PERSONAS
import com.example.persona.Persona
import com.example.ui.components.ConnectionStatusBanner
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
import com.example.ui.theme.WarningAmber

@Composable
fun SettingsScreen(
    viewModel: AlicViewModel,
    hasConfiguredKey: Boolean,
    maskedApiKey: String,
    connectionStatus: ConnectionStatus,
    selectedModel: String,
    voiceSettings: VoiceSettings,
    onNavigateToPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentPersona by viewModel.currentPersona.collectAsState()

    var editingKey by remember { mutableStateOf(!hasConfiguredKey) }
    var apiKeyInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceDark)
    ) {
        // Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardDark,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 0. GOOGLE ACCOUNT & ACCESS LICENSE GATE
            val authenticatedEmail by viewModel.authenticatedUserEmail.collectAsState()
            SettingsCard(
                title = "Google Account & Access License",
                icon = Icons.Default.Security,
                iconTint = CyberSky
            ) {
                Text(
                    text = "Your access to Alic AI Voice Assistant is verified with Firebase Auth and Cloud Firestore (voiceAiLicenses).",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = CardElevatedDark,
                    border = BorderStroke(1.dp, CardBorderDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Signed in as:",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = authenticatedEmail ?: "Google Account",
                                fontSize = 14.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SuccessGreen.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "ACTIVE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedAnimatedButton(
                        text = "Change Code",
                        onClick = {
                            viewModel.revokeAccessAndChangeCode()
                        },
                        modifier = Modifier.weight(1f),
                        borderGradient = Brush.horizontalGradient(listOf(CyberSky, NeonPurple)),
                        containerColor = CyberSky.copy(alpha = 0.15f)
                    )
                    OutlinedAnimatedButton(
                        text = "Sign Out",
                        onClick = {
                            viewModel.signOutFromApp()
                        },
                        modifier = Modifier.weight(1f),
                        borderGradient = Brush.horizontalGradient(listOf(ErrorRed, ErrorRed.copy(alpha = 0.7f))),
                        containerColor = ErrorRed.copy(alpha = 0.15f)
                    )
                }
            }

            // 1. GEMINI API CONFIGURATION
            SettingsCard(
                title = "Gemini API Key Configuration",
                icon = Icons.Default.Key,
                iconTint = CyberSky
            ) {
                Text(
                    text = "Provide your personal Google Gemini API key. The key is encrypted locally using Android Keystore AES-GCM.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (hasConfiguredKey && !editingKey) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        color = CardElevatedDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "SAVED API KEY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberSky, letterSpacing = 1.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = maskedApiKey, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                            }
                            Row {
                                IconButton(onClick = { editingKey = true }) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = CyberSky)
                                }
                                IconButton(onClick = { showDeleteConfirmDialog = true }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it.trim() },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Gemini API Key", color = TextSecondary) },
                        placeholder = { Text("Enter AIzaSy... key", color = TextMuted) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility",
                                    tint = TextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberSky,
                            unfocusedBorderColor = CardBorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = CyberSky
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedAnimatedButton(
                            text = "Save API Key",
                            icon = Icons.Default.Save,
                            onClick = {
                                if (apiKeyInput.isNotBlank()) {
                                    viewModel.saveApiKey(apiKeyInput)
                                    editingKey = false
                                    apiKeyInput = ""
                                    Toast.makeText(context, "API Key saved securely", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = apiKeyInput.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        )
                        if (hasConfiguredKey) {
                            TextButton(onClick = { editingKey = false; apiKeyInput = "" }) {
                                Text("Cancel", color = TextSecondary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                ConnectionStatusBanner(status = connectionStatus)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedAnimatedButton(
                    text = "Test Gemini Connection",
                    icon = Icons.Default.NetworkCheck,
                    onClick = { viewModel.testCurrentConnection() },
                    enabled = hasConfiguredKey && connectionStatus !is ConnectionStatus.Testing,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "MODEL CONFIGURATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonPurple, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(6.dp))

                AVAILABLE_GEMINI_MODELS.forEach { model ->
                    val isSelected = model.id == selectedModel
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.selectModel(model.id) },
                        color = if (isSelected) Color(0x2238BDF8) else CardElevatedDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyberSky else CardBorderDark)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, if (isSelected) CyberSky else TextMuted, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyberSky))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = model.displayName, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) CyberSky else TextPrimary)
                                Text(text = model.description, fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }

            // 2. 5 AI PERSONAS & VOICE SETTINGS
            SettingsCard(
                title = "5 AI Personas & Voice Settings",
                icon = Icons.Default.RecordVoiceOver,
                iconTint = NeonPurple
            ) {
                Text(
                    text = "Choose from 5 distinct AI companions. Each features a custom voice configuration and unique intelligence role.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                ALL_PERSONAS.forEach { persona ->
                    val isSelected = persona.id == currentPersona.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.switchPersona(persona) },
                        color = if (isSelected) Color(0x2238BDF8) else CardElevatedDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyberSky else CardBorderDark)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Brush.radialGradient(listOf(persona.primaryColor, persona.secondaryColor))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = persona.avatarLetter, color = SpaceDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = persona.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "(${persona.gender.name.lowercase()})", fontSize = 11.sp, color = CyberSky)
                                }
                                Text(text = persona.subtitle, fontSize = 11.sp, color = TextMuted)
                            }
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CyberSky)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "Active", fontSize = 11.sp, color = SpaceDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mute Spoken Replies
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardElevatedDark)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (voiceSettings.muteSpokenReplies) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = if (voiceSettings.muteSpokenReplies) WarningAmber else CyberSky,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Mute Spoken Responses", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    Switch(
                        checked = voiceSettings.muteSpokenReplies,
                        onCheckedChange = { isMuted ->
                            viewModel.updateVoiceSettings(voiceSettings.copy(muteSpokenReplies = isMuted))
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberSky, checkedTrackColor = CardBorderDark)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Speech Rate & Pitch
                Text(text = "Speech Rate: ${String.format(java.util.Locale.US, "%.1fx", voiceSettings.speechRate)}", fontSize = 12.sp, color = TextPrimary)
                Slider(
                    value = voiceSettings.speechRate,
                    onValueChange = { viewModel.updateVoiceSettings(voiceSettings.copy(speechRate = it)) },
                    valueRange = 0.5f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = CyberSky, activeTrackColor = CyberSky)
                )

                Text(text = "Voice Pitch: ${String.format(java.util.Locale.US, "%.1fx", voiceSettings.pitch)}", fontSize = 12.sp, color = TextPrimary)
                Slider(
                    value = voiceSettings.pitch,
                    onValueChange = { viewModel.updateVoiceSettings(voiceSettings.copy(pitch = it)) },
                    valueRange = 0.5f..1.5f,
                    colors = SliderDefaults.colors(thumbColor = NeonPurple, activeTrackColor = NeonPurple)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedAnimatedButton(
                    text = "Test Persona Voice",
                    icon = Icons.Default.PlayArrow,
                    onClick = { viewModel.testVoice() },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. PERMISSIONS DASHBOARD SHORTCUT
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onNavigateToPermissions() },
                color = CardDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Permissions Dashboard", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = "Microphone, Camera, Vision & Accessibility", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                }
            }

            // 4. SECURITY & BEST PRACTICES
            SettingsCard(
                title = "Security & Keystore",
                icon = Icons.Default.Security,
                iconTint = SuccessGreen
            ) {
                Text(
                    text = "• Hardware-Backed Protection: Stored keys are encrypted with AES/GCM backed by Android KeyStore.\n\n" +
                            "• Zero Embedded Keys: No API credentials are committed to git repositories or packaged in the build.\n\n" +
                            "• Production Recommendation: For production deployments, route remote AI requests through an authenticated backend server proxy to keep API keys server-side.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
            }

            // 5. ABOUT
            SettingsCard(
                title = "About Alic AI",
                icon = Icons.Default.Info,
                iconTint = MagentaGlow
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "App", fontSize = 13.sp, color = TextMuted)
                        Text(text = "Alic AI — Ultimate Agentic Assistant", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Developer", fontSize = 13.sp, color = TextMuted)
                        Text(text = "Rohit Raj", fontSize = 13.sp, color = CyberSky, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Version", fontSize = 13.sp, color = TextMuted)
                        Text(text = "2.0.0 (Agentic Edition)", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Saved API Key?", color = TextPrimary) },
            text = { Text("This will permanently remove the encrypted key from Android KeyStore.", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteApiKey()
                        showDeleteConfirmDialog = false
                        editingKey = true
                        Toast.makeText(context, "API Key deleted", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        color = CardDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f))
                        .border(1.dp, iconTint.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}

