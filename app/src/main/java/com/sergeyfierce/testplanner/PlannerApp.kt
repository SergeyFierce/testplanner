package com.sergeyfierce.testplanner

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
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
    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, bottom = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier.clip(RoundedCornerShape(24.dp))
                    ) {
                        val backStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = backStackEntry?.destination?.route
                        items.forEach { destination ->
                            NavigationBarItem(
                                selected = currentRoute == destination.route,
                                onClick = {
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
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
        }
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
private fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = text, textAlign = TextAlign.Center)
    }
}
