package com.tarang.elite.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.tarang.elite.ui.theme.Amber200
import com.tarang.elite.ui.theme.Amber400
import com.tarang.elite.ui.theme.Amber500
import com.tarang.elite.ui.theme.Cyan
import com.tarang.elite.ui.theme.Emerald
import com.tarang.elite.ui.theme.Rose
import com.tarang.elite.ui.theme.Violet
import kotlin.random.Random

private val confettiPalette = listOf(
    Amber400, Amber500, Amber200, Emerald, Cyan, Violet, Rose, Color.White,
)

private data class Particle(
    val x0: Float,
    val y0: Float,
    val vx: Float,
    val vy: Float,
    val spin: Float,
    val sizePx: Float,
    val color: Color,
)

private fun randomParticle(): Particle = Particle(
    x0 = 0.5f + Random.nextFloat() * 0.16f - 0.08f,
    y0 = 0.42f + Random.nextFloat() * 0.08f,
    vx = (Random.nextFloat() - 0.5f) * 1.2f,
    vy = -(0.35f + Random.nextFloat() * 0.85f),
    spin = (Random.nextFloat() - 0.5f) * 6f,
    sizePx = 9f + Random.nextFloat() * 14f,
    color = confettiPalette[Random.nextInt(confettiPalette.size)],
)

/** Hold in the screen; call [burst] to fire the celebration. */
class ConfettiState {
    var burstId by mutableIntStateOf(0)
        private set

    fun burst() {
        burstId++
    }
}

@Composable
fun rememberConfettiState(): ConfettiState = remember { ConfettiState() }

/**
 * Full-screen particle burst (~90 pieces, 1.4 s, gravity + spin + fade).
 * Draw it last in a Box so it sits above everything.
 */
@Composable
fun ConfettiOverlay(state: ConfettiState, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    var particles by remember { mutableStateOf<List<Particle>>(emptyList()) }

    LaunchedEffect(state.burstId) {
        if (state.burstId == 0) return@LaunchedEffect
        particles = List(90) { randomParticle() }
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 1400, easing = LinearEasing))
        particles = emptyList()
    }

    if (particles.isEmpty()) return

    Canvas(modifier.fillMaxSize()) {
        val t = progress.value
        val gravity = 1.15f
        particles.forEach { p ->
            val x = (p.x0 + p.vx * t) * size.width
            val y = (p.y0 + p.vy * t + gravity * t * t) * size.height
            val alpha = if (t < 0.6f) 1f else ((1f - t) / 0.4f).coerceIn(0f, 1f)
            rotate(degrees = p.spin * t * 360f, pivot = Offset(x, y)) {
                drawRoundRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(x - p.sizePx / 2f, y - p.sizePx / 2f),
                    size = Size(p.sizePx, p.sizePx * 1.55f),
                    cornerRadius = CornerRadius(2.5f, 2.5f),
                )
            }
        }
    }
}
