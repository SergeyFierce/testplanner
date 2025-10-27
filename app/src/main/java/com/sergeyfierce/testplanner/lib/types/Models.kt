package com.sergeyfierce.testplanner.lib.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Core data models used across the planner application.
 */
@Serializable
enum class TaskPriority {
    @SerialName("low") LOW,
    @SerialName("medium") MEDIUM,
    @SerialName("high") HIGH
}

@Serializable
enum class TaskRecurrence {
    @SerialName("none") NONE,
    @SerialName("daily") DAILY,
    @SerialName("weekly") WEEKLY,
    @SerialName("monthly") MONTHLY
}

@Serializable
data class Task(
    val id: String,
    val title: String,
    val description: String? = null,
    val date: String,
    val time: String? = null,
    val duration: Int? = 60,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val completed: Boolean = false,
    val recurrence: TaskRecurrence = TaskRecurrence.NONE,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val reminderMinutes: Int? = 15,
    val createdAt: String,
    val completedAt: String? = null
)

@Serializable
data class Settings(
    val theme: ThemeMode = ThemeMode.AUTO,
    val accentColor: String = AccentColor.Default.hex,
    val notificationsEnabled: Boolean = true,
    val defaultReminderMinutes: Int = 15,
    val weekStartsOn: Int = 1,
    val workingHoursStart: String = "09:00",
    val workingHoursEnd: String = "18:00",
    val language: String = "ru"
)

@Serializable
enum class ThemeMode {
    @SerialName("light") LIGHT,
    @SerialName("dark") DARK,
    @SerialName("auto") AUTO
}

enum class AccentColor(val hex: String) {
    Blue("#3b82f6"),
    Purple("#8b5cf6"),
    Pink("#ec4899"),
    Orange("#f59e0b"),
    Green("#10b981"),
    Cyan("#06b6d4");

    companion object {
        val Default = Blue

        fun fromHex(hex: String): AccentColor = values().firstOrNull {
            it.hex.equals(hex, ignoreCase = true)
        } ?: Default
    }
}

/**
 * Represents an item that can be displayed within the day agenda.
 */
sealed interface DayAgendaItem {
    val key: String

    data class TaskEntry(val task: Task) : DayAgendaItem {
        override val key: String = task.id
    }

    data class FreeSlot(
        override val key: String,
        val startTime: String,
        val endTime: String,
        val durationMinutes: Int
    ) : DayAgendaItem
}

/**
 * Basic analytics structure used by the Statistics screen.
 */
data class PlannerStatistics(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val completionRate: Float = 0f,
    val streakDays: Int = 0,
    val productivityByWeekday: List<WeekdayProductivity> = emptyList(),
    val priorityDistribution: List<PriorityDistribution> = emptyList(),
    val categoryDistribution: List<CategoryDistribution> = emptyList(),
    val completionHistory: List<CompletionHistoryEntry> = emptyList()
)

data class WeekdayProductivity(
    val weekday: Int,
    val label: String,
    val completed: Int
)

data class PriorityDistribution(
    val priority: TaskPriority,
    val total: Int,
    val percentage: Float
)

data class CategoryDistribution(
    val category: String,
    val total: Int,
    val percentage: Float
)

data class CompletionHistoryEntry(
    val date: String,
    val completed: Int
)

