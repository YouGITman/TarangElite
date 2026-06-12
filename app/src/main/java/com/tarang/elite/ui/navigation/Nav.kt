package com.tarang.elite.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tarang.elite.ui.fuel.FuelScreen
import com.tarang.elite.ui.mind.BreathingScreen
import com.tarang.elite.ui.mind.MindScreen
import com.tarang.elite.ui.progress.ProgressScreen
import com.tarang.elite.ui.today.TodayScreen
import com.tarang.elite.ui.train.TrainScreen
import com.tarang.elite.ui.workout.WorkoutScreen

private data class TopTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val topTabs = listOf(
    TopTab("today", "Today", Icons.Rounded.Today),
    TopTab("train", "Train", Icons.Rounded.FitnessCenter),
    TopTab("fuel", "Fuel", Icons.Rounded.Restaurant),
    TopTab("mind", "Mind", Icons.Rounded.SelfImprovement),
    TopTab("progress", "Progress", Icons.Rounded.Insights),
)

private val topRoutes = topTabs.map { it.route }.toSet()

@Composable
fun RootScaffold() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination?.hierarchy?.any { it.route in topRoutes } == true

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    topTabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy
                            ?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = {
                                Text(
                                    tab.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "today",
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 24 }
            },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = {
                fadeIn(tween(220)) + slideInVertically(tween(220)) { -it / 24 }
            },
            popExitTransition = {
                fadeOut(tween(160)) + slideOutVertically(tween(160)) { it / 24 }
            },
        ) {
            composable("today") {
                TodayScreen(
                    onOpenWorkout = { key -> navController.navigate("workout/$key") },
                    onOpenBreathing = { navController.navigate("breathing") },
                )
            }
            composable("train") {
                TrainScreen(
                    onOpenWorkout = { key -> navController.navigate("workout/$key") },
                )
            }
            composable("fuel") { FuelScreen() }
            composable("mind") {
                MindScreen(onOpenBreathing = { navController.navigate("breathing") })
            }
            composable("progress") { ProgressScreen() }
            composable("workout/{key}") { entry ->
                val key = entry.arguments?.getString("key") ?: "tuesday"
                WorkoutScreen(
                    workoutKey = key,
                    onBack = { navController.popBackStack() },
                )
            }
            composable("breathing") {
                BreathingScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
