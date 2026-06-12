package com.tarang.elite.ui.today

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val moodEmojis = listOf("😞", "😕", "😐", "🙂", "😄")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInSheet(
    onDismiss: () -> Unit,
    onSave: (mood: Int, energy: Int, stress: Int, note: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var mood by rememberSaveable { mutableIntStateOf(0) } // 0 = unselected, 1..5 chosen
    var energy by rememberSaveable { mutableFloatStateOf(6f) }
    var stress by rememberSaveable { mutableFloatStateOf(4f) }
    var note by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
        ) {
            Text("Morning check-in", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Ten seconds of honesty unlocks today's Recovery Score.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(22.dp))
            Text("How's your mood?", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                moodEmojis.forEachIndexed { index, emoji ->
                    val value = index + 1
                    val selected = mood == value
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.22f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "moodScale",
                    )
                    val bg by animateColorAsState(
                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                        else Color.Transparent,
                        label = "moodBg",
                    )
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(CircleShape)
                            .background(bg)
                            .clickable { mood = value },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(emoji, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Energy", style = MaterialTheme.typography.titleSmall)
                Text(
                    "${energy.roundToInt()}/10",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Slider(
                value = energy,
                onValueChange = { energy = it },
                valueRange = 1f..10f,
                steps = 8,
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Stress", style = MaterialTheme.typography.titleSmall)
                Text(
                    "${stress.roundToInt()}/10",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Slider(
                value = stress,
                onValueChange = { stress = it },
                valueRange = 1f..10f,
                steps = 8,
            )

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Anything on your mind? (optional)") },
                minLines = 2,
            )

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    onSave(mood, energy.roundToInt(), stress.roundToInt(), note.trim())
                    onDismiss()
                },
                enabled = mood in 1..5,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Text("Reveal my Recovery Score", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
