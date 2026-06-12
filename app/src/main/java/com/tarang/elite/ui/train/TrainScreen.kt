package com.tarang.elite.ui.train

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tarang.elite.data.content.WorkoutLibrary
import com.tarang.elite.ui.components.ExpandableCard
import com.tarang.elite.ui.theme.Rose
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TrainScreen(onOpenWorkout: (String) -> Unit) {
    val todayLabel = LocalDate.now().dayOfWeek
        .getDisplayName(TextStyle.FULL, Locale.UK)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("Weekly plan", style = MaterialTheme.typography.headlineMedium)
        }
        items(WorkoutLibrary.weeklySchedule) { plan ->
            val isToday = plan.dayLabel.equals(todayLabel, ignoreCase = true)
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (plan.priority) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceContainer,
                border = if (isToday) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(plan.emoji, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(plan.dayLabel, style = MaterialTheme.typography.titleMedium)
                        Text(
                            plan.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (plan.priority) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            plan.duration,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            plan.intensity,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (plan.intensity) {
                                "High" -> Rose
                                "Moderate" -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text("📚 Exercise library", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Open any routine on any day — kids' weekend, free weekend, your call.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(WorkoutLibrary.all) { spec ->
            ExpandableCard(
                title = spec.name,
                subtitle = "${spec.duration}  •  ${spec.intensity}",
            ) {
                Text(
                    "🎯 ${spec.goal}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(10.dp))
                spec.exercises.forEachIndexed { idx, ex ->
                    Column(Modifier.padding(vertical = 6.dp)) {
                        Text(
                            "${idx + 1}.  ${ex.name}",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            "${ex.sets} × ${ex.reps}  •  ${ex.equipment}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            ex.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onOpenWorkout(spec.key) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Text("Start this session")
                }
            }
        }
    }
}
