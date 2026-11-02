package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.codebuilder.CodeBuildStep
import com.example.ui.theme.CardBorderDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardElevatedDark
import com.example.ui.theme.CyberSky
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CodeRunningDialog(
    currentStep: CodeBuildStep,
    statusDetail: String,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gear_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gear_angle"
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(CyberSky.copy(alpha = 0.8f), NeonPurple.copy(alpha = 0.8f))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("code_running_dialog"),
            color = CardDark,
            tonalElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Spinning Gear Icon Header
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(CyberSky.copy(alpha = 0.3f), NeonPurple.copy(alpha = 0.15f))
                            )
                        )
                        .border(1.dp, CyberSky.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentStep == CodeBuildStep.COMPLETED) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = SuccessGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Gear",
                            tint = CyberSky,
                            modifier = Modifier
                                .size(32.dp)
                                .rotate(rotation)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (currentStep == CodeBuildStep.COMPLETED) "✅ तैयार है!" else "⚙️ Code Running...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentStep == CodeBuildStep.COMPLETED) SuccessGreen else TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Current Step Headline
                Text(
                    text = currentStep.displayNameHindi,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyberSky
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Detail Explanation
                Text(
                    text = statusDetail,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Sequential Step Tracker
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardElevatedDark, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StepRow(title = "कोड लिख रहा हूँ", isDone = currentStep.ordinal > CodeBuildStep.WRITING.ordinal, isCurrent = currentStep == CodeBuildStep.WRITING)
                    StepRow(title = "टेस्ट कर रहा हूँ", isDone = currentStep.ordinal > CodeBuildStep.TESTING.ordinal, isCurrent = currentStep == CodeBuildStep.TESTING)
                    StepRow(title = "गलती ठीक कर रहा हूँ", isDone = currentStep.ordinal > CodeBuildStep.FIXING.ordinal, isCurrent = currentStep == CodeBuildStep.FIXING)
                    StepRow(title = "सेव कर रहा हूँ", isDone = currentStep.ordinal > CodeBuildStep.SAVING.ordinal, isCurrent = currentStep == CodeBuildStep.SAVING)
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (currentStep != CodeBuildStep.COMPLETED) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = CyberSky,
                        trackColor = CardElevatedDark
                    )
                }
            }
        }
    }
}

@Composable
private fun StepRow(
    title: String,
    isDone: Boolean,
    isCurrent: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isDone -> SuccessGreen
                        isCurrent -> CyberSky
                        else -> CardBorderDark
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = SpaceDark,
                    modifier = Modifier.size(13.dp)
                )
            } else if (isCurrent) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    color = SpaceDark,
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isCurrent || isDone) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isDone -> SuccessGreen
                isCurrent -> TextPrimary
                else -> TextMuted
            }
        )
    }
}
