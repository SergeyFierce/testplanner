package com.sergeyfierce.testplanner.data

import com.sergeyfierce.testplanner.data.local.TaskDao
import com.sergeyfierce.testplanner.data.local.TaskEntity
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
        updateParentCompletionIfNeeded(entity)
        return entity.toDomain()
    }

    suspend fun createTask(
        parentId: String?,
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
            parentId = parentId,
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
        updateParentCompletionIfNeeded(task.toEntity())
        return task
    }

    suspend fun deleteTask(taskId: String) {
        dao.deleteCascade(taskId)
    }

    suspend fun setTaskDone(taskId: String, done: Boolean) {
        val existing = dao.getById(taskId) ?: return
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        dao.upsert(existing.copy(isDone = done, updatedAt = now))
        updateParentCompletionIfNeeded(existing)
    }

    private suspend fun updateParentCompletionIfNeeded(entity: TaskEntity) {
        val parentId = entity.parentId ?: return
        val parent = dao.getById(parentId) ?: return
        val children = dao.getChildren(parentId)
        val allChildrenDone = children.isNotEmpty() && children.all { it.isDone }
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        val shouldBeDone = allChildrenDone
        if (parent.isDone != shouldBeDone) {
            dao.upsert(parent.copy(isDone = shouldBeDone, updatedAt = now))
        }
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

        task.parentId?.let { parentId ->
            val parent = dao.getById(parentId)?.toDomain()
                ?: error("Родительская активность не найдена")
            require(parent.parentId == null) { "Допускается только один уровень вложенности" }
            require(parent.type == TaskType.INTERVAL) { "Родитель должен быть интервалом" }
            require(parent.date == task.date) { "Дата подзадачи должна совпадать с родителем" }
            val parentEnd = requireNotNull(parent.end)
            require(task.start >= parent.start) { "Подзадача должна начинаться не раньше родителя" }
            val effectiveEnd = task.end ?: task.start
            require(effectiveEnd <= parentEnd) { "Подзадача должна завершаться до окончания родителя" }
        }

        val sameDayTasks = dao.getTasksForDate(task.date.toString())
            .filterNot { it.id == task.id }
            .map { it.toDomain() }

        if (task.parentId == null && task.type == TaskType.INTERVAL) {
            val overlap = sameDayTasks.any { other ->
                other.parentId == null && other.type == TaskType.INTERVAL && task.overlaps(other)
            }
            require(!overlap) { "Основные активности не должны пересекаться" }
        }

        if (task.parentId != null) {
            val parent = dao.getById(task.parentId)?.toDomain()
            val siblings = sameDayTasks.filter { it.parentId == task.parentId }
            if (task.type == TaskType.INTERVAL) {
                requireNotNull(task.end)
            }
            parent?.let {
                val parentEnd = requireNotNull(it.end)
                val effectiveEnd = task.end ?: task.start
                // заменили isBefore/isAfter на сравнения
                require(task.start >= it.start)
                require(effectiveEnd <= parentEnd)
            }
        }
    }

    private fun Task.overlaps(other: Task): Boolean {
        val thisEnd = requireNotNull(end)
        val otherEnd = requireNotNull(other.end)
        return start < otherEnd && other.start < thisEnd
    }
}
