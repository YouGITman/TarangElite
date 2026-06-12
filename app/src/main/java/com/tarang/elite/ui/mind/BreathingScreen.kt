package com.tarang.elite.ui.mind

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarang.elite.TarangApp
import com.tarang.elite.data.repo.TrackingRepository
import com.tarang.elite.ui.components.appViewModel
import com.tarang.elite.ui.theme.Violet
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate

class BreathingViewModel(private val tracking: TrackingRepository) : ViewModel() {
    fun saveCycles(cycles: Int) {
        if (cycles <= 0) return
        viewModelScope.launch {
            tracking.addBreathCycles(LocalDate.now().toString(), cycles)
        }
    }
}

private enum class BreathPhase(val label: String, val seconds: Int) {
    READY("Ready when you are", 0),
    INHALE("Inhale through your nose", 4),
    HOLD("Hold", 7),
    EXHALE("Exhale through your mouth", 8),
}

@Composable
fun BreathingScreen(onBack: () -> Unit) {
    val viewModel = appViewModel { app: TarangApp ->
        BreathingViewModel(app.container.tracking)
    }
    val context = LocalContext.current
    val vibrator = remember { vibratorFor(context) }

    var running by remember { mutableStateOf(false) }
    var phase by remember { mutableStateOf(BreathPhase.READY) }
    var secondsLeft by remember { mutableIntStateOf(0) }
    var cycles by remember { mutableIntStateOf(0) }
    val scale = remember { Animatable(0.6f) }

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        while (isActive) {
            phase = BreathPhase.INHALE
            tick(vibrator)
            launch { scale.animateTo(1f, tween(4_000, easing = LinearOutSlowInEasing)) }
            countdown(BreathPhase.INHALE.seconds) { secondsLeft = it }

            phase = BreathPhase.HOLD
            tick(vibrator)
            countdown(BreathPhase.HOLD.seconds) { secondsLeft = it }

            phase = BreathPhase.EXHALE
            tick(vibrator)
            launch { scale.animateTo(0.55f, tween(8_000, easing = FastOutSlowInEasing)) }
            countdown(BreathPhase.EXHALE.seconds) { secondsLeft = it }

            cycles++
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                viewModel.saveCycles(cycles)
                onBack()
            }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Text("4-7-8 breathing", style = MaterialTheme.typography.titleLarge)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // The orb
            Box(contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(280.dp)
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                        }
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Violet.copy(alpha = 0.85f),
                                    Violet.copy(alpha = 0.35f),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (running && secondsLeft > 0) {
                        Text(
                            "$secondsLeft",
                            style = MaterialTheme.typography.displayLarge,
                        )
                    } else {
                        Text("🌬️", style = MaterialTheme.typography.displaySmall)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            Text(
                phase.label,
                style = MaterialTheme.typography.headlineSmall,
                color = Violet,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (cycles == 0) "Aim for 4–8 cycles. Phone buzzes at each phase."
                else "Cycles completed: $cycles",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(36.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { running = !running },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                ) {
                    Text(if (running) "Pause" else if (cycles > 0) "Resume" else "Begin")
                }
                OutlinedButton(
                    onClick = {
                        running = false
                        viewModel.saveCycles(cycles)
                        onBack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                ) {
                    Text("Finish")
                }
            }
            Spacer(Modifier.height(60.dp))
        }
    }
}

private suspend fun countdown(seconds: Int, onTick: (Int) -> Unit) {
    for (s in seconds downTo 1) {
        onTick(s)
        delay(1_000)
    }
}

private fun tick(vibrator: Vibrator?) {
    try {
        vibrator?.vibrate(VibrationEffect.createOneShot(45, 120))
    } catch (_: Exception) {
    }
}

private fun vibratorFor(context: Context): Vibrator? = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
} catch (_: Exception) {
    null
}
