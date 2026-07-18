package com.tarang.elite.ui.today

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tarang.elite.TarangApp
import com.tarang.elite.data.content.DayKind
import com.tarang.elite.data.content.WorkoutLibrary
import com.tarang.elite.data.health.HcStatus
import com.tarang.elite.ui.components.ProgressRing
import com.tarang.elite.ui.components.SectionCard
import com.tarang.elite.ui.components.StatChip
import com.tarang.elite.ui.components.appViewModel
import com.tarang.elite.ui.components.pulse
import com.tarang.elite.ui.components.scoreColor
import com.tarang.elite.ui.theme.Cyan
import com.tarang.elite.ui.theme.Emerald
import com.tarang.elite.ui.theme.Rose
import com.tarang.elite.ui.theme.Violet
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(
    onOpenWorkout: (String) -> Unit,
    onOpenBreathing: () -> Unit,
) {
    val viewModel = appViewModel { app: TarangApp ->
        TodayViewModel(app.container.tracking, app.container.healthRepo)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val hcState by viewModel.hcState.collectAsStateWithLifecycle()
    var showCheckIn by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract(),
    ) { viewModel.onHcPermissionResult() }

    if (showCheckIn) {
        CheckInSheet(
            onDismiss = { showCheckIn = false },
            onSave = { mood, energy, stress, note ->
                viewModel.saveCheckIn(mood, energy, stress, note)
            },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 18.dp, end = 18.dp, top = 14.dp, bottom = 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Header(state) }
        item {
            RecoveryCard(
                state = state,
                onCheckIn = { showCheckIn = true },
            )
        }
        item {
            GarminSection(
                state = state,
                hcState = hcState,
                onConnect = { permissionLauncher.launch(viewModel.permissionsToRequest()) },
                onSync = { viewModel.syncNow() },
            )
        }
        item {
            SessionCard(
                state = state,
                onOpenWorkout = onOpenWorkout,
                onMarkAttended = { viewModel.markClassAttended(state.plan) },
            )
        }
        item { WaterCard(state.waterMl, onAdd = viewModel::addWater) }
        item {
            MeditationRow(
                morningDone = state.meditation?.morning == true,
                eveningDone = state.meditation?.evening == true,
                onMorning = { viewModel.toggleMorningMeditation(state.meditation?.morning == true) },
                onEvening = { viewModel.toggleEveningMeditation(state.meditation?.evening == true) },
                onBreathe = onOpenBreathing,
            )
        }
        item { FireCard(state.fireMessage) }
        item { HumanDoorCard() }
    }
}

@Composable
private fun HumanDoorCard() {
    SectionCard(title = "A human, when you're ready", titleEmoji = "🤝") {
        Spacer(Modifier.height(6.dp))
        Text(
            "Forge holds the plan and the numbers. A personal trainer or physio sees what an app can't — the form, the compensations, the thing you're avoiding. No pressure, no upsell. The door is simply open.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Human connection and human intelligence bring what AI never can. That's the plan, not the fallback.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/* ---------------------------------------------------------------------- */

@Composable
private fun Header(state: TodayUiState) {
    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
    val dateText = state.date.format(
        DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.UK),
    )
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("$greeting", style = MaterialTheme.typography.headlineMedium)
            Text(
                dateText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Rounded.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(22.dp)
                        .pulse(),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "${state.streak.count}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/* ---------------------------------------------------------------------- */

@Composable
private fun RecoveryCard(
    state: TodayUiState,
    onCheckIn: () -> Unit,
) {
    SectionCard {
        val recovery = state.recovery
        if (recovery == null) {
            // Locked until check-in — the daily hook.
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    progress = 0f,
                    modifier = Modifier.size(120.dp),
                    stroke = 12.dp,
                ) {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = "Locked",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(34.dp),
                    )
                }
                Spacer(Modifier.width(18.dp))
                Column(Modifier.weight(1f)) {
                    Text("Recovery Score", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Check in to reveal how hard to push today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onCheckIn) { Text("Morning check-in") }
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProgressRing(
                    progress = recovery.score / 100f,
                    modifier = Modifier.size(132.dp),
                    stroke = 13.dp,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${recovery.score}",
                            style = MaterialTheme.typography.displaySmall,
                            color = scoreColor(recovery.score / 100f),
                        )
                        Text(
                            recovery.label.uppercase(Locale.UK),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.width(18.dp))
                Column(Modifier.weight(1f)) {
                    Text("Recovery Score", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        recovery.advice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                recovery.components.forEach { c ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Column(
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "${c.score}",
                                style = MaterialTheme.typography.titleSmall,
                                color = scoreColor(c.score / 100f),
                            )
                            Text(
                                c.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            val debt = state.sleepDebtMin
            if (debt != null && debt > 30) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "😴 Sleep debt this week: ${debt / 60}h ${debt % 60}m — an earlier night repays it fastest.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Violet,
                )
            }
        }
    }
}

/* ---------------------------------------------------------------------- */

@Composable
private fun GarminSection(
    state: TodayUiState,
    hcState: HcUiState,
    onConnect: () -> Unit,
    onSync: () -> Unit,
) {
    if (hcState.status == HcStatus.AVAILABLE && hcState.hasPermissions) {
        val h = state.todayHealth
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatChip(
                    icon = Icons.Rounded.DirectionsWalk,
                    value = h?.steps?.let { String.format(Locale.UK, "%,d", it) } ?: "—",
                    label = "Steps",
                    tint = Emerald,
                    modifier = Modifier.weight(1f),
                )
                StatChip(
                    icon = Icons.Rounded.Bedtime,
                    value = h?.sleepMinutes?.let { "${it / 60}h ${it % 60}m" } ?: "—",
                    label = "Sleep",
                    tint = Violet,
                    modifier = Modifier.weight(1f),
                )
                StatChip(
                    icon = Icons.Rounded.MonitorHeart,
                    value = h?.restingHr?.let { "$it bpm" } ?: "—",
                    label = "Resting HR",
                    tint = Rose,
                    modifier = Modifier.weight(1f),
                )
            }
            TextButton(onClick = onSync, modifier = Modifier.align(Alignment.End)) {
                Text("Sync Garmin data", color = Cyan, style = MaterialTheme.typography.labelMedium)
            }
        }
    } else if (hcState.checked) {
        SectionCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(46.dp)
                        .background(Cyan.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Watch, contentDescription = null, tint = Cyan)
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Connect your Garmin", style = MaterialTheme.typography.titleMedium)
                    Text(
                        when (hcState.status) {
                            HcStatus.AVAILABLE -> "Steps, sleep, heart rate and workouts sync automatically via Health Connect."
                            HcStatus.NEEDS_UPDATE -> "Update the Health Connect app on this phone, then come back."
                            HcStatus.UNAVAILABLE -> "Health Connect isn't available on this device."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (hcState.status == HcStatus.AVAILABLE) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onConnect, modifier = Modifier.fillMaxWidth()) {
                    Text("Grant access")
                }
            }
        }
    }
}

/* ---------------------------------------------------------------------- */

@Composable
private fun SessionCard(
    state: TodayUiState,
    onOpenWorkout: (String) -> Unit,
    onMarkAttended: () -> Unit,
) {
    val plan = state.plan
    val done = state.streak.workoutDoneToday
    SectionCard(
        title = "Today's session",
        titleEmoji = plan.emoji,
        accent = if (plan.priority) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface,
    ) {
        Text(plan.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            "⏱️ ${plan.duration}   •   🔥 ${plan.intensity}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        when (plan.kind) {
            DayKind.WORKOUT -> {
                val key = plan.workoutKey
                if (key != null) {
                    val spec = WorkoutLibrary.byKey(key)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "🎯 ${spec.goal}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { onOpenWorkout(key) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Text(if (done) "Session logged ✓ — go again?" else "Start session")
                    }
                }
            }
            DayKind.CLASS -> {
                Spacer(Modifier.height(14.dp))
                if (done) {
                    Text(
                        "✓ Attended — consistency compounds.",
                        style = MaterialTheme.typography.titleSmall,
                        color = Emerald,
                    )
                } else {
                    Button(
                        onClick = onMarkAttended,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                    ) {
                        Text("Mark attended")
                    }
                }
            }
            DayKind.REST -> {
                Spacer(Modifier.height(10.dp))
                Text(
                    "Recovery is where growth happens. Your muscles are rebuilding.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Text(
                        "Forge tip: protein, water, and a proper night's sleep. The quiet trio.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

/* ---------------------------------------------------------------------- */

@Composable
private fun WaterCard(waterMl: Int, onAdd: (Int) -> Unit) {
    SectionCard(title = "Water", titleEmoji = "💧", accent = Cyan) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                String.format(Locale.UK, "%.1f L", waterMl / 1000f),
                style = MaterialTheme.typography.headlineSmall,
                color = Cyan,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "of 3.0 L",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(10.dp))
        // Progress bar
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHighest,
                    MaterialTheme.shapes.extraSmall,
                ),
        ) {
            Box(
                Modifier
                    .fillMaxWidth((waterMl / 3000f).coerceIn(0f, 1f))
                    .height(8.dp)
                    .background(Cyan, MaterialTheme.shapes.extraSmall),
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(250, 500, 750).forEach { amount ->
                OutlinedButton(
                    onClick = { onAdd(amount) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("+$amount ml", color = Cyan)
                }
            }
        }
    }
}

/* ---------------------------------------------------------------------- */

@Composable
private fun MeditationRow(
    morningDone: Boolean,
    eveningDone: Boolean,
    onMorning: () -> Unit,
    onEvening: () -> Unit,
    onBreathe: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MeditationTile("🌅", "Morning meditation", morningDone, onMorning, Modifier.weight(1f))
            MeditationTile("🌙", "Evening meditation", eveningDone, onEvening, Modifier.weight(1f))
        }
        Surface(
            onClick = onBreathe,
            shape = MaterialTheme.shapes.medium,
            color = Violet.copy(alpha = 0.14f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🌬️", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("4-7-8 breathing", style = MaterialTheme.typography.titleMedium, color = Violet)
                    Text(
                        "Two minutes to a calmer nervous system",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text("→", style = MaterialTheme.typography.titleLarge, color = Violet)
            }
        }
    }
}

@Composable
private fun MeditationTile(
    emoji: String,
    label: String,
    done: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onToggle,
        shape = MaterialTheme.shapes.medium,
        color = if (done) Violet.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier,
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.titleSmall)
            Text(
                if (done) "✓ Completed" else "5–10 mins",
                style = MaterialTheme.typography.labelSmall,
                color = if (done) Violet else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/* ---------------------------------------------------------------------- */

@Composable
private fun FireCard(message: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "🔥 TODAY'S FIRE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
            )
        }
    }
}
