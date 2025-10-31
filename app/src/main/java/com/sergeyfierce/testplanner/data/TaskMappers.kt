package com.sergeyfierce.testplanner.data

import com.sergeyfierce.testplanner.data.local.TaskEntity
import com.sergeyfierce.testplanner.domain.model.Task
import com.sergeyfierce.testplanner.domain.model.TaskRepeat
import com.sergeyfierce.testplanner.domain.model.TaskType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    title = title,
    description = description,
    date = LocalDate.parse(date),
    type = TaskType.valueOf(type),
    start = LocalTime.parse(start),
    end = end?.let(LocalTime::parse),
    isImportant = isImportant,
    reminderMinutesBefore = reminderOffsetMinutes,
    repeat = TaskRepeat.valueOf(repeatRule),
    repeatFlexibleIntervalDays = repeatFlexibleIntervalDays,
    isDone = isDone,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    date = date.toString(),
    type = type.name,
    start = start.toString(),
    end = end?.toString(),
    isImportant = isImportant,
    reminderOffsetMinutes = reminderMinutesBefore,
    repeatRule = repeat.name,
    repeatFlexibleIntervalDays = repeatFlexibleIntervalDays,
    isDone = isDone,
    createdAt = createdAt,
    updatedAt = updatedAt
)
