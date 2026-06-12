package com.tarang.elite.ui.mind

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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.tarang.elite.TarangApp
import com.tarang.elite.data.content.MeditationGuide
import com.tarang.elite.data.content.MindContent
import com.tarang.elite.data.db.MeditationDayEntity
import com.tarang.elite.data.repo.TrackingRepository
import com.tarang.elite.ui.components.ExpandableCard
import com.tarang.elite.ui.components.SectionCard
import com.tarang.elite.ui.components.appViewModel
import com.tarang.elite.ui.theme.Cyan
import com.tarang.elite.ui.theme.Emerald
import com.tarang.elite.ui.theme.Violet
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class MindViewModel(private val tracking: TrackingRepository) : ViewModel() {
    private val todayIso = LocalDate.now().toString()

    val meditation: StateFlow<MeditationDayEntity?> = tracking.meditation(todayIso)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setMorning(done: Boolean) {
        viewModelScope.launch { tracking.setMeditation(todayIso, morning = done) }
    }

    fun setEvening(done: Boolean) {
        viewModelScope.launch { tracking.setMeditation(todayIso, evening = done) }
    }
}

@Composable
fun MindScreen(onOpenBreathing: () -> Unit) {
    val viewModel = appViewModel { app: TarangApp -> MindViewModel(app.container.tracking) }
    val meditation by viewModel.meditation.collectAsStateWithLifecycle()

    var quoteIndex by rememberSaveable { mutableIntStateOf(0) }
    var storyIndex by rememberSaveable { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Mind", style = MaterialTheme.typography.headlineMedium) }

        // Breathing hero
        item {
            Surface(
                onClick = onOpenBreathing,
                shape = MaterialTheme.shapes.large,
                color = Violet.copy(alpha = 0.16f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🌬️", style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("4-7-8 breathing", style = MaterialTheme.typography.titleLarge, color = Violet)
                        Text(
                            "Dr Breus' technique to drop your heart rate — perfect before bed or a big moment.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text("→", style = MaterialTheme.typography.headlineSmall, color = Violet)
                }
            }
        }

        // Meditation guides
        item {
            MeditationGuideCard(
                guide = MindContent.morningMeditation,
                emoji = "🌅",
                done = meditation?.morning == true,
                onToggle = { viewModel.setMorning(meditation?.morning != true) },
            )
        }
        item {
            MeditationGuideCard(
                guide = MindContent.eveningMeditation,
                emoji = "🌙",
                done = meditation?.evening == true,
                onToggle = { viewModel.setEvening(meditation?.evening != true) },
            )
        }

        // Visioning
        item {
            ExpandableCard(
                title = "Visioning exercise",
                subtitle = "Weekly (Sunday evening) • 10–15 mins",
                emoji = "🎯",
            ) {
                MindContent.visioningSteps.forEachIndexed { idx, step ->
                    Text(
                        "${idx + 1}.  $step",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        }

        // Power nap
        item {
            ExpandableCard(
                title = "Power nap protocol",
                subtitle = "10–20 mins • early afternoon",
                emoji = "😴",
                accent = Cyan,
            ) {
                Text("Benefits", style = MaterialTheme.typography.titleSmall, color = Cyan)
                MindContent.napBenefits.forEach {
                    Text("•  $it", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text("Tips", style = MaterialTheme.typography.titleSmall, color = Cyan)
                MindContent.napTips.forEach {
                    Text("•  $it", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                }
            }
        }

        // Health tips
        item {
            Spacer(Modifier.height(4.dp))
            Text("❤️ Health tips", style = MaterialTheme.typography.headlineSmall)
        }
        items(MindContent.healthTips) { tip ->
            ExpandableCard(
                title = tip.title,
                subtitle = "Target: ${tip.target}",
                emoji = tip.emoji,
            ) {
                tip.tips.forEach {
                    Text(
                        "•  $it",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 3.dp),
                    )
                }
            }
        }

        // Quote of the day
        item {
            Spacer(Modifier.height(4.dp))
            val quote = MindContent.quotes[quoteIndex % MindContent.quotes.size]
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💬", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "\u201C${quote.text}\u201D",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "— ${quote.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    TextButton(onClick = { quoteIndex++ }) { Text("Next quote →") }
                }
            }
        }

        // Story
        item {
            val story = MindContent.stories[storyIndex % MindContent.stories.size]
            SectionCard(title = story.title, titleEmoji = "📖", accent = Emerald) {
                Text(story.content, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { storyIndex++ },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Read another story")
                }
            }
        }
    }
}

@Composable
private fun MeditationGuideCard(
    guide: MeditationGuide,
    emoji: String,
    done: Boolean,
    onToggle: () -> Unit,
) {
    ExpandableCard(
        title = guide.title,
        subtitle = "${guide.duration} • ${guide.time}" + if (done) "  •  ✓ done" else "",
        emoji = emoji,
        accent = Violet,
    ) {
        guide.steps.forEachIndexed { idx, step ->
            Text(
                "${idx + 1}.  $step",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onToggle,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
        ) {
            Text(if (done) "Marked done ✓ — undo" else "Mark done")
        }
    }
}
