package com.example.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Clean, minimalistic Sub-Setting categories in ALIC-AI.
 * Formatted with icon and concise English title.
 */
enum class SettingsSection(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    ORB_THEME(
        title = "ORB & Theme Settings",
        icon = Icons.Default.Public,
        testTag = "setting_item_orb_theme"
    ),
    STUDENT_PROFILE(
        title = "Student Profile",
        icon = Icons.Default.School,
        testTag = "setting_item_student_profile"
    ),
    VOICE_AUDIO(
        title = "Voice Models",
        icon = Icons.Default.RecordVoiceOver,
        testTag = "setting_item_voice_audio"
    ),
    SOS_EMERGENCY(
        title = "Emergency SOS",
        icon = Icons.Default.Shield,
        testTag = "setting_item_sos_emergency"
    ),
    AI_SYSTEM(
        title = "AI & Model Settings",
        icon = Icons.Default.Psychology,
        testTag = "setting_item_ai_system"
    )
}
