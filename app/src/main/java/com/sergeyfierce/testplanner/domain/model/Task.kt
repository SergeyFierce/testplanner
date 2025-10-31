package com.sergeyfierce.testplanner.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class Task(
    val id: String,
    val title: String,
    val description: String?,
    val date: LocalDate,
    val type: TaskType,
    val start: LocalTime,
    val end: LocalTime?,
    val isImportant: Boolean,
    val reminderMinutesBefore: Int? = null,
    val repeat: TaskRepeat = TaskRepeat.NONE,
    val repeatFlexibleIntervalDays: Int? = null,
    val isDone: Boolean,
    val createdAt: String,
    val updatedAt: String
) {
    val isInterval: Boolean get() = type == TaskType.INTERVAL
}
