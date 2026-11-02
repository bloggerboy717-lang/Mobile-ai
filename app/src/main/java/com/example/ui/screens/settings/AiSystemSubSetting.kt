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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
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
import com.example.ui.components.ConnectionStatusBanner
import com.example.ui.components.OutlinedAnimatedButton
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.ThemeColors

@Composable
fun AiSystemSubSetting(
    viewModel: AlicViewModel,
    hasConfiguredKey: Boolean,
    maskedApiKey: String,
    connectionStatus: ConnectionStatus,
    selectedModel: String,
    onNavigateToPermissions: () -> Unit,
    themeColors: ThemeColors,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var editingKey by remember { mutableStateOf(!hasConfiguredKey) }
    var apiKeyInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteApiKeyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // API Key Section (Compact, No Gaps)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = themeColors.primaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gemini API Key Configuration",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.textPrimary
                    )
                }

                Text(
                    text = "Encrypted locally using Android Keystore AES-GCM.",
                    fontSize = 11.sp,
                    color = themeColors.textSecondary
                )

                if (hasConfiguredKey && !editingKey) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        color = themeColors.cardElevated,
                        border = BorderStroke(1.dp, themeColors.cardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SAVED API KEY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.primaryAccent,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = maskedApiKey,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = themeColors.textPrimary
                                )
                            }
                            Row {
                                IconButton(onClick = { editingKey = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = themeColors.primaryAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(onClick = { showDeleteApiKeyDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = ErrorRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it.trim() },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Gemini API Key", color = themeColors.textSecondary) },
                        placeholder = { Text("Enter AIzaSy... key", color = themeColors.textMuted) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility",
                                    tint = themeColors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = themeColors.primaryAccent,
                            unfocusedBorderColor = themeColors.cardBorder,
                            focusedTextColor = themeColors.textPrimary,
                            unfocusedTextColor = themeColors.textPrimary,
                            focusedContainerColor = themeColors.cardElevated,
                            unfocusedContainerColor = themeColors.cardElevated,
                            cursorColor = themeColors.primaryAccent
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedAnimatedButton(
                            text = "Save API Key",
                            icon = Icons.Default.Key,
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
                                Text("Cancel", color = themeColors.textSecondary)
                            }
                        }
                    }
                }

                ConnectionStatusBanner(status = connectionStatus)

                OutlinedAnimatedButton(
                    text = "Test Gemini Connection",
                    icon = Icons.Default.NetworkCheck,
                    onClick = { viewModel.testCurrentConnection() },
                    enabled = hasConfiguredKey && connectionStatus !is ConnectionStatus.Testing,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Gemini Model Selector (Compact, No Gaps)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "ACTIVE GEMINI MODEL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.secondaryAccent,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                AVAILABLE_GEMINI_MODELS.forEach { model ->
                    val isSelected = model.id == selectedModel
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.selectModel(model.id) },
                        color = if (isSelected) themeColors.cardElevated else themeColors.cardBackground,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) themeColors.primaryAccent else themeColors.cardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        if (isSelected) themeColors.primaryAccent else themeColors.textMuted,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(themeColors.primaryAccent)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = model.displayName,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) themeColors.primaryAccent else themeColors.textPrimary
                                )
                                Text(
                                    text = model.description,
                                    fontSize = 11.sp,
                                    color = themeColors.textMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Permissions Dashboard Shortcut
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { onNavigateToPermissions() },
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Permissions Dashboard",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.textPrimary
                        )
                        Text(
                            text = "Microphone, Location & Contacts",
                            fontSize = 10.sp,
                            color = themeColors.textMuted
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = themeColors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Danger Zone: Clear App Data
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Reset Data",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Clear local chat history and student profile cache.",
                    fontSize = 11.sp,
                    color = themeColors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedAnimatedButton(
                    text = "Clear Conversation Data",
                    icon = Icons.Default.Delete,
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Delete API Key Dialog
        if (showDeleteApiKeyDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteApiKeyDialog = false },
                title = { Text("Delete API Key?", color = themeColors.textPrimary) },
                text = {
                    Text(
                        "Are you sure you want to remove your saved Gemini API key from secure storage?",
                        color = themeColors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteApiKey()
                        editingKey = true
                        showDeleteApiKeyDialog = false
                        Toast.makeText(context, "API Key removed", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Delete", color = ErrorRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteApiKeyDialog = false }) {
                        Text("Cancel", color = themeColors.textSecondary)
                    }
                },
                containerColor = themeColors.cardBackground
            )
        }

        // Clear All Data Confirm Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Clear All Data?", color = themeColors.textPrimary) },
                text = {
                    Text(
                        "Are you sure you want to reset all chats and delete local cached data? This cannot be undone.",
                        color = themeColors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.clearConversation()
                        showDeleteConfirmDialog = false
                        Toast.makeText(context, "All chats reset successfully", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Clear All", color = ErrorRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancel", color = themeColors.textSecondary)
                    }
                },
                containerColor = themeColors.cardBackground
            )
        }
    }
}
