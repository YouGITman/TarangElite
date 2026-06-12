package com.tarang.elite.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.tarang.elite.TarangApp
import com.tarang.elite.data.db.BodyMetricEntity
import com.tarang.elite.data.db.CheckInEntity
import com.tarang.elite.data.db.DailyHealthEntity
import com.tarang.elite.data.db.SyncedActivityEntity
import com.tarang.elite.data.db.WorkoutLogEntity
import com.tarang.elite.data.repo.HealthRepository
import com.tarang.elite.data.repo.TrackingRepository
import com.tarang.elite.ui.components.SectionCard
import com.tarang.elite.ui.components.Sparkline
import com.tarang.elite.ui.components.appViewModel
import com.tarang.elite.ui.components.scoreColor
import com.tarang.elite.ui.theme.Cyan
import com.tarang.elite.ui.theme.Emerald
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ProgressUiState(
    val metrics: List<BodyMetricEntity> = emptyList(),
    val workoutLogs: List<WorkoutLogEntity> = emptyList(),
    val healthDays: List<DailyHealthEntity> = emptyList(),
    val activities: List<SyncedActivityEntity> = emptyList(),
    val checkIns: List<CheckInEntity> = emptyList(),
)

class ProgressViewModel(
    private val tracking: TrackingRepository,
    health: HealthRepository,
) : ViewModel() {

    val uiState: StateFlow<ProgressUiState> = combine(
        tracking.bodyMetrics(),
        tracking.workoutLogs(),
        health.lastDays(14),
        health.recentActivities(),
        tracking.recentCheckIns(14),
    ) { metrics, logs, days, activities, checkIns ->
        ProgressUiState(metrics, logs, days, activities, checkIns)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgressUiState())

    fun addMetric(weightKg: Double, bodyFatPct: Double, muscleKg: Double) {
        viewModelScope.launch { tracking.addBodyMetric(weightKg, bodyFatPct, muscleKg) }
    }
}

@Composable
fun ProgressScreen() {
    val viewModel = appViewModel { app: TarangApp ->
        ProgressViewModel(app.container.tracking, app.container.healthRepo)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showEdit by remember { mutableStateOf(false) }

    if (showEdit) {
        BodyMetricDialog(
            latest = state.metrics.lastOrNull(),
            onDismiss = { showEdit = false },
            onSave = { w, bf, m ->
                viewModel.addMetric(w, bf, m)
                showEdit = false
            },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Progress", style = MaterialTheme.typography.headlineMedium) }

        item {
            BodyCompositionCard(
                metrics = state.metrics,
                onEdit = { showEdit = true },
            )
        }

        if (state.metrics.size >= 2) {
            item {
                SectionCard(title = "Weight trend", titleEmoji = "⚖️") {
                    Sparkline(
                        values = state.metrics.map { it.weightKg.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                    )
                }
            }
        }

        if (state.checkIns.isNotEmpty()) {
            item { MoodStrip(state.checkIns) }
        }

        item { CoachAssessment(state) }

        if (state.workoutLogs.isNotEmpty()) {
            item {
                SectionCard(title = "Workout history", titleEmoji = "🏋️") {
                    state.workoutLogs.take(12).forEach { log ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(log.workoutName, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${log.dayLabel} • ${log.date}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                "${log.completed}/${log.total}",
                                style = MaterialTheme.typography.titleSmall,
                                color = Emerald,
                            )
                        }
                    }
                }
            }
        }

        if (state.activities.isNotEmpty()) {
            item {
                SectionCard(title = "Garmin activities", titleEmoji = "⌚", accent = Cyan) {
                    state.activities.take(10).forEach { activity ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                activityEmoji(activity.type),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    activity.title.ifBlank { activity.type },
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Text(
                                    formatActivityDate(activity.startMillis),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                "${activity.durationMinutes} min",
                                style = MaterialTheme.typography.titleSmall,
                                color = Cyan,
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ---------------------------------------------------------------------- */

@Composable
private fun BodyCompositionCard(
    metrics: List<BodyMetricEntity>,
    onEdit: () -> Unit,
) {
    // Repo returns ascending by time: first = baseline, last = latest.
    val first = metrics.firstOrNull()
    val latest = metrics.lastOrNull()

    SectionCard(title = "Body composition", titleEmoji = "💪") {
        if (latest == null) {
            Text(
                "Log your first reading — weight, body fat % and muscle mass from your scales.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(Modifier.fillMaxWidth()) {
                MetricColumn(
                    value = String.format(Locale.UK, "%.1f", latest.weightKg),
                    label = "Weight (kg)",
                    delta = first?.let { latest.weightKg - it.weightKg },
                    goodWhenDown = false,
                    modifier = Modifier.weight(1f),
                )
                MetricColumn(
                    value = String.format(Locale.UK, "%.1f%%", latest.bodyFatPct),
                    label = "Body fat",
                    delta = first?.let { latest.bodyFatPct - it.bodyFatPct },
                    goodWhenDown = true,
                    modifier = Modifier.weight(1f),
                )
                MetricColumn(
                    value = String.format(Locale.UK, "%.1f", latest.muscleKg),
                    label = "Muscle (kg)",
                    delta = first?.let { latest.muscleKg - it.muscleKg },
                    goodWhenDown = false,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Button(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
            Text(if (latest == null) "Log first reading" else "Log new reading")
        }
    }
}

@Composable
private fun MetricColumn(
    value: String,
    label: String,
    delta: Double?,
    goodWhenDown: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (delta != null && kotlin.math.abs(delta) >= 0.05) {
            val improving = if (goodWhenDown) delta < 0 else delta > 0
            Text(
                String.format(Locale.UK, "%s%.1f", if (delta > 0) "↑ +" else "↓ ", delta),
                style = MaterialTheme.typography.labelSmall,
                color = if (improving) Emerald else MaterialTheme.colorScheme.error,
            )
        }
    }
}

/* ---------------------------------------------------------------------- */

@Composable
private fun MoodStrip(checkIns: List<CheckInEntity>) {
    SectionCard(title = "Mood — last 14 days", titleEmoji = "🧠") {
        // recent first from the DAO → reverse for left-to-right time.
        val ordered = checkIns.reversed()
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            ordered.forEach { c ->
                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val colour = scoreColor((c.mood - 1) / 4f)
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = colour,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((14 + c.mood * 7).dp),
                    ) {}
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        val avgMood = checkIns.map { it.mood }.average()
        val avgStress = checkIns.map { it.stress }.average()
        Text(
            String.format(
                Locale.UK,
                "Average mood %.1f/5 • average stress %.1f/10",
                avgMood,
                avgStress,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/* ---------------------------------------------------------------------- */

@Composable
private fun CoachAssessment(state: ProgressUiState) {
    val first = state.metrics.firstOrNull()
    val latest = state.metrics.lastOrNull()
    val workoutsLast14 = state.workoutLogs.count {
        runCatching { LocalDate.parse(it.date) }.getOrNull()
            ?.isAfter(LocalDate.now().minusDays(14)) == true
    }
    val avgSleep = state.healthDays.mapNotNull { it.sleepMinutes }.takeIf { it.isNotEmpty() }?.average()

    SectionCard(title = "Coach's assessment", titleEmoji = "🎯") {
        AssessmentBlock(
            heading = "What's working",
            body = buildString {
                if (first != null && latest != null && first !== latest) {
                    val fatDelta = latest.bodyFatPct - first.bodyFatPct
                    val muscleDelta = latest.muscleKg - first.muscleKg
                    if (fatDelta < 0 || muscleDelta > 0) {
                        append(
                            String.format(
                                Locale.UK,
                                "Body fat %s%.1f%% and muscle %s%.1f kg since your first log. The recomposition is happening — keep this trajectory.",
                                if (fatDelta <= 0) "down " else "up +",
                                kotlin.math.abs(fatDelta),
                                if (muscleDelta >= 0) "up +" else "down ",
                                kotlin.math.abs(muscleDelta),
                            ),
                        )
                    } else {
                        append("You're logging consistently — that honesty is the foundation everything else builds on.")
                    }
                } else {
                    append("You've started. Most people never do. Log workouts and readings and this section sharpens up.")
                }
            },
        )
        Spacer(Modifier.height(10.dp))
        AssessmentBlock(
            heading = "Focus areas",
            body = buildString {
                append("Thursday leg day stays the priority — it's the engine of the physique you're after. ")
                append(
                    if (workoutsLast14 >= 6) "Session count is elite ($workoutsLast14 in 14 days)."
                    else "Aim for at least 4 sessions a week ($workoutsLast14 logged in the last 14 days).",
                )
                if (avgSleep != null && avgSleep < 420) {
                    append(
                        String.format(
                            Locale.UK,
                            " Sleep is averaging %.1f h — muscle is built in bed, so protect it.",
                            avgSleep / 60,
                        ),
                    )
                }
            },
        )
        Spacer(Modifier.height(10.dp))
        AssessmentBlock(
            heading = "12-week projection",
            body = "At this rate: visible arm definition, stronger legs, flatter stomach. " +
                "Hit 130 g protein minimum daily and the mirror does the talking. Stay consistent.",
        )
    }
}

@Composable
private fun AssessmentBlock(heading: String, body: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                heading,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(body, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/* ---------------------------------------------------------------------- */

@Composable
private fun BodyMetricDialog(
    latest: BodyMetricEntity?,
    onDismiss: () -> Unit,
    onSave: (Double, Double, Double) -> Unit,
) {
    var weight by rememberSaveable {
        mutableStateOf(latest?.weightKg?.toString() ?: "")
    }
    var bodyFat by rememberSaveable {
        mutableStateOf(latest?.bodyFatPct?.toString() ?: "")
    }
    var muscle by rememberSaveable {
        mutableStateOf(latest?.muscleKg?.toString() ?: "")
    }
    val valid = weight.toDoubleOrNull() != null &&
        bodyFat.toDoubleOrNull() != null &&
        muscle.toDoubleOrNull() != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log body reading") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = bodyFat,
                    onValueChange = { bodyFat = it },
                    label = { Text("Body fat (%)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = muscle,
                    onValueChange = { muscle = it },
                    label = { Text("Muscle mass (kg)") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                enabled = valid,
                onClick = {
                    onSave(
                        weight.toDouble(),
                        bodyFat.toDouble(),
                        muscle.toDouble(),
                    )
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

/* ---------------------------------------------------------------------- */

private fun activityEmoji(type: String): String = when (type) {
    "Boxing" -> "🥊"
    "Run", "Treadmill" -> "🏃"
    "Walk" -> "🚶"
    "Ride" -> "🚴"
    "Strength" -> "🏋️"
    "HIIT" -> "⚡"
    "Yoga", "Pilates" -> "🧘"
    "Swim" -> "🏊"
    "Hike" -> "🥾"
    "Rowing" -> "🚣"
    "Elliptical" -> "🔄"
    else -> "💪"
}

private fun formatActivityDate(startMillis: Long): String =
    Instant.ofEpochMilli(startMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .format(DateTimeFormatter.ofPattern("EEE d MMM • HH:mm", Locale.UK))
