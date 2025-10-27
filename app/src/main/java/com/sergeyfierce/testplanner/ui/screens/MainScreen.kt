package com.sergeyfierce.testplanner.ui.screens

import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.sergeyfierce.testplanner.ui.CalendarViewMode
import com.sergeyfierce.testplanner.ui.PlannerViewModel
import com.sergeyfierce.testplanner.ui.ToastType
import com.sergeyfierce.testplanner.ui.components.DayView
import com.sergeyfierce.testplanner.ui.components.MonthView
import com.sergeyfierce.testplanner.ui.components.PlannerToastHost
import com.sergeyfierce.testplanner.ui.components.SettingsScreen
import com.sergeyfierce.testplanner.ui.components.StatisticsScreen
import com.sergeyfierce.testplanner.ui.components.TaskForm
import com.sergeyfierce.testplanner.ui.components.WeekView
import com.sergeyfierce.testplanner.ui.theme.TestplannerTheme
import com.sergeyfierce.testplanner.lib.types.ThemeMode
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private enum class BottomTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CALENDAR("Календарь", Icons.Filled.CalendarMonth),
    STATS("Статистика", Icons.Filled.BarChart),
    SETTINGS("Настройки", Icons.Filled.Settings)
}

@Composable
fun PlannerMainScreen(
    viewModel: PlannerViewModel,
    onRequestNotifications: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentTab by remember { mutableStateOf(BottomTab.CALENDAR) }
    val context = LocalContext.current
    val isDarkTheme = when (uiState.settings.theme) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.AUTO -> isSystemInDarkTheme()
    }
    val accentColor = remember(uiState.settings.accentColor) {
        Color(AndroidColor.parseColor(uiState.settings.accentColor))
    }
    val selectedDate = remember(uiState.selectedDate) {
        runCatching { LocalDate.parse(uiState.selectedDate) }.getOrNull() ?: LocalDate.now()
    }
    val prevAction: () -> Unit = {
        val nextDate = when (uiState.viewMode) {
            CalendarViewMode.DAY -> selectedDate.minusDays(1)
            CalendarViewMode.WEEK -> selectedDate.minusWeeks(1)
            CalendarViewMode.MONTH -> selectedDate.minusMonths(1)
        }
        viewModel.selectDate(nextDate.toString())
    }
    val nextAction: () -> Unit = {
        val nextDate = when (uiState.viewMode) {
            CalendarViewMode.DAY -> selectedDate.plusDays(1)
            CalendarViewMode.WEEK -> selectedDate.plusWeeks(1)
            CalendarViewMode.MONTH -> selectedDate.plusMonths(1)
        }
        viewModel.selectDate(nextDate.toString())
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            val payload = viewModel.buildExportPayload()
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(payload.toByteArray(StandardCharsets.UTF_8))
            }
            viewModel.notify("Экспорт завершен", ToastType.SUCCESS)
        } else {
            viewModel.notify("Экспорт отменен", ToastType.INFO)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val content = context.contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input)).readText()
            }
            if (content != null) {
                viewModel.importPayload(content)
            }
        }
    }

    TestplannerTheme(darkTheme = isDarkTheme, accentColor = accentColor) {
        Scaffold(
        topBar = {
            if (currentTab == BottomTab.CALENDAR) {
                CalendarTopBar(
                    selectedDate = uiState.selectedDate,
                    viewMode = uiState.viewMode,
                    onViewModeChange = viewModel::setViewMode,
                    onPrev = prevAction,
                    onNext = nextAction,
                    onToday = viewModel::goToToday
                )
            } else {
                TopAppBar(title = { Text(text = when (currentTab) {
                    BottomTab.STATS -> "Статистика"
                    BottomTab.SETTINGS -> "Настройки"
                    BottomTab.CALENDAR -> ""
                }) })
            }
        },
        floatingActionButton = {
            if (currentTab == BottomTab.CALENDAR && uiState.viewMode == CalendarViewMode.DAY) {
                FloatingActionButton(onClick = viewModel::onAddTask) {
                    Icon(Icons.Filled.Add, contentDescription = "Добавить задачу")
                }
            }
        },
        bottomBar = {
            NavigationBar {
                BottomTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == currentTab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            when (currentTab) {
                BottomTab.CALENDAR -> CalendarContent(uiState, viewModel)
                BottomTab.STATS -> StatisticsScreen(statistics = uiState.statistics, modifier = Modifier.padding(16.dp))
                BottomTab.SETTINGS -> SettingsScreen(
                    settings = uiState.settings,
                    onSettingsChanged = viewModel::updateSettings,
                    onExport = {
                        val filename = "planner-backup-${uiState.selectedDate}.json"
                        exportLauncher.launch(filename)
                    },
                    onImport = { importLauncher.launch(arrayOf("application/json")) },
                    onClearAll = viewModel::clearAllData,
                    onRequestNotifications = onRequestNotifications
                )
            }

            PlannerToastHost(toast = uiState.toast, onDismiss = viewModel::dismissToast, modifier = Modifier.fillMaxSize())
        }
    }

    if (uiState.showTaskForm) {
        TaskForm(
            selectedDate = uiState.selectedDate,
            settingsDefaultReminder = uiState.settings.defaultReminderMinutes,
            editingTask = uiState.editingTask,
            onDismiss = viewModel::dismissTaskForm,
            onSave = viewModel::saveTask
        )
    }
    }
}

@Composable
private fun CalendarContent(uiState: com.sergeyfierce.testplanner.ui.PlannerUiState, viewModel: PlannerViewModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        when (uiState.viewMode) {
            CalendarViewMode.DAY -> DayView(
                dayUiState = uiState.dayUiState,
                onTaskClick = viewModel::onEditTask,
                onToggleComplete = viewModel::toggleTaskCompleted,
                onDeleteTask = viewModel::deleteTask,
                onAddTask = viewModel::onAddTask
            )

            CalendarViewMode.WEEK -> WeekView(
                weekSummaries = uiState.weekSummaries,
                onDaySelected = {
                    viewModel.selectDate(it)
                    viewModel.setViewMode(CalendarViewMode.DAY)
                }
            )

            CalendarViewMode.MONTH -> MonthView(
                monthSummaries = uiState.monthSummaries,
                onSelectDate = {
                    viewModel.selectDate(it)
                    viewModel.setViewMode(CalendarViewMode.DAY)
                }
            )
        }
    }
}

@Composable
private fun CalendarTopBar(
    selectedDate: String,
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    val date = runCatching { LocalDate.parse(selectedDate) }.getOrNull()
    val title = date?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)) ?: selectedDate
    Column {
        TopAppBar(
            title = { Text(title, fontWeight = FontWeight.SemiBold) },
            actions = {
                TextButton(onClick = onPrev) { Text("Назад") }
                TextButton(onClick = onToday) { Text("Сегодня") }
                TextButton(onClick = onNext) { Text("Вперёд") }
            }
        )
        TabRow(selectedTabIndex = viewMode.ordinal) {
            CalendarViewMode.entries.forEachIndexed { index, mode ->
                Tab(
                    selected = index == viewMode.ordinal,
                    onClick = { onViewModeChange(mode) },
                    text = {
                        Text(
                            text = when (mode) {
                                CalendarViewMode.DAY -> "День"
                                CalendarViewMode.WEEK -> "Неделя"
                                CalendarViewMode.MONTH -> "Месяц"
                            }
                        )
                    }
                )
            }
        }
    }
}

