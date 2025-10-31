package com.sergeyfierce.testplanner.data

import com.sergeyfierce.testplanner.data.local.TaskDao
import com.sergeyfierce.testplanner.domain.model.Task
import com.sergeyfierce.testplanner.domain.model.TaskRepeat
import com.sergeyfierce.testplanner.domain.model.TaskType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

class TaskRepository(private val dao: TaskDao) {

    private val tasksFlow: Flow<List<Task>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeTasksForDate(date: LocalDate): Flow<List<Task>> =
        tasksFlow.map { tasks ->
            tasks.filter { it.occursOn(date) }
                .map { it.toOccurrence(date) }
                .sortedWith(compareBy<Task> { it.start }.thenBy { it.title })
        }

    fun observeTasksBetween(start: LocalDate, end: LocalDate): Flow<List<Task>> =
        tasksFlow.map { tasks ->
            val result = mutableListOf<Task>()
            var cursor = start
            while (cursor <= end) {
                tasks.filter { it.occursOn(cursor) }
                    .mapTo(result) { it.toOccurrence(cursor) }
                cursor = cursor.plus(DatePeriod(days = 1))
            }
            result.sortedWith(compareBy<Task> { it.date }.thenBy { it.start }.thenBy { it.title })
        }

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
        isImportant: Boolean,
        reminderMinutesBefore: Int?,
        repeat: TaskRepeat,
        flexibleIntervalDays: Int?
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
            reminderMinutesBefore = reminderMinutesBefore,
            repeat = repeat,
            repeatFlexibleIntervalDays = flexibleIntervalDays,
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
        task.reminderMinutesBefore?.let { minutes ->
            require(minutes >= 5) { "Напоминание должно быть минимум за 5 минут до начала" }
            val startTotalMinutes = task.start.hour * 60 + task.start.minute
            val maxLead = startTotalMinutes - 5
            require(maxLead >= 5) { "Для выбранного времени нельзя добавить напоминание" }
            require(minutes <= maxLead) { "Напоминание должно быть минимум за 5 минут до начала" }
        }
        if (task.repeat == TaskRepeat.FLEXIBLE) {
            val interval = task.repeatFlexibleIntervalDays
            require(interval != null && interval in 1..365) { "Укажите интервал для гибкого графика" }
        }
        val conflictMessage = findScheduleConflict(
            taskId = task.id,
            date = task.date,
            type = task.type,
            start = task.start,
            end = task.end
        )
        require(conflictMessage == null) { conflictMessage ?: "" }
    }

    suspend fun findScheduleConflict(
        taskId: String?,
        date: LocalDate,
        type: TaskType,
        start: LocalTime,
        end: LocalTime?
    ): String? {
        val sameDayTasks = dao.getAll()
            .map { it.toDomain() }
            .filterNot { it.id == taskId }
            .filter { it.occursOn(date) }

        return when (type) {
            TaskType.INTERVAL -> {
                if (end == null) return "Интервальной задаче требуется окончание"
                val conflict = sameDayTasks.firstOrNull { other ->
                    other.type == TaskType.INTERVAL && start < requireNotNull(other.end) && other.start < end
                }
                conflict?.let { other ->
                    "Интервал пересекается с задачей \"${other.title}\""
                }
            }
            TaskType.POINT -> {
                val conflict = sameDayTasks.firstOrNull { other ->
                    other.type == TaskType.POINT && other.start == start
                }
                conflict?.let { other ->
                    "На это время уже есть задача \"${other.title}\""
                }
            }
        }
    }

}

private fun Task.toOccurrence(date: LocalDate): Task =
    if (this.date == date) {
        this
    } else {
        copy(date = date)
    }

private fun Task.occursOn(date: LocalDate): Boolean {
    if (date == this.date) return true
    if (date < this.date) return false
    return when (repeat) {
        TaskRepeat.NONE -> false
        TaskRepeat.EVERY_DAY -> true
        TaskRepeat.WEEKDAYS -> date.dayOfWeek.isWeekday()
        TaskRepeat.EVERY_WEEK -> occursEvery(days = 7, date = date)
        TaskRepeat.EVERY_TWO_WEEKS -> occursEvery(days = 14, date = date)
        TaskRepeat.EVERY_THREE_WEEKS -> occursEvery(days = 21, date = date)
        TaskRepeat.EVERY_MONTH -> date.dayOfMonth == this.date.dayOfMonth
        TaskRepeat.EVERY_YEAR -> date.dayOfYear == this.date.dayOfYear
        TaskRepeat.FLEXIBLE -> {
            val interval = repeatFlexibleIntervalDays ?: return false
            occursEvery(days = interval, date = date)
        }
    }
}

private fun Task.occursEvery(days: Int, date: LocalDate): Boolean {
    if (days <= 0) return false
    val diff = this.date.daysUntil(date)
    return diff % days == 0
}

private fun DayOfWeek.isWeekday(): Boolean = this != DayOfWeek.SATURDAY && this != DayOfWeek.SUNDAY
