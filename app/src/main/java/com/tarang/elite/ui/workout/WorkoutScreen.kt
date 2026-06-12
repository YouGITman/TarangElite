package com.tarang.elite.ui.workout

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.tarang.elite.TarangApp
import com.tarang.elite.data.content.ExerciseSpec
import com.tarang.elite.data.content.MindContent
import com.tarang.elite.data.content.WorkoutLibrary
import com.tarang.elite.data.content.WorkoutSpec
import com.tarang.elite.data.repo.TrackingRepository
import com.tarang.elite.ui.components.AnimatedCheckCircle
import com.tarang.elite.ui.components.ConfettiOverlay
import com.tarang.elite.ui.components.appViewModel
import com.tarang.elite.ui.components.rememberConfettiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class WorkoutViewModel(
    private val tracking: TrackingRepository,
    val spec: WorkoutSpec,
) : ViewModel() {

    private val todayIso = LocalDate.now().toString()

    /** Set of exercise ids ticked today — survives leaving and returning. */
    val doneIds: StateFlow<Set<String>> = tracking.ticks(todayIso)
        .map { list -> list.filter { it.done }.map { it.exerciseId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggle(exerciseId: String, currentlyDone: Boolean) {
        viewModelScope.launch { tracking.setTick(todayIso, exerciseId, !currentlyDone) }
    }

    fun complete(completedCount: Int) {
        viewModelScope.launch {
            tracking.logWorkout(
                date = todayIso,
                dayLabel = LocalDate.now().dayOfWeek.name.lowercase()
                    .replaceFirstChar { it.uppercase() },
                name = spec.name,
                completed = completedCount,
                total = spec.exercises.size,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    workoutKey: String,
    onBack: () -> Unit,
) {
    val viewModel = appViewModel(key = "workout_$workoutKey") { app: TarangApp ->
        WorkoutViewModel(app.container.tracking, WorkoutLibrary.byKey(workoutKey))
    }
    val spec = viewModel.spec
    val doneIds by viewModel.doneIds.collectAsStateWithLifecycle()
    val confetti = rememberConfettiState()
    var celebration by remember { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(spec.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "⏱️ ${spec.duration}  •  🔥 ${spec.intensity}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column {
                        Text(
                            "🎯 ${spec.goal}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(12.dp))
                        val progress = if (spec.exercises.isEmpty()) 0f
                        else doneIds.count { id -> spec.exercises.any { it.id == id } }
                            .toFloat() / spec.exercises.size
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                        )
                    }
                }
                items(spec.exercises, key = { it.id }) { exercise ->
                    ExerciseCard(
                        index = spec.exercises.indexOf(exercise) + 1,
                        exercise = exercise,
                        done = exercise.id in doneIds,
                        onToggle = { viewModel.toggle(exercise.id, exercise.id in doneIds) },
                    )
                }
                item {
                    val completedCount =
                        doneIds.count { id -> spec.exercises.any { it.id == id } }
                    Button(
                        onClick = {
                            viewModel.complete(completedCount)
                            confetti.burst()
                            celebration = MindContent.trainerCompleted.random()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    ) {
                        Text(
                            "COMPLETE WORKOUT  ($completedCount/${spec.exercises.size})",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }

        ConfettiOverlay(confetti, Modifier.fillMaxSize())

        celebration?.let { message ->
            Dialog(onDismissRequest = {
                celebration = null
                onBack()
            }) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Column(
                        Modifier.padding(26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("🏆", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            message,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = {
                                celebration = null
                                onBack()
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("LET'S GO")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    index: Int,
    exercise: ExerciseSpec,
    done: Boolean,
    onToggle: () -> Unit,
) {
    var showAlt by rememberSaveable(exercise.id) { mutableStateOf(false) }
    var showHowTo by rememberSaveable(exercise.id) { mutableStateOf(false) }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (done) MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f)
        else MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(26.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$index", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (showAlt) exercise.altName else exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        "${exercise.sets} × ${if (showAlt) exercise.altReps else exercise.reps}" +
                            "   •   ${exercise.equipment}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.width(8.dp))
                AnimatedCheckCircle(done = done, onToggle = onToggle)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                if (showAlt) exercise.altNotes else exercise.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row {
                TextButton(onClick = { showAlt = !showAlt }) {
                    Text(
                        if (showAlt) "↩ Main exercise" else "Too hard? Easier option",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                TextButton(onClick = { showHowTo = !showHowTo }) {
                    Text(
                        if (showHowTo) "Hide how-to" else "How to",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            if (showHowTo) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Text(
                        "💡 ${exercise.howTo}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }
        }
    }
}
