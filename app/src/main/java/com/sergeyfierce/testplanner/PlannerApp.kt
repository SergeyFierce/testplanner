package com.sergeyfierce.testplanner

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sergeyfierce.testplanner.data.TaskRepository
import com.sergeyfierce.testplanner.data.preferences.CalendarPreferencesRepository
import com.sergeyfierce.testplanner.ui.calendar.CalendarScreen
import com.sergeyfierce.testplanner.ui.calendar.CalendarViewModel
import com.sergeyfierce.testplanner.ui.calendar.CalendarViewModelFactory
import androidx.compose.ui.res.stringResource

private enum class PlannerDestination(val route: String) {
    CALENDAR("calendar"),
    STATISTICS("statistics"),
    SETTINGS("settings")
}

@Composable
fun PlannerApp(
    repository: TaskRepository,
    preferencesRepository: CalendarPreferencesRepository
) {
    val navController = rememberNavController()
    val items = listOf(PlannerDestination.CALENDAR, PlannerDestination.STATISTICS, PlannerDestination.SETTINGS)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            PlannerNavigationBar(
                destinations = items,
                currentDestination = currentRoute,
                onDestinationSelected = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp) // Минимальные отступы
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = PlannerDestination.CALENDAR.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(PlannerDestination.CALENDAR.route) {
                val viewModel: CalendarViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = CalendarViewModelFactory(repository, preferencesRepository)
                )
                CalendarScreen(viewModel = viewModel)
            }
            composable(PlannerDestination.STATISTICS.route) {
                PlaceholderScreen(text = stringResource(id = R.string.statistics_placeholder))
            }
            composable(PlannerDestination.SETTINGS.route) {
                PlaceholderScreen(text = stringResource(id = R.string.settings_placeholder))
            }
        }
    }
}

@Composable
private fun PlannerNavigationBar(
    destinations: List<PlannerDestination>,
    currentDestination: String?,
    onDestinationSelected: (PlannerDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), // Полупрозрачный фон
        tonalElevation = 8.dp, // Лёгкая тень
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        destinations.forEach { destination ->
            val selected = currentDestination == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    val icon = when (destination) {
                        PlannerDestination.CALENDAR -> Icons.Outlined.CalendarMonth
                        PlannerDestination.STATISTICS -> Icons.Outlined.BarChart
                        PlannerDestination.SETTINGS -> Icons.Outlined.Settings
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                },
                label = {
                    Text(
                        text = stringResource(
                            when (destination) {
                                PlannerDestination.CALENDAR -> R.string.nav_calendar
                                PlannerDestination.STATISTICS -> R.string.nav_statistics
                                PlannerDestination.SETTINGS -> R.string.nav_settings
                            }
                        ),
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                alwaysShowLabel = true // Всегда показываем текст (по желанию можно убрать)
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, textAlign = TextAlign.Center)
    }
}
