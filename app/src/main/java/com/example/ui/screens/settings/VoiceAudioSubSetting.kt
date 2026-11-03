package com.example.ui.screens.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AlicViewModel
import com.example.data.VoiceLanguage
import com.example.data.VoiceSettings
import com.example.persona.ALL_PERSONAS
import com.example.persona.PersonaGender
import com.example.ui.components.OutlinedAnimatedButton
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.ThemeColors
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceAudioSubSetting(
    viewModel: AlicViewModel,
    voiceSettings: VoiceSettings,
    themeColors: ThemeColors,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentPersona by viewModel.currentPersona.collectAsState()

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleBackgroundListening(true)
            Toast.makeText(context, "Background listening enabled with notification", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification permission needed for background assistant", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Human-Like Gemini Voice Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SuccessGreen.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Official Gemini Human-Like Voices",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Conversational models with natural human intonation. Supports male and female personas in multiple Indian & international languages.",
                        fontSize = 11.sp,
                        color = themeColors.textPrimary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // 1. Language Selection (भोजपुरी, हिंदी, इंग्लिश, मराठी, तेलुगु, etc.)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = themeColors.primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Conversation Language",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select the language the AI should speak and reply in:",
                    fontSize = 12.sp,
                    color = themeColors.textSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VoiceLanguage.values().forEach { lang ->
                        val isSelected = voiceSettings.language == lang
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.selectVoiceLanguage(lang)
                                    Toast.makeText(
                                        context,
                                        "Language set to ${lang.displayName} (${lang.nativeName})",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .testTag("lang_chip_${lang.code}"),
                            color = if (isSelected) themeColors.primaryAccent else themeColors.cardElevated,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) themeColors.primaryAccent else themeColors.cardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Column {
                                    Text(
                                        text = lang.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else themeColors.textPrimary
                                    )
                                    Text(
                                        text = lang.nativeName,
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else themeColors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Voice Models List (Male & Female)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Headset,
                        contentDescription = null,
                        tint = themeColors.secondaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gemini Prebuilt Voice Models",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select your preferred voice persona (Male & Female models):",
                    fontSize = 12.sp,
                    color = themeColors.textSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                ALL_PERSONAS.forEach { persona ->
                    val isSelected = persona.id == currentPersona.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.switchPersona(persona)
                                Toast.makeText(context, "${persona.name} voice activated", Toast.LENGTH_SHORT).show()
                            }
                            .testTag("voice_persona_${persona.id.name.lowercase()}"),
                        color = if (isSelected) themeColors.cardElevated else themeColors.cardBackground,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) themeColors.primaryAccent else themeColors.cardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Brush.radialGradient(listOf(persona.primaryColor, persona.secondaryColor))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = persona.avatarLetter,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = persona.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                when (persona.gender) {
                                                    PersonaGender.MALE -> Color(0xFF1E88E5).copy(alpha = 0.2f)
                                                    PersonaGender.FEMALE -> Color(0xFFE91E63).copy(alpha = 0.2f)
                                                    else -> themeColors.primaryAccent.copy(alpha = 0.15f)
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = when (persona.gender) {
                                                PersonaGender.MALE -> "MALE VOICE"
                                                PersonaGender.FEMALE -> "FEMALE VOICE"
                                                else -> "NEUTRAL"
                                            },
                                            fontSize = 9.sp,
                                            color = when (persona.gender) {
                                                PersonaGender.MALE -> Color(0xFF64B5F6)
                                                PersonaGender.FEMALE -> Color(0xFFF48FB1)
                                                else -> themeColors.primaryAccent
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = persona.subtitle,
                                    fontSize = 11.sp,
                                    color = themeColors.textMuted
                                )
                            }
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(themeColors.primaryAccent)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Active",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Background Working & Listening
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = themeColors.primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Background Working & Listening",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                }

                Text(
                    text = "Keeps ALIC-AI continuously active and listening even when the app is minimized, running in the background, or while using other apps.",
                    fontSize = 12.sp,
                    color = themeColors.textSecondary,
                    lineHeight = 16.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(themeColors.cardElevated)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Background Listening Mode",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.textPrimary
                        )
                        Text(
                            text = if (voiceSettings.backgroundListeningEnabled) "Active in background" else "Disabled",
                            fontSize = 11.sp,
                            color = if (voiceSettings.backgroundListeningEnabled) SuccessGreen else themeColors.textMuted
                        )
                    }

                    Switch(
                        checked = voiceSettings.backgroundListeningEnabled,
                        onCheckedChange = { isEnabled ->
                            if (isEnabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.toggleBackgroundListening(true)
                                    Toast.makeText(context, "Background listening enabled", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                viewModel.toggleBackgroundListening(false)
                                Toast.makeText(context, "Background listening disabled", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = themeColors.primaryAccent,
                            checkedTrackColor = themeColors.cardBorder
                        ),
                        modifier = Modifier.testTag("bg_listening_switch")
                    )
                }

                // Battery Optimization Exemption button if user needs background persistence
                OutlinedAnimatedButton(
                    text = "Grant Battery Optimization Exemption",
                    icon = Icons.Default.Security,
                    onClick = {
                        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                            powerManager?.isIgnoringBatteryOptimizations(context.packageName) == false
                        ) {
                            try {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(fallbackIntent)
                            }
                        } else {
                            Toast.makeText(context, "Battery optimizations already exempt", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("grant_battery_opt_button")
                )
            }
        }

        // 4. Audio Preferences (Mute & Pitch only)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Audio Preferences",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )

                // Mute Spoken Responses Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(themeColors.cardElevated)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (voiceSettings.muteSpokenReplies) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = if (voiceSettings.muteSpokenReplies) WarningAmber else themeColors.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mute Spoken Responses",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.textPrimary
                        )
                    }
                    Switch(
                        checked = voiceSettings.muteSpokenReplies,
                        onCheckedChange = { isMuted ->
                            viewModel.updateVoiceSettings(voiceSettings.copy(muteSpokenReplies = isMuted))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = themeColors.primaryAccent,
                            checkedTrackColor = themeColors.cardBorder
                        ),
                        modifier = Modifier.testTag("mute_voice_switch")
                    )
                }

                // Voice Pitch Tuning
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Voice Pitch",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = themeColors.textPrimary
                        )
                        Text(
                            text = String.format(java.util.Locale.US, "%.2fx", voiceSettings.pitch),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.secondaryAccent
                        )
                    }
                    Slider(
                        value = voiceSettings.pitch,
                        onValueChange = { viewModel.updateVoiceSettings(voiceSettings.copy(pitch = it)) },
                        valueRange = 0.7f..1.4f,
                        colors = SliderDefaults.colors(
                            thumbColor = themeColors.secondaryAccent,
                            activeTrackColor = themeColors.secondaryAccent
                        ),
                        modifier = Modifier.testTag("voice_pitch_slider")
                    )
                }

                // Test Voice Button
                OutlinedAnimatedButton(
                    text = "Test Gemini Voice",
                    icon = Icons.Default.PlayArrow,
                    onClick = { viewModel.testVoice() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_voice_button")
                )
            }
        }
    }
}
