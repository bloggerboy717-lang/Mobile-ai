package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.AssistantState
import com.example.ui.theme.CyberSky
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.MagentaGlow
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.OrbCyan
import com.example.ui.theme.OrbPurple

@Composable
fun GlowingOrb(
    state: AssistantState,
    audioRms: Float = 0f,
    size: Dp = 220.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbTransitions")

    // Idle breathing pulse
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdlePulse"
    )

    // Continuous rotation for Thinking state
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    // Speaking wave expansion
    val speakingWave by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SpeakingWave"
    )

    // Dynamic scale factor based on state and microphone volume
    val scaleFactor = when (state) {
        AssistantState.IDLE -> idlePulse
        AssistantState.LISTENING -> 1.0f + (audioRms * 0.35f)
        AssistantState.THINKING -> 1.05f
        AssistantState.SPEAKING -> speakingWave
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension / 2.7f) * scaleFactor

            // Outer atmospheric halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        when (state) {
                            AssistantState.LISTENING -> OrbCyan.copy(alpha = 0.35f)
                            AssistantState.THINKING -> NeonPurple.copy(alpha = 0.40f)
                            AssistantState.SPEAKING -> MagentaGlow.copy(alpha = 0.45f)
                            AssistantState.IDLE -> NeonPurple.copy(alpha = 0.20f)
                        },
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.55f
                ),
                radius = baseRadius * 1.55f,
                center = center
            )

            // Outer orbital energy rings
            when (state) {
                AssistantState.THINKING -> {
                    rotate(rotation, pivot = center) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(OrbCyan, NeonPurple, MagentaGlow, OrbCyan),
                                center = center
                            ),
                            startAngle = 0f,
                            sweepAngle = 260f,
                            useCenter = false,
                            topLeft = Offset(center.x - baseRadius * 1.28f, center.y - baseRadius * 1.28f),
                            size = androidx.compose.ui.geometry.Size(baseRadius * 2.56f, baseRadius * 2.56f),
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(MagentaGlow, CyberSky, NeonPurple),
                                center = center
                            ),
                            startAngle = 180f,
                            sweepAngle = 140f,
                            useCenter = false,
                            topLeft = Offset(center.x - baseRadius * 1.45f, center.y - baseRadius * 1.45f),
                            size = androidx.compose.ui.geometry.Size(baseRadius * 2.9f, baseRadius * 2.9f),
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
                AssistantState.LISTENING -> {
                    // Pulsing acoustic wave rings
                    drawCircle(
                        color = OrbCyan.copy(alpha = 0.45f),
                        radius = baseRadius * (1.15f + audioRms * 0.25f),
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawCircle(
                        color = CyberSky.copy(alpha = 0.25f),
                        radius = baseRadius * (1.35f + audioRms * 0.4f),
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                AssistantState.SPEAKING -> {
                    drawCircle(
                        color = MagentaGlow.copy(alpha = 0.4f),
                        radius = baseRadius * 1.22f,
                        center = center,
                        style = Stroke(width = 3.5.dp.toPx())
                    )
                    drawCircle(
                        color = OrbPurple.copy(alpha = 0.3f),
                        radius = baseRadius * 1.40f,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                AssistantState.IDLE -> {
                    drawCircle(
                        color = CyberSky.copy(alpha = 0.25f),
                        radius = baseRadius * 1.18f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }

            // Core Glowing Orb Gradient
            val coreColors = when (state) {
                AssistantState.LISTENING -> listOf(
                    Color.White,
                    OrbCyan,
                    CyberSky,
                    DeepViolet
                )
                AssistantState.THINKING -> listOf(
                    Color.White,
                    OrbPurple,
                    NeonPurple,
                    DeepViolet
                )
                AssistantState.SPEAKING -> listOf(
                    Color.White,
                    MagentaGlow,
                    NeonPurple,
                    OrbCyan
                )
                AssistantState.IDLE -> listOf(
                    Color.White.copy(alpha = 0.9f),
                    CyberSky,
                    NeonPurple,
                    DeepViolet
                )
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = coreColors,
                    center = Offset(center.x - baseRadius * 0.25f, center.y - baseRadius * 0.25f),
                    radius = baseRadius
                ),
                radius = baseRadius,
                center = center
            )

            // Inner speculative light sheen
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.65f), Color.Transparent),
                    center = Offset(center.x - baseRadius * 0.35f, center.y - baseRadius * 0.35f),
                    radius = baseRadius * 0.5f
                ),
                radius = baseRadius * 0.5f,
                center = Offset(center.x - baseRadius * 0.35f, center.y - baseRadius * 0.35f)
            )
        }
    }
}
