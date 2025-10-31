package com.sergeyfierce.testplanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
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
import com.sergeyfierce.testplanner.R

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
    val destinations = listOf(PlannerDestination.CALENDAR, PlannerDestination.STATISTICS, PlannerDestination.SETTINGS)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    var addTaskAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // ⬇️ Белый фон всего приложения
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black,                 // фон приложения
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Scaffold(
            containerColor = Color.Transparent // Scaffold прозрачный
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = PlannerDestination.CALENDAR.route,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 104.dp)
                ) {
                    composable(PlannerDestination.CALENDAR.route) {
                        val viewModel: CalendarViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                            factory = CalendarViewModelFactory(repository, preferencesRepository)
                        )
                        CalendarScreen(
                            viewModel = viewModel,
                            onAddTaskActionChange = { addTaskAction = it }
                        )
                    }
                    composable(PlannerDestination.STATISTICS.route) {
                        PlaceholderScreen(text = stringResource(id = R.string.statistics_placeholder))
                    }
                    composable(PlannerDestination.SETTINGS.route) {
                        PlaceholderScreen(text = stringResource(id = R.string.settings_placeholder))
                    }
                }

                PlannerNavigationFab(
                    destinations = destinations,
                    currentDestination = currentRoute,
                    onDestinationSelected = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAddClick = addTaskAction,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .navigationBarsPadding()
                )
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PlannerNavigationFab(
    destinations: List<PlannerDestination>,
    currentDestination: String?,
    onDestinationSelected: (PlannerDestination) -> Unit,
    onAddClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            border = null,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                destinations.forEach { destination ->
                    val selected = currentDestination == destination.route
                    NavigationFabItem(
                        destination = destination,
                        selected = selected,
                        onClick = { onDestinationSelected(destination) }
                    )
                }
            }
        }

        val addClick = onAddClick
        AnimatedVisibility(
            visible = addClick != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = (-32).dp)
        ) {
            addClick?.let { click ->
                FloatingActionButton(
                    onClick = click,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp
                    ),
                    modifier = Modifier.shadow(16.dp, CircleShape, clip = false)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.new_task)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun NavigationFabItem(
    destination: PlannerDestination,
    selected: Boolean,
    onClick: () -> Unit
) {
    val label = stringResource(
        when (destination) {
            PlannerDestination.CALENDAR -> R.string.nav_calendar
            PlannerDestination.STATISTICS -> R.string.nav_statistics
            PlannerDestination.SETTINGS -> R.string.nav_settings
        }
    )
    val icon = when (destination) {
        PlannerDestination.CALENDAR -> Icons.Outlined.CalendarMonth
        PlannerDestination.STATISTICS -> Icons.Outlined.BarChart
        PlannerDestination.SETTINGS -> Icons.Outlined.Settings
    }
    val indicatorColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        Color.Transparent
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = indicatorColor,
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
        tonalElevation = if (selected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = contentColor)
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally()
            ) {
                Text(
                    text = label,
                    color = contentColor,
                    style = MaterialTheme.typography.labelLarge
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
