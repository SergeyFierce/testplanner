package com.sergeyfierce.testplanner.lib.storage

import com.sergeyfierce.testplanner.lib.types.Settings
import com.sergeyfierce.testplanner.lib.types.Task
import com.sergeyfierce.testplanner.lib.types.TaskPriority
import com.sergeyfierce.testplanner.lib.types.TaskRecurrence
import com.sergeyfierce.testplanner.lib.types.nowIsoString
import com.sergeyfierce.testplanner.lib.types.todayIsoDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.random.Random

/**
 * Repository that exposes CRUD operations for tasks and handles settings persistence.
 */
class PlannerRepository(private val storage: PlannerStorage) {

    val tasksFlow: Flow<List<Task>> = storage.tasks
    val settingsFlow: Flow<Settings> = storage.settings

    suspend fun getTasks(): List<Task> = tasksFlow.first()

    suspend fun getTasksForDate(date: String): List<Task> =
        getTasks().filter { it.date == date }

    suspend fun getTasksForDateRange(start: String, end: String): List<Task> {
        val startDate = start
        val endDate = end
        return getTasks().filter { task ->
            task.date >= startDate && task.date <= endDate
        }
    }

    suspend fun addTask(task: Task) {
        val tasks = getTasks().toMutableList().apply { add(task) }
        persistTasks(tasks)
    }

    suspend fun updateTask(id: String, updates: (Task) -> Task) {
        val tasks = getTasks().map { task -> if (task.id == id) updates(task) else task }
        persistTasks(tasks)
    }

    suspend fun deleteTask(id: String) {
        val tasks = getTasks().filterNot { it.id == id }
        persistTasks(tasks)
    }

    suspend fun deleteAll() {
        persistTasks(emptyList())
    }

    suspend fun replaceTasks(tasks: List<Task>) {
        persistTasks(tasks)
    }

    suspend fun toggleTaskCompleted(id: String, completed: Boolean, completedAt: String?) {
        updateTask(id) { task -> task.copy(completed = completed, completedAt = completedAt) }
    }

    suspend fun updateSettings(settings: Settings) {
        storage.persistSettings(settings)
        storage.updateLastSync(nowIsoString())
    }

    fun generateTaskId(): String {
        val random = (1..4)
            .map { Random.nextInt(0, 36) }
            .joinToString("") { idx ->
                "0123456789abcdefghijklmnopqrstuvwxyz"[idx].toString()
            }
        return "${System.currentTimeMillis()}_$random"
    }

    fun tasksByDate(date: String): Flow<List<Task>> = tasksFlow.map { tasks ->
        tasks.filter { it.date == date }
    }

    private suspend fun persistTasks(tasks: List<Task>) {
        storage.persistTasks(tasks)
        storage.updateLastSync(nowIsoString())
    }

    suspend fun ensureDefaultsIfEmpty() {
        if (getTasks().isEmpty()) {
            val samples = listOf(
                Task(
                    id = generateTaskId(),
                    title = "Встреча с командой",
                    description = "Обсудить планы на неделю",
                    date = todayIsoDate(),
                    time = "10:00",
                    duration = 60,
                    priority = TaskPriority.HIGH,
                    recurrence = TaskRecurrence.NONE,
                    category = "Работа",
                    reminderMinutes = 15,
                    createdAt = nowIsoString()
                ),
                Task(
                    id = generateTaskId(),
                    title = "Обед",
                    description = "",
                    date = todayIsoDate(),
                    time = "13:00",
                    duration = 45,
                    priority = TaskPriority.MEDIUM,
                    category = "Личное",
                    reminderMinutes = 15,
                    createdAt = nowIsoString()
                ),
                Task(
                    id = generateTaskId(),
                    title = "Тренировка",
                    description = "Зал",
                    date = todayIsoDate(),
                    time = "18:00",
                    duration = 90,
                    priority = TaskPriority.MEDIUM,
                    category = "Здоровье",
                    reminderMinutes = 30,
                    createdAt = nowIsoString()
                ),
                Task(
                    id = generateTaskId(),
                    title = "Купить продукты",
                    description = "",
                    date = todayIsoDate(),
                    priority = TaskPriority.LOW,
                    category = "Личное",
                    reminderMinutes = null,
                    createdAt = nowIsoString()
                )
            )
            persistTasks(samples)
        }
    }
}

