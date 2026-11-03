package com.example.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.AlicViewModel
import com.example.ui.theme.ThemeColors

/**
 * About ALIC-AI Screen:
 * - Comprehensive explanation of ALIC-AI
 * - Step-by-step instructions on how to use it
 * - Complete feature list
 * - Complete guide on where & how to obtain the Gemini API Key
 */
@Composable
fun AboutAlicSubSetting(
    viewModel: AlicViewModel,
    themeColors: ThemeColors,
    onOpenApiKeyDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HERO APP IDENTITY CARD
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        themeColors.primaryAccent.copy(alpha = 0.6f),
                        themeColors.secondaryAccent.copy(alpha = 0.4f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "ALIC AI Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, themeColors.primaryAccent, CircleShape)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "ALIC-AI",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )

                Text(
                    text = "Autonomous Linguistic Intelligent Core",
                    fontSize = 12.sp,
                    color = themeColors.primaryAccent,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Version 2.4.0 • Student & Personal AI Engine",
                    fontSize = 11.sp,
                    color = themeColors.textMuted
                )
            }
        }

        // 2. WHERE & HOW TO GET API KEY (Prominent Guide requested by user)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, themeColors.primaryAccent.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(themeColors.primaryAccent.copy(alpha = 0.15f)),
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
                            text = "How to Get Free API Key",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.textPrimary
                        )
                        Text(
                            text = "Official AI Studio Key Setup",
                            fontSize = 11.sp,
                            color = themeColors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ApiKeyStepItem(
                    step = "1",
                    title = "Open Official AI Studio Portal",
                    description = "Tap the button below to visit https://aistudio.google.com/app/apikey on your mobile browser.",
                    themeColors = themeColors
                )

                ApiKeyStepItem(
                    step = "2",
                    title = "Sign In with your Account",
                    description = "Log in with your existing email account (free tier available with generous rate limits).",
                    themeColors = themeColors
                )

                ApiKeyStepItem(
                    step = "3",
                    title = "Create and Copy Key",
                    description = "Tap 'Create API Key', select or create a project, then tap 'Copy' on the generated key string.",
                    themeColors = themeColors
                )

                ApiKeyStepItem(
                    step = "4",
                    title = "Paste into ALIC-AI",
                    description = "Tap 'Configure API Key Now' below, paste your key, and tap 'Save Key'. You're ready to talk!",
                    themeColors = themeColors
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://aistudio.google.com/app/apikey")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_ai_studio_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.primaryAccent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (themeColors.isDark) Color(0xFF0F172A) else Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Get Key Online",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (themeColors.isDark) Color(0xFF0F172A) else Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenApiKeyDialog,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("enter_api_key_dialog_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, themeColors.primaryAccent)
                    ) {
                        Text(
                            text = "Enter Key Now",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.primaryAccent
                        )
                    }
                }
            }
        }

        // 3. WHAT IS ALIC-AI?
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "What is ALIC-AI?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ALIC-AI is a next-generation autonomous AI companion engineered for students and personal productivity. It combines ultra-fast neural language models, natural human voice synthesis, camera visual perception, hands-free device automation, and continuous background listening into a single unified companion.",
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = themeColors.textSecondary
                )
            }
        }

        // 4. HOW TO USE ALIC-AI
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "How to Use ALIC-AI",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                HowToUseItem(
                    icon = Icons.Default.Mic,
                    title = "Center ORB Voice Control",
                    description = "Tap the glowing center ORB on the Home screen or simply speak hands-free. ALIC-AI listens continuously without beeps or cutoffs.",
                    themeColors = themeColors
                )

                HowToUseItem(
                    icon = Icons.Default.Language,
                    title = "Multilingual Conversations",
                    description = "Choose your preferred language under Settings > Voice Models (Bhojpuri, Hindi, Marathi, Telugu, Tamil, Bengali, Gujarati, Punjabi, or English).",
                    themeColors = themeColors
                )

                HowToUseItem(
                    icon = Icons.Default.PhoneAndroid,
                    title = "Hands-Free Phone Actions",
                    description = "Say 'Play Arijit Singh on YouTube', 'Open Maps to Delhi', or 'Open Calculator'. ALIC executes the command automatically.",
                    themeColors = themeColors
                )

                HowToUseItem(
                    icon = Icons.Default.CameraAlt,
                    title = "Vision Doubt & Homework Solver",
                    description = "Point the camera at math problems, science diagrams, or textbook questions to get instant, step-by-step explanations.",
                    themeColors = themeColors
                )

                HowToUseItem(
                    icon = Icons.Default.Call,
                    title = "Voice Call Answering & Ending",
                    description = "Say 'फोन उठाओ' (Answer call) or 'फोन काटो' (End call) hands-free even when doing other activities.",
                    themeColors = themeColors
                )

                HowToUseItem(
                    icon = Icons.Default.Email,
                    title = "SMS Messages & Live Read Aloud",
                    description = "Say 'मैसेज भेजो' to send text messages, 'मैसेज पढ़ो' to check inbox, and ALIC automatically announces incoming SMS out loud when they arrive ('जब मैसेज आए तो पढ़कर बताना').",
                    themeColors = themeColors
                )

                HowToUseItem(
                    icon = Icons.Default.Mic,
                    title = "Whisper & Soft-Speech Recognition",
                    description = "Speak as quietly as you need — ALIC's high-gain sensitivity accurately captures soft whispers and low-volume voices.",
                    themeColors = themeColors
                )

                HowToUseItem(
                    icon = Icons.Default.Shield,
                    title = "Emergency SOS Protection",
                    description = "Activate Emergency SOS to instantly trigger a loud warning beacon and transmit your live GPS coordinates to emergency contacts.",
                    themeColors = themeColors
                )
            }
        }

        // 5. CORE FEATURES CATALOG
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = themeColors.cardBackground,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Features at a Glance",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.textPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                FeatureBadge(title = "Human-Like Neural Voices", detail = "Natural male & female models (Charon, Aoede, Puck, Fenrir, Kore)", themeColors = themeColors)
                FeatureBadge(title = "Real-Time Background Execution", detail = "Listens and responds even when the phone is locked or other apps are open", themeColors = themeColors)
                FeatureBadge(title = "Local Keystore Encryption", detail = "API keys are encrypted locally on-device with AES-GCM", themeColors = themeColors)
                FeatureBadge(title = "Zero Audio Artifacts", detail = "Silent mic triggers with smooth natural transitions", themeColors = themeColors)
                FeatureBadge(title = "Dynamic Theming", detail = "Classic Cyan, Neon Magenta, Earth Emerald, Sunset Amber, Dark & Light palettes", themeColors = themeColors)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ApiKeyStepItem(
    step: String,
    title: String,
    description: String,
    themeColors: ThemeColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(themeColors.primaryAccent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = themeColors.primaryAccent
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.textPrimary
            )
            Text(
                text = description,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = themeColors.textSecondary
            )
        }
    }
}

@Composable
private fun HowToUseItem(
    icon: ImageVector,
    title: String,
    description: String,
    themeColors: ThemeColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(themeColors.cardElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = themeColors.primaryAccent,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = themeColors.textSecondary
            )
        }
    }
}

@Composable
private fun FeatureBadge(
    title: String,
    detail: String,
    themeColors: ThemeColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .background(themeColors.cardElevated, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.primaryAccent
        )
        Text(
            text = detail,
            fontSize = 11.sp,
            color = themeColors.textMuted
        )
    }
}
