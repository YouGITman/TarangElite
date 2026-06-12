package com.tarang.elite.ui.fuel

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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.tarang.elite.TarangApp
import com.tarang.elite.data.content.NutritionLibrary
import com.tarang.elite.data.content.WorkoutLibrary
import com.tarang.elite.data.db.BadFoodEntity
import com.tarang.elite.data.repo.TrackingRepository
import com.tarang.elite.ui.components.BrandToggle
import com.tarang.elite.ui.components.ExpandableCard
import com.tarang.elite.ui.components.SectionCard
import com.tarang.elite.ui.components.appViewModel
import com.tarang.elite.ui.theme.Cyan
import com.tarang.elite.ui.theme.Emerald
import com.tarang.elite.ui.theme.Rose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class FuelViewModel(private val tracking: TrackingRepository) : ViewModel() {
    val badFoods: StateFlow<List<BadFoodEntity>> = tracking.badFoods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun logBadFood(food: String) {
        if (food.isBlank()) return
        viewModelScope.launch { tracking.logBadFood(food.trim()) }
    }
}

@Composable
fun FuelScreen() {
    val viewModel = appViewModel { app: TarangApp -> FuelViewModel(app.container.tracking) }
    val badFoods by viewModel.badFoods.collectAsStateWithLifecycle()

    val defaultIndex = if (WorkoutLibrary.isWorkoutDay(LocalDate.now().dayOfWeek)) 0 else 1
    var dayIndex by rememberSaveable { mutableIntStateOf(defaultIndex) }
    val isWorkoutDay = dayIndex == 0
    val targets = if (isWorkoutDay) NutritionLibrary.workoutDayTargets else NutritionLibrary.restDayTargets
    val meals = if (isWorkoutDay) NutritionLibrary.workoutDayMeals else NutritionLibrary.restDayMeals

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Fuel", style = MaterialTheme.typography.headlineMedium) }
        item {
            BrandToggle(
                options = listOf("🔥 Workout day", "😴 Rest day"),
                selectedIndex = dayIndex,
                onSelect = { dayIndex = it },
            )
        }
        item {
            SectionCard(title = "Daily targets") {
                Row(Modifier.fillMaxWidth()) {
                    MacroStat("Calories", targets.calories, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    MacroStat("Protein", targets.protein, Emerald, Modifier.weight(1f))
                    MacroStat("Carbs", targets.carbs, Cyan, Modifier.weight(1f))
                    MacroStat("Fat", targets.fat, Rose, Modifier.weight(1f))
                }
            }
        }
        item {
            Text(
                "🍽️ ${if (isWorkoutDay) "Workout day" else "Rest day"} meal plan",
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        items(meals) { meal ->
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(meal.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            meal.time,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = Emerald.copy(alpha = 0.16f),
                    ) {
                        Text(
                            meal.protein,
                            style = MaterialTheme.typography.titleSmall,
                            color = Emerald,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(meal.items, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
                Text(
                    "💡 ${meal.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        item {
            Spacer(Modifier.height(6.dp))
            Text("📊 Food guide", style = MaterialTheme.typography.headlineSmall)
        }
        item {
            ExpandableCard(title = "🟢 GOOD — eat regularly", accent = Emerald) {
                NutritionLibrary.goodFoods.forEach { food ->
                    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(food.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                food.portion,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            food.protein?.let {
                                Text(it, style = MaterialTheme.typography.titleSmall, color = Emerald)
                            }
                            Text(
                                food.notes,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
        item {
            ExpandableCard(title = "🟡 OK — moderate", accent = MaterialTheme.colorScheme.primary) {
                NutritionLibrary.okayFoods.forEach { food ->
                    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(food.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                food.portion,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            food.notes,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            ExpandableCard(title = "🔴 AVOID — limit these", accent = Rose) {
                NutritionLibrary.avoidFoods.forEach { food ->
                    Column(Modifier.padding(vertical = 6.dp)) {
                        Text(food.name, style = MaterialTheme.typography.titleSmall, color = Rose)
                        Text(
                            food.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(6.dp))
            BadFoodLogger(
                recent = badFoods,
                onLog = viewModel::logBadFood,
            )
        }
    }
}

@Composable
private fun MacroStat(label: String, value: String, colour: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, color = colour)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BadFoodLogger(
    recent: List<BadFoodEntity>,
    onLog: (String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    SectionCard(title = "Bad food logger", titleEmoji = "📝", accent = Rose) {
        Text(
            "Honesty is progress — log slips, no judgement.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("What did you eat?") },
                singleLine = true,
            )
            Spacer(Modifier.width(10.dp))
            Button(onClick = {
                onLog(input)
                input = ""
            }) {
                Text("Log")
            }
        }
        if (recent.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            recent.take(8).forEach { entry ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        entry.food,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${entry.date} ${entry.time}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
