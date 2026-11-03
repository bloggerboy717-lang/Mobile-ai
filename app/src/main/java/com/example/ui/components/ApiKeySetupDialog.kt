package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ConnectionStatus
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardElevatedDark
import com.example.ui.theme.CyberSky
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun ApiKeySetupDialog(
    connectionStatus: ConnectionStatus,
    onSaveKey: (String) -> Unit,
    onTestConnection: () -> Unit,
    onDismiss: () -> Unit,
    canDismiss: Boolean = true
) {
    var apiKeyInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        val themeColors = com.example.ui.theme.AppTheme.colors
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(themeColors.primaryAccent.copy(alpha = 0.6f), themeColors.secondaryAccent.copy(alpha = 0.6f))),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("api_key_setup_dialog"),
            color = themeColors.cardBackground,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row with Title and Cut / Close ('X') Option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(listOf(themeColors.primaryAccent.copy(alpha = 0.35f), themeColors.secondaryAccent.copy(alpha = 0.2f)))
                                )
                                .border(1.dp, themeColors.primaryAccent.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "API Key",
                                tint = themeColors.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Configure Gemini API",
                                color = themeColors.textPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "AI Studio Key",
                                color = themeColors.primaryAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Cut/Close 'X' button requested by user
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(themeColors.cardElevated)
                            .border(1.dp, themeColors.cardBorder, CircleShape)
                            .testTag("dialog_cut_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cut / Close Dialog",
                            tint = themeColors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ALIC-AI uses high-performance Gemini API models for natural voice intelligence. You can configure your key now or in About ALIC / Settings.",
                    color = themeColors.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "• Keys are stored securely on-device with Android Keystore AES-GCM.\n• No API keys are hardcoded in the app.",
                    color = themeColors.textMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(themeColors.cardElevated, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Input Field
                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it.trim() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("setup_gemini_api_key_input"),
                    label = { Text("Gemini API Key", color = themeColors.textSecondary) },
                    placeholder = { Text("AIzaSy...", color = themeColors.textMuted) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisible = !passwordVisible },
                            modifier = Modifier.testTag("setup_toggle_visibility_button")
                        ) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide key" else "Show key",
                                tint = themeColors.textSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.primaryAccent,
                        unfocusedBorderColor = themeColors.cardBorder,
                        focusedTextColor = themeColors.textPrimary,
                        unfocusedTextColor = themeColors.textPrimary,
                        focusedContainerColor = themeColors.cardBackground,
                        unfocusedContainerColor = themeColors.cardBackground,
                        cursorColor = themeColors.primaryAccent
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Status Indicator Banner
                ConnectionStatusBanner(status = connectionStatus)

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedAnimatedButton(
                        text = "Save Key",
                        icon = Icons.Default.Save,
                        onClick = {
                            if (apiKeyInput.isNotBlank()) {
                                onSaveKey(apiKeyInput)
                            }
                        },
                        enabled = apiKeyInput.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        testTag = "setup_save_key_button"
                    )

                    OutlinedAnimatedButton(
                        text = "Test",
                        icon = Icons.Default.NetworkCheck,
                        onClick = {
                            if (apiKeyInput.isNotBlank()) {
                                onSaveKey(apiKeyInput)
                            } else {
                                onTestConnection()
                            }
                        },
                        enabled = apiKeyInput.isNotBlank() || connectionStatus !is ConnectionStatus.Untested,
                        modifier = Modifier.weight(0.9f),
                        testTag = "setup_test_key_button"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("setup_skip_button")
                ) {
                    Text("Configure Later in Settings", color = themeColors.textSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusBanner(
    status: ConnectionStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, icon, text, textColor) = when (status) {
        ConnectionStatus.Untested -> Tuple5(
            Color(0x1538BDF8),
            CyberSky.copy(alpha = 0.3f),
            Icons.Default.Key,
            "Key not yet tested",
            TextSecondary
        )
        ConnectionStatus.Testing -> Tuple5(
            Color(0x2238BDF8),
            CyberSky.copy(alpha = 0.6f),
            Icons.Default.Refresh,
            "Testing Gemini API connection...",
            CyberSky
        )
        is ConnectionStatus.Connected -> Tuple5(
            Color(0x2210B981),
            SuccessGreen.copy(alpha = 0.8f),
            Icons.Default.CheckCircle,
            "Connected (${status.latencyMs}ms) • ${status.model}",
            SuccessGreen
        )
        is ConnectionStatus.InvalidApiKey -> Tuple5(
            Color(0x22EF4444),
            ErrorRed.copy(alpha = 0.8f),
            Icons.Default.Warning,
            "Invalid API Key: ${status.message}",
            ErrorRed
        )
        is ConnectionStatus.RateLimit -> Tuple5(
            Color(0x22F59E0B),
            WarningAmber.copy(alpha = 0.8f),
            Icons.Default.Warning,
            "Rate Limit Reached: ${status.message}",
            WarningAmber
        )
        is ConnectionStatus.NetworkError -> Tuple5(
            Color(0x22EF4444),
            ErrorRed.copy(alpha = 0.7f),
            Icons.Default.Warning,
            "Network Error: ${status.message}",
            ErrorRed
        )
        is ConnectionStatus.GeneralError -> Tuple5(
            Color(0x22EF4444),
            ErrorRed.copy(alpha = 0.7f),
            Icons.Default.Warning,
            status.message,
            ErrorRed
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("connection_status_banner")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (status is ConnectionStatus.Testing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = CyberSky,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
