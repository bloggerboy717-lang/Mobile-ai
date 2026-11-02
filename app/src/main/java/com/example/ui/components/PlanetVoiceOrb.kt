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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AssistantState
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * PlanetVoiceOrb:
 * A celestial 3D-shaded Planet with the letter 'A' in the center,
 * surrounded by dotted circular planetary orbits ("बिंदु-बिंदु गोल सर्कल").
 *
 * Fully reactive to AssistantState, audio RMS levels,
 * and dynamically restyled by the active AppTheme (Sunset, Red, Light, Dark, Emerald, Custom).
 */
@Composable
fun PlanetVoiceOrb(
    assistantState: AssistantState,
    audioRms: Float = 0f,
    onClick: () -> Unit,
    themeColors: ThemeColors = AppTheme.colors,
    size: Dp = 270.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "planet_orb_animation")

    // Slow planetary orbit rotation 1 (Clockwise)
    val rotationAngle1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_rotation_1"
    )

    // Counter-clockwise planetary orbit rotation 2
    val rotationAngle2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_rotation_2"
    )

    // Fast rotation for active thinking or speaking
    val fastRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                when (assistantState) {
                    AssistantState.THINKING -> 2400
                    AssistantState.SPEAKING -> 4000
                    AssistantState.LISTENING -> 6000
                    AssistantState.IDLE -> 30000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "fast_rotation"
    )

    // Planet breathing/pulsing
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_pulse"
    )

    // Speaking energy glow pulse
    val speakGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speak_glow"
    )

    // Audio reactive expansion
    val rmsNormalized = (audioRms.coerceIn(0f, 10f) / 10f)
    val reactiveScale = when (assistantState) {
        AssistantState.LISTENING -> 1.0f + rmsNormalized * 0.12f
        AssistantState.SPEAKING -> 1.0f + speakGlow * 0.08f
        AssistantState.THINKING -> breathingPulse * 1.02f
        AssistantState.IDLE -> breathingPulse
    }

    val primaryAccent = themeColors.orbPrimary
    val secondaryAccent = themeColors.orbSecondary
    val glowColor = themeColors.orbGlow
    val innerColor = themeColors.orbInner

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("planet_voice_orb"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    scaleX = reactiveScale
                    scaleY = reactiveScale
                }
        ) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val planetRadius = size.toPx() * 0.28f

            // 1. Deep Atmospheric Outer Halo
            val haloRadius = planetRadius * 1.6f
            val haloColors = when (assistantState) {
                AssistantState.LISTENING -> listOf(
                    primaryAccent.copy(alpha = 0.45f + rmsNormalized * 0.25f),
                    secondaryAccent.copy(alpha = 0.20f),
                    Color.Transparent
                )
                AssistantState.THINKING -> listOf(
                    secondaryAccent.copy(alpha = 0.45f),
                    glowColor.copy(alpha = 0.25f),
                    Color.Transparent
                )
                AssistantState.SPEAKING -> listOf(
                    primaryAccent.copy(alpha = 0.40f * speakGlow),
                    secondaryAccent.copy(alpha = 0.25f),
                    Color.Transparent
                )
                AssistantState.IDLE -> listOf(
                    primaryAccent.copy(alpha = 0.25f),
                    secondaryAccent.copy(alpha = 0.10f),
                    Color.Transparent
                )
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = haloColors,
                    center = center,
                    radius = haloRadius
                ),
                radius = haloRadius,
                center = center
            )

            // 2. DOTTED CIRCULAR ORBITS ("बिंदु-बिंदु गोल सर्कल")

            // Orbit Ring A: Outermost dotted circle (32 glowing dots rotating)
            val orbitRadiusOuter = planetRadius * 1.52f
            val dotCountOuter = 32
            val dotRadiusOuter = 2.4.dp.toPx()
            val outerOrbitColor = when (assistantState) {
                AssistantState.LISTENING -> primaryAccent.copy(alpha = 0.90f)
                AssistantState.THINKING -> secondaryAccent.copy(alpha = 0.85f)
                AssistantState.SPEAKING -> glowColor.copy(alpha = 0.90f)
                AssistantState.IDLE -> primaryAccent.copy(alpha = 0.55f)
            }

            for (i in 0 until dotCountOuter) {
                val angleDeg = rotationAngle1 + (i * (360f / dotCountOuter))
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val dotX = center.x + orbitRadiusOuter * cos(angleRad).toFloat()
                val dotY = center.y + orbitRadiusOuter * sin(angleRad).toFloat()
                val alpha = if (i % 2 == 0) 1.0f else 0.5f
                drawCircle(
                    color = outerOrbitColor.copy(alpha = outerOrbitColor.alpha * alpha),
                    radius = if (i % 4 == 0) dotRadiusOuter * 1.4f else dotRadiusOuter,
                    center = Offset(dotX, dotY)
                )
            }

            // Also draw a very subtle faint dashed line behind outer orbit
            drawCircle(
                color = outerOrbitColor.copy(alpha = 0.15f),
                radius = orbitRadiusOuter,
                center = center,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 16f), 0f)
                )
            )

            // Orbit Ring B: Middle Inclined Dotted Ring (Planetary Ring tilted at -22 degrees)
            rotate(degrees = -22f, pivot = center) {
                val ringA = planetRadius * 1.42f
                val ringB = planetRadius * 0.62f
                val dotCountTilted = 36
                val tiltedColor = when (assistantState) {
                    AssistantState.LISTENING -> secondaryAccent.copy(alpha = 0.9f)
                    AssistantState.THINKING -> primaryAccent.copy(alpha = 0.85f)
                    AssistantState.SPEAKING -> glowColor.copy(alpha = 0.9f)
                    AssistantState.IDLE -> secondaryAccent.copy(alpha = 0.55f)
                }

                for (i in 0 until dotCountTilted) {
                    val angleDeg = rotationAngle2 + (i * (360f / dotCountTilted))
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val dotX = center.x + ringA * cos(angleRad).toFloat()
                    val dotY = center.y + ringB * sin(angleRad).toFloat()
                    drawCircle(
                        color = tiltedColor,
                        radius = if (i % 3 == 0) 2.6.dp.toPx() else 1.8.dp.toPx(),
                        center = Offset(dotX, dotY)
                    )
                }

                // Continuous faint orbital trace
                drawOval(
                    color = tiltedColor.copy(alpha = 0.18f),
                    topLeft = Offset(center.x - ringA, center.y - ringB),
                    size = Size(ringA * 2, ringB * 2),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 14f), fastRotation)
                    )
                )
            }

            // Orbit Ring C: Inner Dotted Circular Orbit close to the planet surface
            val orbitRadiusInner = planetRadius * 1.18f
            val dotCountInner = 24
            val innerOrbitColor = when (assistantState) {
                AssistantState.LISTENING -> glowColor
                AssistantState.THINKING -> primaryAccent
                AssistantState.SPEAKING -> secondaryAccent
                AssistantState.IDLE -> glowColor.copy(alpha = 0.65f)
            }

            for (i in 0 until dotCountInner) {
                val angleDeg = fastRotation + (i * (360f / dotCountInner))
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val dotX = center.x + orbitRadiusInner * cos(angleRad).toFloat()
                val dotY = center.y + orbitRadiusInner * sin(angleRad).toFloat()
                drawCircle(
                    color = innerOrbitColor.copy(alpha = 0.75f),
                    radius = 2.0.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }

            // 3. THE PLANET BODY (3D Celestial Sphere)
            val highlightCenter = Offset(
                center.x - planetRadius * 0.32f,
                center.y - planetRadius * 0.32f
            )

            val planetColors = when (assistantState) {
                AssistantState.LISTENING -> listOf(
                    Color.White,
                    glowColor,
                    primaryAccent,
                    secondaryAccent,
                    innerColor
                )
                AssistantState.THINKING -> listOf(
                    Color.White,
                    secondaryAccent,
                    primaryAccent,
                    glowColor,
                    innerColor
                )
                AssistantState.SPEAKING -> listOf(
                    Color.White,
                    glowColor,
                    secondaryAccent,
                    primaryAccent,
                    innerColor
                )
                AssistantState.IDLE -> listOf(
                    if (themeColors.isDark) Color(0xFFF1F5F9) else Color.White,
                    glowColor,
                    primaryAccent,
                    secondaryAccent,
                    innerColor
                )
            }

            // Draw Spherical Planet Body
            drawCircle(
                brush = Brush.radialGradient(
                    colors = planetColors,
                    center = highlightCenter,
                    radius = planetRadius * 1.25f
                ),
                radius = planetRadius,
                center = center
            )

            // 4. Subtle Planetary Bands / Gas Giant Stripes
            val stripeColor = Color.White.copy(alpha = 0.12f)
            rotate(degrees = -15f, pivot = center) {
                drawArc(
                    color = stripeColor,
                    startAngle = 160f,
                    sweepAngle = 220f,
                    useCenter = false,
                    topLeft = Offset(center.x - planetRadius * 0.9f, center.y - planetRadius * 0.3f),
                    size = Size(planetRadius * 1.8f, planetRadius * 0.6f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 5. Crescent Shadow overlay on the lower right
            val shadowColors = listOf(
                Color.Transparent,
                Color.Transparent,
                innerColor.copy(alpha = if (themeColors.isDark) 0.65f else 0.40f),
                innerColor.copy(alpha = if (themeColors.isDark) 0.90f else 0.70f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = shadowColors,
                    center = Offset(center.x + planetRadius * 0.55f, center.y + planetRadius * 0.55f),
                    radius = planetRadius * 1.4f
                ),
                radius = planetRadius,
                center = center
            )

            // 6. Atmospheric Rim Glow
            drawCircle(
                color = primaryAccent.copy(alpha = 0.35f),
                radius = planetRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // 7. CENTER 'A' EMBLEM
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size * 0.45f)
        ) {
            // Glow shadow behind 'A'
            Text(
                text = "A",
                color = primaryAccent.copy(alpha = 0.65f),
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-1).sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = 1.08f
                    scaleY = 1.08f
                    alpha = 0.7f
                }
            )

            // Crisp Core 'A'
            Text(
                text = "A",
                color = if (themeColors.isDark) Color.White else Color(0xFF2C1810),
                fontSize = 50.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-1).sp
            )
        }
    }
}
