package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Web
import com.example.ui.components.CodeRunningDialog
import com.example.ui.components.ProjectPreviewDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AlicViewModel
import com.example.data.AssistantState
import com.example.data.ChatMessage
import com.example.data.MessageSender
import com.example.persona.ALL_PERSONAS
import com.example.persona.Persona
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: AlicViewModel,
    chatMessages: List<ChatMessage>,
    assistantState: AssistantState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val currentPersona by viewModel.currentPersona.collectAsState()
    val pendingConfirmation by viewModel.pendingConfirmationAction.collectAsState()
    val isBuildingCode by viewModel.isBuildingCode.collectAsState()
    val codeBuildStep by viewModel.codeBuildStep.collectAsState()
    val codeBuildStatusText by viewModel.codeBuildStatusText.collectAsState()
    val activeProject by viewModel.activeGeneratedProject.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()
    val themeColors by viewModel.themeColors.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(chatMessages.size, assistantState) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(themeColors.background)
    ) {
        // Chat Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = themeColors.cardBackground,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(currentPersona.primaryColor, currentPersona.secondaryColor))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentPersona.avatarLetter,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SpaceDark
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = currentPersona.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "ALIC Neural Intelligence",
                            fontSize = 10.sp,
                            color = CyberSky
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (assistantState == AssistantState.SPEAKING) {
                        IconButton(
                            onClick = { viewModel.stopSpeaking() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop", tint = MagentaGlow, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CardDark.copy(alpha = 0.6f))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Alic AI Agentic Conversation • Developed by Rohit Raj",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            items(chatMessages, key = { it.id }) { message ->
                ChatBubble(
                    message = message,
                    persona = currentPersona,
                    isCurrentlySpeaking = assistantState == AssistantState.SPEAKING,
                    themeColors = themeColors,
                    onRepeat = { viewModel.repeatMessage(message) },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Alic AI Message", message.text))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Pending Action Confirmation Card inside Chat
            if (pendingConfirmation != null) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp)),
                        color = CardElevatedDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSky)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "ACTION PENDING APPROVAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberSky, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = pendingConfirmation!!.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(text = pendingConfirmation!!.description, fontSize = 11.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedAnimatedButton(
                                    text = "Confirm & Execute",
                                    onClick = { viewModel.confirmPendingAction() },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedAnimatedButton(
                                    text = "Cancel",
                                    onClick = { viewModel.cancelPendingAction() },
                                    containerColor = Color(0x22EF4444),
                                    modifier = Modifier.weight(0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // Thinking State
            if (assistantState == AssistantState.THINKING) {
                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardElevatedDark)
                            .border(1.dp, NeonPurple.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(15.dp), color = CyberSky, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "${currentPersona.name} is planning...", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Suggestion Chips for Students
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val suggestions = listOf(
                "पोर्टफोलियो वेबसाइट बनाओ",
                "Class 10 Physics: Ohm's Law समझाओ",
                "Maths Doubt: Quadratic formula ट्रिक",
                "मेरा 7 दिनों का Study Planner बनाओ",
                "Pomodoro focus timer",
                "बचाओ बचाओ बचाओ (Test SOS)"
            )
            suggestions.forEach { text ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(themeColors.cardBackground)
                        .border(1.dp, themeColors.cardBorder, RoundedCornerShape(16.dp))
                        .clickable { viewModel.submitTypedPrompt(text) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = text, fontSize = 11.sp, color = themeColors.textSecondary)
                }
            }
        }

        // Bottom Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = themeColors.cardBackground,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.onMicClicked() },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (assistantState == AssistantState.LISTENING) themeColors.primaryAccent else themeColors.cardElevated)
                ) {
                    Icon(
                        imageVector = if (assistantState == AssistantState.SPEAKING) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (assistantState == AssistantState.LISTENING) Color.White else themeColors.primaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask ALIC-AI or type a question...", color = themeColors.textMuted, fontSize = 13.sp) },
                    maxLines = 3,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColors.primaryAccent,
                        unfocusedBorderColor = themeColors.cardBorder,
                        focusedTextColor = themeColors.textPrimary,
                        unfocusedTextColor = themeColors.textPrimary,
                        focusedContainerColor = themeColors.cardBackground,
                        unfocusedContainerColor = themeColors.cardBackground,
                        cursorColor = themeColors.primaryAccent
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.submitTypedPrompt(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) themeColors.primaryAccent else themeColors.cardElevated)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) Color.White else themeColors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Code Builder Running Progression Popup (Feature 16)
    if (isBuildingCode) {
        CodeRunningDialog(
            currentStep = codeBuildStep,
            statusDetail = codeBuildStatusText,
            onDismiss = {}
        )
    }

    // In-App Live Preview Dialog (Feature 16)
    if (activeProject != null) {
        ProjectPreviewDialog(
            project = activeProject!!,
            onDismiss = { viewModel.dismissProjectPreview() }
        )
    }

    // Clear Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Conversation?", color = TextPrimary) },
            text = { Text("This will reset your chat history with ALIC-AI.", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearConversation()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear All", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    persona: Persona,
    isCurrentlySpeaking: Boolean,
    themeColors: com.example.ui.theme.ThemeColors,
    onRepeat: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == MessageSender.USER
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormatter.format(Date(message.timestamp)) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(persona.primaryColor, persona.secondaryColor))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = persona.avatarLetter,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.84f)
        ) {
            Surface(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    ),
                color = when {
                    message.isError -> Color(0x33EF4444)
                    isUser -> themeColors.primaryAccent.copy(alpha = if (themeColors.isDark) 0.22f else 0.14f)
                    else -> themeColors.cardElevated
                },
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when {
                        message.isError -> ErrorRed.copy(alpha = 0.6f)
                        isUser -> themeColors.primaryAccent.copy(alpha = 0.5f)
                        else -> themeColors.cardBorder
                    }
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        color = if (message.isError) ErrorRed else themeColors.textPrimary,
                        fontSize = 14.sp,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedTime,
                            fontSize = 10.sp,
                            color = themeColors.textMuted
                        )

                        if (!isUser && !message.isError) {
                            Row {
                                IconButton(
                                    onClick = onRepeat,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Repeat", tint = themeColors.primaryAccent, modifier = Modifier.size(15.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = onCopy,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = themeColors.textSecondary, modifier = Modifier.size(15.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
