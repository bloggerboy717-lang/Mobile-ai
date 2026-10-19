package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AlicViewModel
import com.example.data.AVAILABLE_GEMINI_MODELS
import com.example.data.ConnectionStatus
import com.example.data.VoicePersona
import com.example.data.VoiceSettings
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
    hasExplicitGenderVoices: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var editingKey by remember { mutableStateOf(!hasConfiguredKey) }
    var apiKeyInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceDark)
    ) {
        // Top Title
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardDark,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alic AI Settings",
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ==========================================
            // 1. GEMINI API KEY CONFIGURATION (MANDATORY)
            // ==========================================
            SettingsCard(
                title = "Gemini API Configuration",
                icon = Icons.Default.Key,
                iconTint = CyberSky
            ) {
                Text(
                    text = "Provide your Google Gemini API key to power voice intelligence. The key is encrypted locally using Android Keystore AES-GCM.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (hasConfiguredKey && !editingKey) {
                    // Display Masked Key View
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
                                Text(
                                    text = "SAVED API KEY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberSky,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = maskedApiKey,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = { editingKey = true },
                                    modifier = Modifier.testTag("edit_api_key_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Key",
                                        tint = CyberSky
                                    )
                                }
                                IconButton(
                                    onClick = { showDeleteConfirmDialog = true },
                                    modifier = Modifier.testTag("delete_api_key_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Key",
                                        tint = ErrorRed
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Editable Key Field
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it.trim() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_gemini_api_key_input"),
                        label = { Text("Gemini API Key", color = TextSecondary) },
                        placeholder = { Text("Enter AIzaSy... key", color = TextMuted) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                onClick = { passwordVisible = !passwordVisible },
                                modifier = Modifier.testTag("settings_toggle_key_visibility")
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Hide Key" else "Show Key",
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                            modifier = Modifier.weight(1f),
                            testTag = "settings_save_key_button"
                        )

                        if (hasConfiguredKey) {
                            TextButton(
                                onClick = {
                                    editingKey = false
                                    apiKeyInput = ""
                                }
                            ) {
                                Text("Cancel", color = TextSecondary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Connection Status Display
                ConnectionStatusBanner(status = connectionStatus)

                Spacer(modifier = Modifier.height(12.dp))

                // Test Connection Button
                OutlinedAnimatedButton(
                    text = "Test Gemini Connection",
                    icon = Icons.Default.NetworkCheck,
                    onClick = { viewModel.testCurrentConnection() },
                    enabled = hasConfiguredKey && connectionStatus !is ConnectionStatus.Testing,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "settings_test_connection_button"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Gemini Model Selection
                Text(
                    text = "SELECT GEMINI MODEL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonPurple,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                AVAILABLE_GEMINI_MODELS.forEach { model ->
                    val isSelected = model.id == selectedModel
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.selectModel(model.id) }
                            .testTag("model_option_${model.id}"),
                        color = if (isSelected) Color(0x2238BDF8) else CardElevatedDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CyberSky else CardBorderDark
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, if (isSelected) CyberSky else TextMuted, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(CyberSky)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = model.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) CyberSky else TextPrimary
                                )
                                Text(
                                    text = model.description,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. NEUTRAL VOICE SETTINGS (MANDATORY)
            // ==========================================
            SettingsCard(
                title = "Neutral Voice & Audio",
                icon = Icons.Default.RecordVoiceOver,
                iconTint = NeonPurple
            ) {
                Text(
                    text = "Alic AI uses Android Text-to-Speech to read responses aloud. A calm, natural, neutral voice is configured by default.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mute Spoken Replies Toggle
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
                        Column {
                            Text(
                                text = "Mute Spoken Replies",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (voiceSettings.muteSpokenReplies) "Assistant will only display text" else "Assistant reads responses aloud",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Switch(
                        checked = voiceSettings.muteSpokenReplies,
                        onCheckedChange = { isMuted ->
                            viewModel.updateVoiceSettings(voiceSettings.copy(muteSpokenReplies = isMuted))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberSky,
                            checkedTrackColor = CardBorderDark
                        ),
                        modifier = Modifier.testTag("mute_spoken_replies_switch")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Voice Persona Selector
                Text(
                    text = "VOICE STYLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberSky,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val personas = listOf(
                        Triple(VoicePersona.NEUTRAL, "Neutral (Default)", "Balanced natural voice"),
                        Triple(VoicePersona.FEMALE, "Female", "Available voices"),
                        Triple(VoicePersona.MALE, "Male", "Available voices")
                    )

                    personas.forEach { (persona, label, _) ->
                        val isSelected = voiceSettings.persona == persona
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0x33A855F7) else CardElevatedDark)
                                .border(1.dp, if (isSelected) NeonPurple else CardBorderDark, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.updateVoiceSettings(voiceSettings.copy(persona = persona))
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                                .testTag("voice_persona_${persona.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                if (!hasExplicitGenderVoices) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Note: Your device's TTS engine does not distinguish gender tags. Alic AI selects the most natural balanced voice available on your system.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Speech Rate Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Speech Rate", fontSize = 13.sp, color = TextPrimary)
                    Text(
                        text = String.format(java.util.Locale.US, "%.1fx", voiceSettings.speechRate),
                        fontSize = 13.sp,
                        color = CyberSky,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = voiceSettings.speechRate,
                    onValueChange = { newRate ->
                        viewModel.updateVoiceSettings(voiceSettings.copy(speechRate = newRate))
                    },
                    valueRange = 0.5f..2.0f,
                    steps = 14,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberSky,
                        activeTrackColor = CyberSky,
                        inactiveTrackColor = CardBorderDark
                    ),
                    modifier = Modifier.testTag("speech_rate_slider")
                )

                // Pitch Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Voice Pitch", fontSize = 13.sp, color = TextPrimary)
                    Text(
                        text = String.format(java.util.Locale.US, "%.1fx", voiceSettings.pitch),
                        fontSize = 13.sp,
                        color = NeonPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = voiceSettings.pitch,
                    onValueChange = { newPitch ->
                        viewModel.updateVoiceSettings(voiceSettings.copy(pitch = newPitch))
                    },
                    valueRange = 0.5f..1.5f,
                    steps = 9,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonPurple,
                        activeTrackColor = NeonPurple,
                        inactiveTrackColor = CardBorderDark
                    ),
                    modifier = Modifier.testTag("pitch_slider")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Test Voice Button
                OutlinedAnimatedButton(
                    text = "Test Voice Sample",
                    icon = Icons.Default.PlayArrow,
                    onClick = { viewModel.testVoice() },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "test_voice_sample_button"
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Spoken replies use native Android Text-to-Speech synthesis with neutral vocal tuning. Gemini generates the intelligent responses.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    lineHeight = 15.sp
                )
            }

            // ==========================================
            // 3. SECURITY & ARCHITECTURE (MANDATORY)
            // ==========================================
            SettingsCard(
                title = "Security & Keystore",
                icon = Icons.Default.Security,
                iconTint = SuccessGreen
            ) {
                Text(
                    text = "• Hardware-backed encryption: Your Gemini API key is encrypted using AES/GCM/NoPadding backed by the Android KeyStore before saving to private app storage.\n\n" +
                            "• Zero hardcoded credentials: No API keys are embedded into the APK source code or checked into git repositories.\n\n" +
                            "• Production Recommendation: For production deployments with broad consumer distribution, always route AI calls through a secure backend server with authentication rather than bundling raw API credentials on client devices.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }

            // ==========================================
            // 4. ABOUT & CREDITS
            // ==========================================
            SettingsCard(
                title = "About Alic AI",
                icon = Icons.Default.Info,
                iconTint = MagentaGlow
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Application", fontSize = 13.sp, color = TextMuted)
                        Text(text = "Alic AI Voice Assistant", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Developer", fontSize = 13.sp, color = TextMuted)
                        Text(text = "Rohit Raj", fontSize = 13.sp, color = CyberSky, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Intelligence", fontSize = 13.sp, color = TextMuted)
                        Text(text = "Google Gemini API", fontSize = 13.sp, color = TextPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Speech Engine", fontSize = 13.sp, color = TextMuted)
                        Text(text = "Android SpeechRecognizer & TTS", fontSize = 13.sp, color = TextPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Version", fontSize = 13.sp, color = TextMuted)
                        Text(text = "1.0.0", fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Saved API Key?", color = TextPrimary) },
            text = {
                Text(
                    "This will remove the encrypted Gemini API key from Android Keystore. You will need to re-enter an API key to use voice and chat intelligence.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteApiKey()
                        showDeleteConfirmDialog = false
                        editingKey = true
                        Toast.makeText(context, "API Key removed", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("confirm_delete_api_key_button")
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
