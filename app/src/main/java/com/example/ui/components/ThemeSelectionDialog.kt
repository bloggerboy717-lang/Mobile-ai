package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ThemePreset
import com.example.ui.theme.ThemeColors


@Composable
fun ThemeSelectionDialog(
    currentPreset: ThemePreset,
    currentCustomHex: String,
    themeColors: ThemeColors,
    onSelectPreset: (ThemePreset) -> Unit,
    onApplyCustomHex: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, themeColors.cardBorder, RoundedCornerShape(28.dp)),
            color = themeColors.cardBackground,
            shadowElevation = 16.dp
        ) {
            ThemeSelectionContent(
                currentPreset = currentPreset,
                currentCustomHex = currentCustomHex,
                themeColors = themeColors,
                onSelectPreset = { preset ->
                    onSelectPreset(preset)
                },
                onApplyCustomHex = { hex ->
                    onApplyCustomHex(hex)
                },
                scrollable = true,
                showHandleBar = true,
                showCloseButton = true,
                onClose = onDismiss
            )
        }
    }
}

@Composable
fun ThemeSelectionContent(
    currentPreset: ThemePreset,
    currentCustomHex: String,
    themeColors: ThemeColors,
    onSelectPreset: (ThemePreset) -> Unit,
    onApplyCustomHex: (String) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    showHandleBar: Boolean = false,
    showCloseButton: Boolean = false,
    onClose: (() -> Unit)? = null
) {
    var hexInput by remember(currentCustomHex) { mutableStateOf(currentCustomHex) }

    val presetSwatches = listOf(
        "#121212", // Obsidian Black
        "#262E3D", // Slate
        "#4D6A65", // Sage Forest
        "#8E6E60", // Warm Sepia
        "#FFFFFF", // Crisp White
        "#FFF4EC", // Sunset Peach
        "#FFE4E6", // Rose Blush
        "#F3E8FF"  // Lavender
    )

    val contentModifier = if (scrollable) {
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    } else {
        modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
    }

    Column(modifier = contentModifier) {
        if (showHandleBar) {
            // Handle bar indicator
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(themeColors.textMuted.copy(alpha = 0.35f))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Select App Theme",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Restyles the entire app and the neural orb",
                    fontSize = 12.sp,
                    color = themeColors.textSecondary
                )
            }

            if (showCloseButton && onClose != null) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(themeColors.cardElevated)
                        .border(1.dp, themeColors.cardBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = themeColors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preset 1: Light
        ThemeOptionCard(
            title = "Light",
            subtitle = "Crisp studio white · Blue & Violet accents",
            previewColor = Color(0xFFF8FAFC),
            borderPreviewColor = Color(0xFFE2E8F0),
            isSelected = currentPreset == ThemePreset.LIGHT,
            themeColors = themeColors,
            onClick = { onSelectPreset(ThemePreset.LIGHT) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Preset 2: Dark
        ThemeOptionCard(
            title = "Dark",
            subtitle = "Deep midnight cosmic · Neon cyan & purple",
            previewColor = Color(0xFF475569),
            borderPreviewColor = Color(0xFF64748B),
            isSelected = currentPreset == ThemePreset.DARK,
            themeColors = themeColors,
            onClick = { onSelectPreset(ThemePreset.DARK) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Preset 3: Red
        ThemeOptionCard(
            title = "Red",
            subtitle = "Obsidian crimson · Laser coral & flame",
            previewColor = Color(0xFF8E6B74),
            borderPreviewColor = Color(0xFFA17C85),
            isSelected = currentPreset == ThemePreset.RED,
            themeColors = themeColors,
            onClick = { onSelectPreset(ThemePreset.RED) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Preset 4: Sunset (Active preset matching screenshot)
        ThemeOptionCard(
            title = "Sunset",
            subtitle = "Warm pastel peach · Coral & magenta twilight",
            previewColor = Color(0xFFFFD7C2),
            borderPreviewColor = Color(0xFFFF6B4A),
            isSelected = currentPreset == ThemePreset.SUNSET,
            themeColors = themeColors,
            onClick = { onSelectPreset(ThemePreset.SUNSET) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Preset 5: Emerald
        ThemeOptionCard(
            title = "Emerald",
            subtitle = "Obsidian jade · Neon mint & matrix green",
            previewColor = Color(0xFF1E5240),
            borderPreviewColor = Color(0xFF10B981),
            isSelected = currentPreset == ThemePreset.EMERALD,
            themeColors = themeColors,
            onClick = { onSelectPreset(ThemePreset.EMERALD) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Preset 6: Custom Background
        val isCustomSelected = currentPreset == ThemePreset.CUSTOM
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable { onSelectPreset(ThemePreset.CUSTOM) },
            color = if (isCustomSelected) themeColors.cardElevated else themeColors.cardBackground,
            border = BorderStroke(
                width = if (isCustomSelected) 1.5.dp else 1.dp,
                color = if (isCustomSelected) themeColors.primaryAccent else themeColors.cardBorder
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                try {
                                    val clean = hexInput.removePrefix("#")
                                    Color(clean.toLong(16).toInt() or (0xFF shl 24))
                                } catch (e: Exception) {
                                    Color.White
                                }
                            )
                            .border(1.dp, themeColors.cardBorder, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Custom Background",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.textPrimary
                        )
                        Text(
                            text = "Choose any HEX colour (#RRGGBB)",
                            fontSize = 12.sp,
                            color = themeColors.textSecondary
                        )
                    }

                    if (isCustomSelected) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(themeColors.primaryAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input + Apply button (styled as in user's screenshot)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { hexInput = it.uppercase() },
                        singleLine = true,
                        placeholder = { Text("#FFFFFF", color = themeColors.textMuted) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.primaryAccent,
                            unfocusedBorderColor = themeColors.cardBorder,
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedContainerColor = themeColors.cardBackground,
                            unfocusedContainerColor = themeColors.cardBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            if (hexInput.isNotBlank()) {
                                onApplyCustomHex(hexInput)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B4A), // Coral orange Apply button from screenshot
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("Apply", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Color swatch quick pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    presetSwatches.forEach { hex ->
                        val swatchColor = try {
                            val clean = hex.removePrefix("#")
                            Color(clean.toLong(16).toInt() or (0xFF shl 24))
                        } catch (e: Exception) {
                            Color.Gray
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(
                                    1.5.dp,
                                    if (hexInput.equals(hex, ignoreCase = true)) themeColors.primaryAccent else themeColors.cardBorder,
                                    CircleShape
                                )
                                .clickable {
                                    hexInput = hex
                                    onApplyCustomHex(hex)
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ThemeOptionCard(
    title: String,
    subtitle: String,
    previewColor: Color,
    borderPreviewColor: Color,
    isSelected: Boolean,
    themeColors: ThemeColors,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("theme_card_$title"),
        color = if (isSelected) themeColors.cardElevated else themeColors.cardBackground,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) themeColors.primaryAccent else themeColors.cardBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Preview Swatch
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(previewColor)
                    .border(1.dp, borderPreviewColor, CircleShape)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = themeColors.textSecondary
                )
            }

            if (isSelected) {
                // Checkbox matching screenshot (Coral/Orange filled box with white checkmark)
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFF6B4A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
