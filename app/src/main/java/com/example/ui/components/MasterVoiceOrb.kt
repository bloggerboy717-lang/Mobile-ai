package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.AssistantState
import com.example.ui.theme.DeepNavyBackground
import com.example.ui.theme.OrbCenterNavy
import com.example.ui.theme.SoftCyan
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftLilac
import com.example.ui.theme.SoftSkyBlue
import com.example.ui.theme.SoftViolet

/**
 * MasterVoiceOrb matches the exact visual design from the provided PNG photo:
 * - Deep navy atmospheric backdrop
 * - Soft cyan-to-lavender gradient disc (#4CA9FF -> #BC7CFF)
 * - Precision dark silhouette microphone icon in the center
 * - Cyan sound wave arc bracket on the left (#54B3FF)
 * - Lavender sound wave arc bracket on the right (#C878FF)
 * - Dynamic pulsation, audio-reactive RMS waves, and state responsiveness
 */
@Composable
fun MasterVoiceOrb(
    assistantState: AssistantState,
    audioRms: Float = 0f,
    onClick: () -> Unit,
    size: Dp = 230.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MasterOrbTransitions")

    // Subtle breathing pulse for idle
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdlePulse"
    )

    // Wave arc pulse
    val arcPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ArcPulse"
    )

    // Speaking glow pulse
    val speakingPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SpeakingPulse"
    )

    // Thinking rotation angle
    val thinkingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ThinkingRotation"
    )

    val currentScale = when (assistantState) {
        AssistantState.IDLE -> idlePulse
        AssistantState.LISTENING -> (1.0f + (audioRms.coerceIn(0f, 1f) * 0.18f))
        AssistantState.THINKING -> 1.02f
        AssistantState.SPEAKING -> speakingPulse
    }

    val arcScale = when (assistantState) {
        AssistantState.LISTENING -> (1.0f + (audioRms.coerceIn(0f, 1f) * 0.32f))
        AssistantState.SPEAKING -> speakingPulse
        else -> arcPulse
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag("master_voice_orb"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val discRadius = (this.size.minDimension * 0.285f) * currentScale
            val arcRadius = (this.size.minDimension * 0.40f) * arcScale

            // 1. Soft atmospheric outer radial halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        when (assistantState) {
                            AssistantState.LISTENING -> SoftCyan.copy(alpha = 0.35f)
                            AssistantState.THINKING -> SoftLavender.copy(alpha = 0.35f)
                            AssistantState.SPEAKING -> SoftLilac.copy(alpha = 0.40f)
                            AssistantState.IDLE -> SoftSkyBlue.copy(alpha = 0.20f)
                        },
                        Color.Transparent
                    ),
                    center = center,
                    radius = discRadius * 1.6f
                ),
                radius = discRadius * 1.6f,
                center = center
            )

            // 2. Left and Right Curved Sound Wave Brackets (Exactly matching PNG)
            val strokeThickness = 12.dp.toPx()

            // Left Sound Wave Arc: Cyan gradient (#4CA9FF to #5AB6FF)
            val leftArcRect = Rect(
                center = center,
                radius = arcRadius
            )
            drawArc(
                brush = Brush.verticalGradient(
                    colors = listOf(SoftSkyBlue, SoftCyan)
                ),
                startAngle = 142f,
                sweepAngle = 76f,
                useCenter = false,
                topLeft = leftArcRect.topLeft,
                size = leftArcRect.size,
                style = Stroke(width = strokeThickness, cap = StrokeCap.Round)
            )

            // Right Sound Wave Arc: Lavender gradient (#BC7CFF to #C788FF)
            val rightArcRect = Rect(
                center = center,
                radius = arcRadius
            )
            drawArc(
                brush = Brush.verticalGradient(
                    colors = listOf(SoftLilac, SoftViolet)
                ),
                startAngle = 322f,
                sweepAngle = 76f,
                useCenter = false,
                topLeft = rightArcRect.topLeft,
                size = rightArcRect.size,
                style = Stroke(width = strokeThickness, cap = StrokeCap.Round)
            )

            // Extra acoustic echo rings when listening or speaking
            if (assistantState == AssistantState.LISTENING && audioRms > 0.15f) {
                val echoRadius = arcRadius + (audioRms * 18.dp.toPx())
                val echoRect = Rect(center = center, radius = echoRadius)
                drawArc(
                    color = SoftCyan.copy(alpha = (audioRms * 0.5f).coerceIn(0.1f, 0.4f)),
                    startAngle = 146f,
                    sweepAngle = 68f,
                    useCenter = false,
                    topLeft = echoRect.topLeft,
                    size = echoRect.size,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = SoftLilac.copy(alpha = (audioRms * 0.5f).coerceIn(0.1f, 0.4f)),
                    startAngle = 326f,
                    sweepAngle = 68f,
                    useCenter = false,
                    topLeft = echoRect.topLeft,
                    size = echoRect.size,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 3. Central Gradient Disc (Soft Cyan top-left to Soft Lavender bottom-right)
            val discTopLeft = Offset(center.x - discRadius, center.y - discRadius)
            val discBottomRight = Offset(center.x + discRadius, center.y + discRadius)

            drawCircle(
                brush = Brush.linearGradient(
                    colors = when (assistantState) {
                        AssistantState.SPEAKING -> listOf(
                            Color(0xFF6BB5FF),
                            SoftLilac,
                            Color(0xFFE879F9)
                        )
                        AssistantState.THINKING -> listOf(
                            SoftLavender,
                            SoftSkyBlue,
                            SoftViolet
                        )
                        else -> listOf(
                            Color(0xFF5AB6FF), // Soft Cyan-Blue
                            Color(0xFF7FA2FF), // Transition Azure
                            Color(0xFFB57DFF)  // Soft Lavender-Violet
                        )
                    },
                    start = discTopLeft,
                    end = discBottomRight
                ),
                radius = discRadius,
                center = center
            )

            // Inner bevel rim highlight (soft 3D depth matching PNG)
            drawCircle(
                color = Color.White.copy(alpha = 0.28f),
                radius = discRadius - 1.5.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Subtle top sheen highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(center.x, center.y - discRadius * 0.45f),
                    radius = discRadius * 0.75f
                ),
                radius = discRadius * 0.75f,
                center = Offset(center.x, center.y - discRadius * 0.45f)
            )

            // 4. Center Silhouette Microphone (Exact replica from PNG)
            val micColor = when (assistantState) {
                AssistantState.SPEAKING -> Color(0xFF140826)
                AssistantState.LISTENING -> Color(0xFF071228)
                else -> OrbCenterNavy
            }

            drawMicrophoneIcon(
                center = center,
                discRadius = discRadius,
                color = micColor
            )
        }
    }
}

/**
 * Draws the clean, high-precision microphone silhouette exactly matching the PNG
 */
private fun DrawScope.drawMicrophoneIcon(
    center: Offset,
    discRadius: Float,
    color: Color
) {
    val scale = discRadius / 80f // normalize to base 80px radius

    val capsuleWidth = 24f * scale
    val capsuleHeight = 44f * scale
    val capsuleRadius = capsuleWidth / 2f

    // 1. Mic Capsule (pill shape)
    val capsuleTop = center.y - 28f * scale
    val capsuleLeft = center.x - (capsuleWidth / 2f)

    drawRoundRect(
        color = color,
        topLeft = Offset(capsuleLeft, capsuleTop),
        size = Size(capsuleWidth, capsuleHeight),
        cornerRadius = CornerRadius(capsuleRadius, capsuleRadius)
    )

    // 2. Mic Cradle (U-shaped arc cup)
    val cradleRadius = 24f * scale
    val cradleThickness = 6f * scale
    val cradleRect = Rect(
        center = Offset(center.x, center.y - 6f * scale),
        radius = cradleRadius
    )

    drawArc(
        color = color,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = cradleRect.topLeft,
        size = cradleRect.size,
        style = Stroke(width = cradleThickness, cap = StrokeCap.Round)
    )

    // 3. Vertical stem
    val stemTop = cradleRect.bottom - cradleThickness / 2f
    val stemHeight = 16f * scale
    val stemThickness = 6f * scale

    drawLine(
        color = color,
        start = Offset(center.x, stemTop),
        end = Offset(center.x, stemTop + stemHeight),
        strokeWidth = stemThickness,
        cap = StrokeCap.Round
    )

    // 4. Horizontal base plate
    val baseWidth = 26f * scale
    val baseThickness = 6f * scale
    val baseY = stemTop + stemHeight

    drawLine(
        color = color,
        start = Offset(center.x - baseWidth / 2f, baseY),
        end = Offset(center.x + baseWidth / 2f, baseY),
        strokeWidth = baseThickness,
        cap = StrokeCap.Square
    )
}
