package com.sergeyfierce.testplanner

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateBottomPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
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

    Box(modifier = Modifier.fillMaxSize()) {
        val navigationPadding = 24.dp
        val bottomInsetPadding = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
        val navigationBarBottomPadding = navigationPadding + NavigationBarDefaults.ContainerHeight + bottomInsetPadding

        NavHost(
            navController = navController,
            startDestination = PlannerDestination.CALENDAR.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = navigationBarBottomPadding)
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
                .align(Alignment.BottomCenter)
                .padding(horizontal = navigationPadding, bottom = navigationPadding + bottomInsetPadding)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun PlannerNavigationBar(
    destinations: List<PlannerDestination>,
    currentDestination: String?,
    onDestinationSelected: (PlannerDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 0.dp,
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier.clip(RoundedCornerShape(28.dp))
        ) {
            destinations.forEach { destination ->
                NavigationBarItem(
                    selected = currentDestination == destination.route,
                    onClick = { onDestinationSelected(destination) },
                    icon = {
                        when (destination) {
                            PlannerDestination.CALENDAR -> Icon(imageVector = Icons.Outlined.CalendarMonth, contentDescription = null)
                            PlannerDestination.STATISTICS -> Icon(imageVector = Icons.Outlined.BarChart, contentDescription = null)
                            PlannerDestination.SETTINGS -> Icon(imageVector = Icons.Outlined.Settings, contentDescription = null)
                        }
                    },
                    label = {
                        Text(
                            text = when (destination) {
                                PlannerDestination.CALENDAR -> stringResource(R.string.nav_calendar)
                                PlannerDestination.STATISTICS -> stringResource(R.string.nav_statistics)
                                PlannerDestination.SETTINGS -> stringResource(R.string.nav_settings)
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, textAlign = TextAlign.Center)
    }
}
