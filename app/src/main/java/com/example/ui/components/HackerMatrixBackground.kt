package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Animated Hacker Matrix Rain & Live Coding Background for Home Screen.
 * Renders cascading hacker characters, binary, hex, and code tokens
 * in pure hacker green (default #00FF41).
 */
@Composable
fun HackerMatrixBackground(
    modifier: Modifier = Modifier,
    matrixColor: Color = Color(0xFF00FF41), // Always Green by default
    alpha: Float = 0.32f,
    onCodeCycleCompleted: (() -> Unit)? = null
) {
    val glyphPool = remember {
        "0101011001010101010010101101010101010101ABCDEF0123456789{}[]<>;:=+*~#\$%!&|/\\fun val var if else return".toCharArray()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "hacker_matrix_clock")
    val ticker by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "matrix_ticker"
    )

    // Tracks columns of falling characters
    val columnCount = 20
    val drops = remember {
        IntArray(columnCount) { Random.nextInt(0, 40) }
    }

    // Trigger code complete callback every ~25-30 seconds so code saves to Downloads
    LaunchedEffect(Unit) {
        while (true) {
            delay(28000)
            onCodeCycleCompleted?.invoke()
        }
    }

    // Update drop positions on frame ticks
    val currentTick = (ticker * 12).toInt()

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("hacker_matrix_background")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val colWidth = width / columnCount
            val fontSize = 28f

            val paint = android.graphics.Paint().apply {
                color = matrixColor.hashCode()
                this.textSize = fontSize
                typeface = android.graphics.Typeface.MONOSPACE
                isAntiAlias = true
                this.alpha = (alpha * 255).toInt().coerceIn(10, 255)
            }

            val headPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
                this.textSize = fontSize
                typeface = android.graphics.Typeface.MONOSPACE
                isAntiAlias = true
                this.alpha = ((alpha + 0.4f).coerceAtMost(1.0f) * 255).toInt()
            }

            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                for (col in 0 until columnCount) {
                    val dropYIndex = (drops[col] + (currentTick + col * 3)) % 45
                    val x = col * colWidth + 8f

                    // Draw a trail of characters above the lead drop
                    for (trail in 0..12) {
                        val row = dropYIndex - trail
                        if (row >= 0) {
                            val y = row * fontSize * 1.35f
                            if (y < height) {
                                val charIndex = (col * 7 + row * 13 + currentTick) % glyphPool.size
                                val charToDraw = glyphPool[charIndex].toString()

                                if (trail == 0) {
                                    // Leading bright head
                                    native.drawText(charToDraw, x, y, headPaint)
                                } else {
                                    val fadeAlpha = ((1.0f - (trail / 12f)) * (alpha * 255)).toInt().coerceIn(15, 255)
                                    paint.alpha = fadeAlpha
                                    native.drawText(charToDraw, x, y, paint)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
