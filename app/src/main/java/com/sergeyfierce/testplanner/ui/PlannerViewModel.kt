package com.sergeyfierce.testplanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sergeyfierce.testplanner.lib.notifications.NotificationScheduler
import com.sergeyfierce.testplanner.lib.statistics.calculateStatistics
import com.sergeyfierce.testplanner.lib.storage.PlannerRepository
import com.sergeyfierce.testplanner.lib.types.DayAgendaItem
import com.sergeyfierce.testplanner.lib.types.Settings
import com.sergeyfierce.testplanner.lib.types.Task
import com.sergeyfierce.testplanner.lib.types.TaskPriority
import com.sergeyfierce.testplanner.lib.types.TaskRecurrence
import com.sergeyfierce.testplanner.lib.types.nowIsoString
import com.sergeyfierce.testplanner.lib.types.toLocalDateOrNull
import com.sergeyfierce.testplanner.lib.types.toLocalTimeOrNull
import com.sergeyfierce.testplanner.lib.types.todayIsoDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PlannerViewModel(
    private val repository: PlannerRepository,
    private val notifications: NotificationScheduler
) : ViewModel() {

    private val json = Json { prettyPrint = true; encodeDefaults = true }

    private val _uiState = MutableStateFlow(
        PlannerUiState(
            selectedDate = todayIsoDate(),
            isLoading = true
        )
    )
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaultsIfEmpty()
            combine(repository.tasksFlow, repository.settingsFlow) { tasks, settings ->
                tasks to settings
            }.collect { (tasks, settings) ->
                updateState { current ->
                    val selectedDate = current.selectedDate.ifEmpty { todayIsoDate() }
                    current.copy(
                        tasks = tasks,
                        settings = settings,
                        statistics = calculateStatistics(tasks),
                        dayUiState = buildDayUiState(tasks, selectedDate, settings),
                        weekSummaries = buildWeekSummaries(tasks, selectedDate, settings),
                        monthSummaries = buildMonthSummaries(tasks, selectedDate, settings),
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            minuteTicker().collect {
                val state = _uiState.value
                if (!state.isLoading) {
                    updateState { current ->
                        current.copy(statistics = calculateStatistics(current.tasks))
                    }
                }
            }
        }
    }

    fun setViewMode(mode: CalendarViewMode) {
        updateState { it.copy(viewMode = mode) }
    }

    fun selectDate(date: String) {
        updateState { state ->
            state.copy(
                selectedDate = date,
                dayUiState = buildDayUiState(state.tasks, date, state.settings),
                weekSummaries = buildWeekSummaries(state.tasks, date, state.settings),
                monthSummaries = buildMonthSummaries(state.tasks, date, state.settings)
            )
        }
    }

    fun goToToday() = selectDate(todayIsoDate())

    fun onAddTask() {
        updateState { it.copy(showTaskForm = true, editingTask = null) }
    }

    fun onEditTask(task: Task) {
        updateState { it.copy(showTaskForm = true, editingTask = task) }
    }

    fun dismissTaskForm() {
        updateState { it.copy(showTaskForm = false, editingTask = null) }
    }

    fun saveTask(input: TaskFormInput) {
        viewModelScope.launch {
            val isEditing = input.id != null
            val id = input.id ?: repository.generateTaskId()
            val existing = if (isEditing) _uiState.value.tasks.firstOrNull { it.id == id } else null
            val task = Task(
                id = id,
                title = input.title,
                description = input.description.takeIf { it.isNotBlank() },
                date = input.date,
                time = input.time,
                duration = if (input.time != null) (input.duration ?: 60).coerceAtLeast(5) else null,
                priority = input.priority,
                completed = existing?.completed ?: false,
                recurrence = input.recurrence,
                category = input.category.takeIf { it.isNotBlank() },
                tags = input.tags,
                reminderMinutes = input.reminderMinutes,
                createdAt = existing?.createdAt ?: nowIsoString(),
                completedAt = existing?.completedAt
            )

            if (isEditing) {
                repository.updateTask(id) { task }
                notifications.cancelReminder(id)
                scheduleReminderIfNeeded(task)
                showToast("Задача обновлена", ToastType.SUCCESS)
            } else {
                repository.addTask(task)
                scheduleReminderIfNeeded(task)
                showToast("Задача создана", ToastType.SUCCESS)
            }
            dismissTaskForm()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task.id)
            notifications.cancelReminder(task.id)
            showToast("Задача удалена", ToastType.INFO)
        }
    }

    fun toggleTaskCompleted(task: Task, completed: Boolean) {
        viewModelScope.launch {
            val completedAt = if (completed) nowIsoString() else null
            repository.toggleTaskCompleted(task.id, completed, completedAt)
            notifications.cancelReminder(task.id)
            if (!completed) {
                scheduleReminderIfNeeded(task.copy(completed = false))
            } else {
                showToast("Задача выполнена! 🎉", ToastType.SUCCESS)
                handleRecurrence(task)
            }
        }
    }

    fun updateSettings(updates: Settings) {
        viewModelScope.launch {
            repository.updateSettings(updates)
            showToast("Настройки сохранены", ToastType.SUCCESS)
        }
    }

    fun dismissToast() {
        updateState { it.copy(toast = null) }
    }

    fun notify(message: String, type: ToastType = ToastType.INFO) {
        showToast(message, type)
    }

    fun buildExportPayload(): String {
        val state = _uiState.value
        val payload = PlannerBackup(tasks = state.tasks, settings = state.settings)
        return json.encodeToString(payload)
    }

    fun importPayload(content: String) {
        viewModelScope.launch {
            runCatching {
                json.decodeFromString<PlannerBackup>(content)
            }.onSuccess { backup ->
                _uiState.value.tasks.forEach { notifications.cancelReminder(it.id) }
                repository.replaceTasks(backup.tasks)
                repository.updateSettings(backup.settings)
                backup.tasks.forEach { scheduleReminderIfNeeded(it) }
                showToast("Данные импортированы", ToastType.SUCCESS)
            }.onFailure {
                showToast("Ошибка импорта данных", ToastType.ERROR)
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _uiState.value.tasks.forEach { notifications.cancelReminder(it.id) }
            repository.deleteAll()
            showToast("Все данные удалены", ToastType.INFO)
        }
    }

    private fun showToast(message: String, type: ToastType) {
        updateState { it.copy(toast = PlannerToast(message, type)) }
    }

    private fun scheduleReminderIfNeeded(task: Task) {
        if (task.time != null && task.reminderMinutes != null && !task.completed) {
            notifications.scheduleReminder(task)
        }
    }

    private fun buildDayUiState(tasks: List<Task>, date: String, settings: Settings): DayUiState {
        val dayTasks = tasks.filter { it.date == date }
        if (dayTasks.isEmpty()) {
            return DayUiState()
        }
        val workingStart = settings.workingHoursStart.toLocalTimeOrNull() ?: LocalTime(9, 0)
        val workingEnd = settings.workingHoursEnd.toLocalTimeOrNull() ?: LocalTime(18, 0)

        val timed = dayTasks.filter { it.time != null }
            .mapNotNull { task ->
                val start = task.time?.toLocalTimeOrNull() ?: return@mapNotNull null
                val duration = (task.duration ?: 60).coerceAtLeast(5)
                val end = start.plus(DateTimePeriod(minutes = duration))
                TimedTask(task, start, end, duration)
            }
            .sortedBy { it.start }

        val agenda = mutableListOf<DayAgendaItem>()
        var current = workingStart
        timed.forEach { timedTask ->
            if (timedTask.start > current) {
                val durationMinutes = durationBetween(current, timedTask.start)
                if (durationMinutes > 0) {
                    agenda += DayAgendaItem.FreeSlot(
                        key = "free_${current}_${timedTask.start}",
                        startTime = current.toString(),
                        endTime = timedTask.start.toString(),
                        durationMinutes = durationMinutes
                    )
                }
            }
            agenda += DayAgendaItem.TaskEntry(timedTask.task)
            if (timedTask.end > current) current = timedTask.end
        }
        if (current < workingEnd) {
            val durationMinutes = durationBetween(current, workingEnd)
            if (durationMinutes > 0) {
                agenda += DayAgendaItem.FreeSlot(
                    key = "free_${current}_${workingEnd}",
                    startTime = current.toString(),
                    endTime = workingEnd.toString(),
                    durationMinutes = durationMinutes
                )
            }
        }

        if (timed.isEmpty()) {
            val durationMinutes = durationBetween(workingStart, workingEnd)
            if (durationMinutes > 0) {
                agenda.clear()
                agenda += DayAgendaItem.FreeSlot(
                    key = "free_${workingStart}_${workingEnd}",
                    startTime = workingStart.toString(),
                    endTime = workingEnd.toString(),
                    durationMinutes = durationMinutes
                )
            }
        }

        val withoutTime = dayTasks.filter { it.time == null }
            .sortedWith(compareBy<Task> { it.completed }
                .thenByDescending { priorityWeight(it.priority) }
                .thenBy { it.title })

        val total = dayTasks.size
        val completed = dayTasks.count { it.completed }
        val completionRate = if (total == 0) 0f else completed.toFloat() / total

        return DayUiState(
            agenda = agenda,
            tasksWithoutTime = withoutTime,
            completionRate = completionRate,
            totalTasks = total,
            completedTasks = completed
        )
    }

    private fun buildWeekSummaries(tasks: List<Task>, date: String, settings: Settings): List<WeekDaySummary> {
        val currentDate = date.toLocalDateOrNull() ?: todayLocalDate()
        val weekStart = startOfWeek(currentDate, settings.weekStartsOn)
        val labels = if (settings.weekStartsOn == 0) {
            listOf("Вс", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб")
        } else {
            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        }
        return (0 until 7).map { offset ->
            val day = weekStart.plus(DatePeriod(days = offset))
            val dayTasks = tasks.filter { it.date == day.toString() }
            val priorityIndicators = TaskPriority.entries.associateWith { priority ->
                dayTasks.count { it.priority == priority }
            }
            WeekDaySummary(
                date = day.toString(),
                dayLabel = labels[(offset + labels.size) % labels.size],
                dateLabel = day.dayOfMonth.toString(),
                totalTasks = dayTasks.size,
                completedTasks = dayTasks.count { it.completed },
                priorityIndicators = priorityIndicators,
                isToday = day == todayLocalDate(),
                isSelected = day.toString() == date
            )
        }
    }

    private fun buildMonthSummaries(tasks: List<Task>, date: String, settings: Settings): List<MonthDaySummary> {
        val selected = date.toLocalDateOrNull() ?: todayLocalDate()
        val firstDay = LocalDate(selected.year, selected.monthNumber, 1)
        val firstGridDay = startOfWeek(firstDay, settings.weekStartsOn)
        return (0 until 42).map { offset ->
            val day = firstGridDay.plus(DatePeriod(days = offset))
            val dayTasks = tasks.filter { it.date == day.toString() }
            val priorityIndicators = TaskPriority.entries.associateWith { priority ->
                dayTasks.count { it.priority == priority }
            }
            MonthDaySummary(
                date = day.toString(),
                dayNumber = day.dayOfMonth,
                totalTasks = dayTasks.size,
                priorityIndicators = priorityIndicators,
                isToday = day == todayLocalDate(),
                isCurrentMonth = day.month == selected.month,
                isSelected = day.toString() == date
            )
        }
    }

    private fun updateState(reducer: (PlannerUiState) -> PlannerUiState) {
        _uiState.value = reducer(_uiState.value)
    }

    private fun startOfWeek(date: LocalDate, weekStart: Int): LocalDate {
        val desiredStart = if (weekStart == 0) DayOfWeek.SUNDAY else DayOfWeek.MONDAY
        var current = date
        while (current.dayOfWeek != desiredStart) {
            current = current.plus(DatePeriod(days = -1))
        }
        return current
    }

    private fun priorityWeight(priority: TaskPriority): Int = when (priority) {
        TaskPriority.HIGH -> 3
        TaskPriority.MEDIUM -> 2
        TaskPriority.LOW -> 1
    }

    private fun todayLocalDate(): LocalDate =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private fun durationBetween(start: LocalTime, end: LocalTime): Int =
        (end.toSecondOfDay() - start.toSecondOfDay()).coerceAtLeast(0) / 60

    private suspend fun handleRecurrence(task: Task) {
        val nextDate = when (task.recurrence) {
            TaskRecurrence.NONE -> null
            TaskRecurrence.DAILY -> task.date.toLocalDateOrNull()?.plus(DatePeriod(days = 1))
            TaskRecurrence.WEEKLY -> task.date.toLocalDateOrNull()?.plus(DatePeriod(days = 7))
            TaskRecurrence.MONTHLY -> task.date.toLocalDateOrNull()?.plus(DatePeriod(months = 1))
        } ?: return

        val newTask = task.copy(
            id = repository.generateTaskId(),
            date = nextDate.toString(),
            completed = false,
            completedAt = null,
            createdAt = nowIsoString()
        )
        repository.addTask(newTask)
        scheduleReminderIfNeeded(newTask)
    }

    private fun minuteTicker() = flow {
        emit(Unit)
        while (true) {
            delay(60_000)
            emit(Unit)
        }
    }

    data class TimedTask(
        val task: Task,
        val start: LocalTime,
        val end: LocalTime,
        val duration: Int
    )
}

data class TaskFormInput(
    val id: String? = null,
    val title: String,
    val description: String = "",
    val date: String,
    val time: String? = null,
    val duration: Int? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val recurrence: TaskRecurrence = TaskRecurrence.NONE,
    val category: String = "",
    val tags: List<String> = emptyList(),
    val reminderMinutes: Int? = null
)

@kotlinx.serialization.Serializable
data class PlannerBackup(
    val tasks: List<Task>,
    val settings: Settings
)

