package com.sergeyfierce.testplanner.data

import com.sergeyfierce.testplanner.data.local.TaskDao
import com.sergeyfierce.testplanner.domain.model.Task
import com.sergeyfierce.testplanner.domain.model.TaskType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

class TaskRepository(private val dao: TaskDao) {

    fun observeTasksForDate(date: LocalDate): Flow<List<Task>> =
        dao.observeTasksForDate(date.toString()).map { list -> list.map { it.toDomain() } }

    fun observeTasksBetween(start: LocalDate, end: LocalDate): Flow<List<Task>> =
        dao.observeTasksBetween(start.toString(), end.toString()).map { list -> list.map { it.toDomain() } }

    suspend fun getTaskById(id: String): Task? = dao.getById(id)?.toDomain()

    suspend fun upsertTask(task: Task): Task {
        validateTask(task)
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        val existing = dao.getById(task.id)
        val entity = task.copy(
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        ).toEntity()
        dao.upsert(entity)
        return entity.toDomain()
    }

    suspend fun createTask(
        title: String,
        description: String?,
        date: LocalDate,
        type: TaskType,
        start: LocalTime,
        end: LocalTime?,
        isImportant: Boolean
    ): Task {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        val task = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            date = date,
            type = type,
            start = start,
            end = end,
            isImportant = isImportant,
            isDone = false,
            createdAt = now,
            updatedAt = now
        )
        validateTask(task)
        dao.upsert(task.toEntity())
        return task
    }

    suspend fun deleteTask(taskId: String) {
        dao.deleteById(taskId)
    }

    suspend fun setTaskDone(taskId: String, done: Boolean) {
        val existing = dao.getById(taskId) ?: return
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        dao.upsert(existing.copy(isDone = done, updatedAt = now))
    }

    private suspend fun validateTask(task: Task) {
        require(task.start >= LocalTime(0, 0)) { "Начало должно быть в пределах суток" }
        require(task.start <= LocalTime(23, 59)) { "Начало должно быть в пределах суток" }
        task.end?.let {
            require(it > task.start) { "Конец интервала должен быть позже начала" }
            require(it <= LocalTime(23, 59)) { "Интервал должен заканчиваться в пределах суток" }
        }
        if (task.type == TaskType.INTERVAL) {
            requireNotNull(task.end) { "Интервальной задаче требуется окончание" }
        }
        val sameDayTasks = dao.getTasksForDate(task.date.toString())
            .filterNot { it.id == task.id }
            .map { it.toDomain() }

        if (task.type == TaskType.INTERVAL) {
            val overlap = sameDayTasks.any { other ->
                other.type == TaskType.INTERVAL && task.overlaps(other)
            }
            require(!overlap) { "Основные активности не должны пересекаться" }
        }
    }

    private fun Task.overlaps(other: Task): Boolean {
        val thisEnd = requireNotNull(end)
        val otherEnd = requireNotNull(other.end)
        return start < otherEnd && other.start < thisEnd
    }
}
