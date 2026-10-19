package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AssistantState
import com.example.ui.theme.OrbCenterNavy
import com.example.ui.theme.SoftCyan
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftLilac
import com.example.ui.theme.SoftSkyBlue
import com.example.ui.theme.SoftViolet

@Composable
fun BigMicButton(
    assistantState: AssistantState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MicWaveTransition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MicPulseScale"
    )

    val isListening = assistantState == AssistantState.LISTENING

    Box(
        modifier = modifier.size(92.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(SoftCyan.copy(alpha = 0.25f))
            )
        }

        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    when (assistantState) {
                        AssistantState.LISTENING -> Brush.linearGradient(listOf(SoftSkyBlue, SoftLilac))
                        AssistantState.SPEAKING -> Brush.linearGradient(listOf(SoftLilac, SoftViolet))
                        AssistantState.THINKING -> Brush.linearGradient(listOf(SoftLavender, SoftCyan))
                        AssistantState.IDLE -> Brush.linearGradient(listOf(SoftCyan, SoftLilac))
                    }
                )
                .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                .clickable { onClick() }
                .testTag("big_mic_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (assistantState) {
                    AssistantState.SPEAKING -> Icons.Default.Stop
                    AssistantState.THINKING -> Icons.Default.Refresh
                    else -> Icons.Default.Mic
                },
                contentDescription = "Voice Input",
                tint = OrbCenterNavy,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
