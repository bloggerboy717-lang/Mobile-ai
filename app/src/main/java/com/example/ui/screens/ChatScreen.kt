package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
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
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardElevatedDark
import com.example.ui.theme.CyberSky
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.MagentaGlow
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceDark
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
    var inputText by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(chatMessages.size, assistantState) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceDark)
    ) {
        // Chat Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardDark,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Alic AI Conversation",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (assistantState == AssistantState.SPEAKING) "Speaking response..." else "Powered by Google Gemini",
                        fontSize = 12.sp,
                        color = if (assistantState == AssistantState.SPEAKING) MagentaGlow else TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (assistantState == AssistantState.SPEAKING) {
                        IconButton(
                            onClick = { viewModel.stopSpeaking() },
                            modifier = Modifier.testTag("chat_header_stop_speaking_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Speaking",
                                tint = MagentaGlow
                            )
                        }
                    }

                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.testTag("chat_header_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Conversation",
                            tint = TextSecondary
                        )
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                // Top Welcome / Attribution notice
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark.copy(alpha = 0.5f))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Alic AI voice assistant developed by Rohit Raj",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            items(chatMessages, key = { it.id }) { message ->
                ChatBubble(
                    message = message,
                    isCurrentlySpeaking = assistantState == AssistantState.SPEAKING,
                    onRepeat = { viewModel.repeatMessage(message) },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Alic AI Message", message.text))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Thinking Bubble
            if (assistantState == AssistantState.THINKING) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardElevatedDark)
                                .border(1.dp, NeonPurple.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = CyberSky,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Alic AI is thinking...",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Bottom Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardDark,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Button
                IconButton(
                    onClick = { viewModel.onMicClicked() },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (assistantState == AssistantState.LISTENING) CyberSky else CardElevatedDark)
                        .testTag("chat_mic_button")
                ) {
                    Icon(
                        imageVector = if (assistantState == AssistantState.SPEAKING) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (assistantState == AssistantState.LISTENING) SpaceDark else CyberSky,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text Input Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input_field"),
                    placeholder = { Text("Ask Alic AI anything...", color = TextMuted, fontSize = 14.sp) },
                    maxLines = 3,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberSky,
                        unfocusedBorderColor = CardBorderDark,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = CyberSky
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.submitTypedPrompt(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) CyberSky else CardElevatedDark)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Message",
                        tint = if (inputText.isNotBlank()) SpaceDark else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Clear Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Conversation?", color = TextPrimary) },
            text = { Text("This will remove all current messages and reset the conversation history.", color = TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearConversation()
                        showClearDialog = false
                    },
                    modifier = Modifier.testTag("confirm_clear_conversation_button")
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
    isCurrentlySpeaking: Boolean,
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
            // Alic AI Avatar Icon
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(CyberSky, NeonPurple)))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.82f)
        ) {
            Surface(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp
                        )
                    )
                    .testTag("chat_bubble_${if (isUser) "user" else "alic"}_${message.id.take(6)}"),
                color = when {
                    message.isError -> Color(0x33EF4444)
                    isUser -> Color(0x2E38BDF8)
                    else -> CardElevatedDark
                },
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when {
                        message.isError -> ErrorRed.copy(alpha = 0.6f)
                        isUser -> CyberSky.copy(alpha = 0.5f)
                        else -> CardBorderDark
                    }
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        color = if (message.isError) ErrorRed else TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
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
                            color = TextMuted
                        )

                        if (!isUser && !message.isError) {
                            Row {
                                IconButton(
                                    onClick = onRepeat,
                                    modifier = Modifier.size(24.dp).testTag("repeat_tts_button_${message.id.take(6)}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Speak Again",
                                        tint = CyberSky,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = onCopy,
                                    modifier = Modifier.size(24.dp).testTag("copy_message_button_${message.id.take(6)}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Text",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
