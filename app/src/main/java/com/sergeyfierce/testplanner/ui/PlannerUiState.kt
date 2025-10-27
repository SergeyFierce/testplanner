package com.sergeyfierce.testplanner.ui

import com.sergeyfierce.testplanner.lib.types.DayAgendaItem
import com.sergeyfierce.testplanner.lib.types.PlannerStatistics
import com.sergeyfierce.testplanner.lib.types.Settings
import com.sergeyfierce.testplanner.lib.types.Task
import com.sergeyfierce.testplanner.lib.types.TaskPriority

enum class CalendarViewMode { DAY, WEEK, MONTH }

enum class ToastType { SUCCESS, ERROR, INFO }

data class PlannerToast(
    val message: String,
    val type: ToastType
)

data class DayUiState(
    val agenda: List<DayAgendaItem> = emptyList(),
    val tasksWithoutTime: List<Task> = emptyList(),
    val completionRate: Float = 0f,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0
)

data class WeekDaySummary(
    val date: String,
    val dayLabel: String,
    val dateLabel: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val priorityIndicators: Map<TaskPriority, Int>,
    val isToday: Boolean,
    val isSelected: Boolean
)

data class MonthDaySummary(
    val date: String,
    val dayNumber: Int,
    val totalTasks: Int,
    val priorityIndicators: Map<TaskPriority, Int>,
    val isToday: Boolean,
    val isCurrentMonth: Boolean,
    val isSelected: Boolean
)

data class PlannerUiState(
    val tasks: List<Task> = emptyList(),
    val settings: Settings = Settings(),
    val selectedDate: String,
    val viewMode: CalendarViewMode = CalendarViewMode.DAY,
    val statistics: PlannerStatistics = PlannerStatistics(),
    val dayUiState: DayUiState = DayUiState(),
    val weekSummaries: List<WeekDaySummary> = emptyList(),
    val monthSummaries: List<MonthDaySummary> = emptyList(),
    val isLoading: Boolean = false,
    val showTaskForm: Boolean = false,
    val editingTask: Task? = null,
    val toast: PlannerToast? = null
)

