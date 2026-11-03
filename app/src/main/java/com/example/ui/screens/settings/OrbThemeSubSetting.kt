package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AlicViewModel
import com.example.data.ThemePreset
import com.example.ui.components.ThemeSelectionContent
import com.example.ui.theme.ThemeColors

@Composable
fun OrbThemeSubSetting(
    viewModel: AlicViewModel,
    currentThemePreset: ThemePreset,
    customHexColor: String,
    themeColors: ThemeColors,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Hacker Live Coding State
    val isHackerCodeRainEnabled by viewModel.isHackerCodeRainEnabled.collectAsState()
    val hackerCodeColorHex by viewModel.hackerCodeColorHex.collectAsState()
    val isAutoSaveDownloadsEnabled by viewModel.isAutoSaveDownloadsEnabled.collectAsState()
    val lastSavedHackerFile by viewModel.lastSavedHackerFile.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Explanatory Banner (100% English)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(themeColors.primaryAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = themeColors.primaryAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ORB & Theme Settings",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Customize the celestial planet ORB, theme colors, and live hacker coding screen background.",
                        fontSize = 11.sp,
                        color = themeColors.textSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // HACKER MATRIX LIVE CODING SECTION (User Requested)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, Color(0xFF00FF41).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00FF41).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = Color(0xFF00FF41),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hacker Live Coding Screen",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = themeColors.textPrimary
                            )
                            Text(
                                text = "Streams live hacker matrix code on Home screen",
                                fontSize = 11.sp,
                                color = themeColors.textSecondary
                            )
                        }
                    }

                    // Toggle Switch
                    Switch(
                        checked = isHackerCodeRainEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.setHackerCodeRainEnabled(enabled)
                            val msg = if (enabled) "Hacker code rain enabled on Home screen" else "Hacker code rain disabled"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00FF41),
                            checkedTrackColor = Color(0x3300FF41),
                            uncheckedThumbColor = themeColors.textMuted,
                            uncheckedTrackColor = themeColors.cardElevated
                        ),
                        modifier = Modifier.testTag("toggle_hacker_code_rain")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Coding Color Selector (Always green by default as requested: "वह ऑलवेज ग्रीन कलर में ही रहेगा। By default, the color is green.")
                Text(
                    text = "Coding Rain Color (Default: Hacker Green)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val greenShades = listOf(
                    "#00FF41" to "Matrix Green (Default)",
                    "#39FF14" to "Neon Cyber",
                    "#00E676" to "Emerald",
                    "#00F0FF" to "Quantum Cyan"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    greenShades.forEach { (hex, label) ->
                        val isSelected = hackerCodeColorHex.equals(hex, ignoreCase = true)
                        val color = try {
                            Color(android.graphics.Color.parseColor(hex))
                        } catch (e: Exception) {
                            Color(0xFF00FF41)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) color.copy(alpha = 0.20f) else themeColors.cardElevated)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) color else themeColors.cardBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    viewModel.setHackerCodeColorHex(hex)
                                    Toast.makeText(context, "$label selected", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(color),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (hex == "#00FF41") "Default" else label.split(" ").first(),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) color else themeColors.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto-Save to Downloads Folder Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(themeColors.cardElevated)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Save Code to Downloads",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.textPrimary
                        )
                        Text(
                            text = "Saves code script directly to Downloads/ALIC_Code/ on finish",
                            fontSize = 10.sp,
                            color = themeColors.textMuted
                        )
                    }

                    Switch(
                        checked = isAutoSaveDownloadsEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.setAutoSaveDownloadsEnabled(enabled)
                            val msg = if (enabled) "Auto-save to Downloads enabled" else "Auto-save disabled"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00FF41),
                            checkedTrackColor = Color(0x3300FF41),
                            uncheckedThumbColor = themeColors.textMuted,
                            uncheckedTrackColor = themeColors.cardBackground
                        ),
                        modifier = Modifier.testTag("toggle_auto_save_downloads")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Save to Downloads Button
                Button(
                    onClick = {
                        val savedFile = viewModel.saveHackerCodeToDownloads(context)
                        if (savedFile != null) {
                            Toast.makeText(context, "Saved to Downloads: $savedFile", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Code saved to Downloads/ALIC_Code/", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_hacker_code_downloads_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00FF41)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save Current Code to Downloads Now",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                if (lastSavedHackerFile != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Last Saved: Downloads/ALIC_Code/$lastSavedHackerFile",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF00FF41)
                    )
                }
            }
        }

        // Complete Theme Selection (Presets + Custom HEX)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                ThemeSelectionContent(
                    currentPreset = currentThemePreset,
                    currentCustomHex = customHexColor,
                    themeColors = themeColors,
                    onSelectPreset = { preset ->
                        viewModel.selectThemePreset(preset)
                        Toast.makeText(context, "${preset.title} theme applied", Toast.LENGTH_SHORT).show()
                    },
                    onApplyCustomHex = { hex ->
                        viewModel.setCustomThemeHex(hex)
                        Toast.makeText(context, "Custom theme applied ($hex)", Toast.LENGTH_SHORT).show()
                    },
                    showCloseButton = false,
                    showHandleBar = false,
                    scrollable = false
                )
            }
        }

        // ORB Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("orb_info_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = themeColors.cardElevated)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = themeColors.primaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Neural Planet ORB Features",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Central 'A' emblem with 3 dotted orbital planet rings.\n" +
                            "• Glows and pulses in sync with live speech and AI audio responses.\n" +
                            "• Kept clean and minimal on the main screen.",
                    fontSize = 11.sp,
                    color = themeColors.textSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
