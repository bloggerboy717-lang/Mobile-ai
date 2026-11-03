package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AlicViewModel
import com.example.data.ConnectionStatus
import com.example.data.VoiceSettings
import com.example.ui.screens.settings.AiSystemSubSetting
import com.example.ui.screens.settings.OrbThemeSubSetting
import com.example.ui.screens.settings.SettingsSection
import com.example.ui.screens.settings.SosEmergencySubSetting
import com.example.ui.screens.settings.StudentSubSetting
import com.example.ui.screens.settings.VoiceAudioSubSetting
import com.example.ui.theme.ThemeColors

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
    // Current active Sub-Setting (null = Main Settings Menu)
    var activeSection by remember { mutableStateOf<SettingsSection?>(null) }

    // Intercept system Back button to exit sub-setting smoothly
    BackHandler(enabled = activeSection != null) {
        activeSection = null
    }

    // Dynamic App Theme State
    val themeColors by viewModel.themeColors.collectAsState()
    val currentThemePreset by viewModel.currentThemePreset.collectAsState()
    val customHexColor by viewModel.customHexColor.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        // TOP HEADER (100% English)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = themeColors.cardBackground,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeSection == null) {
                    // Main Settings Header
                    Column {
                        Text(
                            text = "Settings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.textPrimary
                        )
                        Text(
                            text = "Preferences and Intelligence",
                            fontSize = 11.sp,
                            color = themeColors.textSecondary
                        )
                    }
                } else {
                    // Sub-Setting Header with Back button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = { activeSection = null },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(themeColors.cardElevated)
                                .border(1.dp, themeColors.cardBorder, CircleShape)
                                .testTag("sub_setting_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Settings",
                                tint = themeColors.primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = activeSection!!.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.textPrimary
                        )
                    }
                }

                // Close 'X' button to return directly to Home screen
                IconButton(
                    onClick = { viewModel.switchTab(0) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(themeColors.cardElevated)
                        .border(1.dp, themeColors.cardBorder, CircleShape)
                        .testTag("settings_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Settings",
                        tint = themeColors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // CONTENT BODY (Tight spacing, no excessive gaps)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (activeSection == null) {
                // MAIN SETTINGS MENU: Clean cards with Logo in front, Name, and Arrow at end
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. ORB & Theme Settings
                    SettingCategoryCard(
                        section = SettingsSection.ORB_THEME,
                        themeColors = themeColors,
                        onClick = { activeSection = SettingsSection.ORB_THEME }
                    )

                    // 2. Student Profile
                    SettingCategoryCard(
                        section = SettingsSection.STUDENT_PROFILE,
                        themeColors = themeColors,
                        onClick = { activeSection = SettingsSection.STUDENT_PROFILE }
                    )

                    // 3. Voice Models
                    SettingCategoryCard(
                        section = SettingsSection.VOICE_AUDIO,
                        themeColors = themeColors,
                        onClick = { activeSection = SettingsSection.VOICE_AUDIO }
                    )

                    // 4. Emergency SOS
                    SettingCategoryCard(
                        section = SettingsSection.SOS_EMERGENCY,
                        themeColors = themeColors,
                        onClick = { activeSection = SettingsSection.SOS_EMERGENCY }
                    )

                    // 5. AI & Model Settings (Tight spacing directly following SOS)
                    SettingCategoryCard(
                        section = SettingsSection.AI_SYSTEM,
                        themeColors = themeColors,
                        onClick = { activeSection = SettingsSection.AI_SYSTEM }
                    )

                    // 6. Unified Permissions Center (Working permissions)
                    SettingCategoryCard(
                        section = SettingsSection.PERMISSIONS_CENTER,
                        themeColors = themeColors,
                        onClick = { onNavigateToPermissions() }
                    )

                    // 7. About ALIC-AI (Full guide, features & API Key instructions)
                    SettingCategoryCard(
                        section = SettingsSection.ABOUT_ALIC,
                        themeColors = themeColors,
                        onClick = { activeSection = SettingsSection.ABOUT_ALIC }
                    )
                }
            } else {
                // ACTIVE SUB-SETTING VIEW
                when (activeSection!!) {
                    SettingsSection.ORB_THEME -> {
                        OrbThemeSubSetting(
                            viewModel = viewModel,
                            currentThemePreset = currentThemePreset,
                            customHexColor = customHexColor,
                            themeColors = themeColors
                        )
                    }
                    SettingsSection.STUDENT_PROFILE -> {
                        StudentSubSetting(
                            viewModel = viewModel,
                            themeColors = themeColors
                        )
                    }
                    SettingsSection.VOICE_AUDIO -> {
                        VoiceAudioSubSetting(
                            viewModel = viewModel,
                            voiceSettings = voiceSettings,
                            themeColors = themeColors
                        )
                    }
                    SettingsSection.SOS_EMERGENCY -> {
                        SosEmergencySubSetting(
                            viewModel = viewModel,
                            themeColors = themeColors
                        )
                    }
                    SettingsSection.AI_SYSTEM -> {
                        AiSystemSubSetting(
                            viewModel = viewModel,
                            hasConfiguredKey = hasConfiguredKey,
                            maskedApiKey = maskedApiKey,
                            connectionStatus = connectionStatus,
                            selectedModel = selectedModel,
                            onNavigateToPermissions = onNavigateToPermissions,
                            themeColors = themeColors
                        )
                    }
                    SettingsSection.PERMISSIONS_CENTER -> {
                        LaunchedEffect(Unit) {
                            onNavigateToPermissions()
                            activeSection = null
                        }
                    }
                    SettingsSection.ABOUT_ALIC -> {
                        com.example.ui.screens.settings.AboutAlicSubSetting(
                            viewModel = viewModel,
                            themeColors = themeColors,
                            onOpenApiKeyDialog = { viewModel.openSetupDialog() }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Minimalist, elegant card representing each Sub-Setting:
 * Leading Logo -> Title -> Trailing Arrow
 */
@Composable
private fun SettingCategoryCard(
    section: SettingsSection,
    themeColors: ThemeColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(section.testTag),
        color = themeColors.cardBackground,
        border = BorderStroke(1.dp, themeColors.cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading Logo / Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(themeColors.primaryAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = themeColors.primaryAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Name / Title
            Text(
                text = section.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.textPrimary,
                modifier = Modifier.weight(1f)
            )

            // Trailing Chevron Arrow to enter inside
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Open ${section.title}",
                tint = themeColors.textMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
