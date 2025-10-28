package com.sergeyfierce.testplanner.data

import com.sergeyfierce.testplanner.data.local.TaskEntity
import com.sergeyfierce.testplanner.domain.model.Task
import com.sergeyfierce.testplanner.domain.model.TaskType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    parentId = parentId,
    title = title,
    description = description,
    date = LocalDate.parse(date),
    type = TaskType.valueOf(type),
    start = LocalTime.parse(start),
    end = end?.let(LocalTime::parse),
    isImportant = isImportant,
    isDone = isDone,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    parentId = parentId,
    title = title,
    description = description,
    date = date.toString(),
    type = type.name,
    start = start.toString(),
    end = end?.toString(),
    isImportant = isImportant,
    isDone = isDone,
    createdAt = createdAt,
    updatedAt = updatedAt
)
