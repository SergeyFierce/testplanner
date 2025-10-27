package com.sergeyfierce.testplanner.lib.statistics

import com.sergeyfierce.testplanner.lib.types.CategoryDistribution
import com.sergeyfierce.testplanner.lib.types.CompletionHistoryEntry
import com.sergeyfierce.testplanner.lib.types.PlannerStatistics
import com.sergeyfierce.testplanner.lib.types.PriorityDistribution
import com.sergeyfierce.testplanner.lib.types.Task
import com.sergeyfierce.testplanner.lib.types.TaskPriority
import com.sergeyfierce.testplanner.lib.types.WeekdayProductivity
import com.sergeyfierce.testplanner.lib.types.toLocalDateOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

private val weekdayLabels = mapOf(
    DayOfWeek.MONDAY to "Пн",
    DayOfWeek.TUESDAY to "Вт",
    DayOfWeek.WEDNESDAY to "Ср",
    DayOfWeek.THURSDAY to "Чт",
    DayOfWeek.FRIDAY to "Пт",
    DayOfWeek.SATURDAY to "Сб",
    DayOfWeek.SUNDAY to "Вс"
)

fun calculateStatistics(tasks: List<Task>): PlannerStatistics {
    if (tasks.isEmpty()) return PlannerStatistics()

    val completedTasks = tasks.filter { it.completed }
    val completionRate = if (tasks.isNotEmpty()) completedTasks.size.toFloat() / tasks.size else 0f

    val streak = calculateStreak(completedTasks)
    val productivity = calculateProductivity(completedTasks)
    val priorityDistribution = calculatePriorityDistribution(tasks)
    val categoryDistribution = calculateCategoryDistribution(tasks)
    val history = buildCompletionHistory(completedTasks)

    return PlannerStatistics(
        totalTasks = tasks.size,
        completedTasks = completedTasks.size,
        completionRate = completionRate,
        streakDays = streak,
        productivityByWeekday = productivity,
        priorityDistribution = priorityDistribution,
        categoryDistribution = categoryDistribution,
        completionHistory = history
    )
}

private fun calculateStreak(completed: List<Task>): Int {
    if (completed.isEmpty()) return 0
    val completedDates = completed.mapNotNull { it.completedAt?.substring(0, 10)?.toLocalDateOrNull() }.toSet()
    var streak = 0
    var currentDate = today()
    while (completedDates.contains(currentDate)) {
        streak += 1
        currentDate = currentDate.plus(DatePeriod(days = -1))
    }
    return streak
}

private fun calculateProductivity(completed: List<Task>): List<WeekdayProductivity> {
    val grouped = completed.groupBy { task ->
        task.completedAt?.substring(0, 10)?.toLocalDateOrNull()?.dayOfWeek
    }
    return DayOfWeek.entries.map { day ->
        WeekdayProductivity(
            weekday = day.ordinal,
            label = weekdayLabels[day] ?: day.name.take(2),
            completed = grouped[day]?.size ?: 0
        )
    }
}

private fun calculatePriorityDistribution(tasks: List<Task>): List<PriorityDistribution> {
    if (tasks.isEmpty()) return emptyList()
    val total = tasks.size
    return TaskPriority.entries.map { priority ->
        val count = tasks.count { it.priority == priority }
        PriorityDistribution(
            priority = priority,
            total = count,
            percentage = if (total == 0) 0f else count.toFloat() / total
        )
    }
}

private fun calculateCategoryDistribution(tasks: List<Task>): List<CategoryDistribution> {
    val categories = tasks.mapNotNull { it.category?.takeIf { name -> name.isNotBlank() } }
    val total = categories.size
    if (total == 0) return emptyList()
    return categories.groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .map { (category, count) ->
            CategoryDistribution(
                category = category,
                total = count,
                percentage = count.toFloat() / total
            )
        }
}

private fun buildCompletionHistory(completed: List<Task>): List<CompletionHistoryEntry> {
    val grouped = completed.groupBy { task ->
        task.completedAt?.substring(0, 10)
    }
    val sorted = grouped.keys.filterNotNull().sortedDescending()
    return sorted.take(7).map { date ->
        CompletionHistoryEntry(date = date, completed = grouped[date]?.size ?: 0)
    }
}

private fun today(): LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

