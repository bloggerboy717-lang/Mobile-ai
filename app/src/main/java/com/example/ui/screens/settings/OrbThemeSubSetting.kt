package com.example.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
                        text = "ORB & App Theme Customization",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Selecting a theme dynamically restyles background, cards, buttons, and the central 'A' Planet ORB.",
                        fontSize = 11.sp,
                        color = themeColors.textSecondary,
                        lineHeight = 15.sp
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
