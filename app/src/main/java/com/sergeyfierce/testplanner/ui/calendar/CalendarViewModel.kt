package com.sergeyfierce.testplanner.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sergeyfierce.testplanner.data.TaskRepository
import com.sergeyfierce.testplanner.data.preferences.CalendarPreferencesRepository
import com.sergeyfierce.testplanner.domain.model.CalendarMode
import com.sergeyfierce.testplanner.domain.model.Task
import com.sergeyfierce.testplanner.domain.model.TaskType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.dayOfWeek
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

data class CalendarUiState(
    val isLoading: Boolean = true,
    val selectedMode: CalendarMode = CalendarMode.DAY,
    val currentDate: LocalDate = today(),
    val dayTasks: List<Task> = emptyList(),
    val weekTasks: Map<LocalDate, List<Task>> = emptyMap(),
    val monthTasks: Map<LocalDate, List<Task>> = emptyMap()
)

class CalendarViewModel(
    private val repository: TaskRepository,
    private val preferencesRepository: CalendarPreferencesRepository
) : ViewModel() {

    private val todayDate = today()

    private val currentDate = MutableStateFlow(todayDate)
    private val modeState = MutableStateFlow(CalendarMode.DAY)
    private val eventsFlow = MutableSharedFlow<CalendarEvent>()

    val events: SharedFlow<CalendarEvent> = eventsFlow.asSharedFlow()

    private val dayTasksFlow = currentDate.flatMapLatest { date ->
        repository.observeTasksForDate(date)
    }

    private val weekTasksFlow = currentDate.flatMapLatest { date ->
        val start = startOfWeek(date)
        val end = start.plus(DatePeriod(days = 6))
        repository.observeTasksBetween(start, end)
    }

    private val monthTasksFlow = currentDate.flatMapLatest { date ->
        val start = LocalDate(date.year, date.monthNumber, 1)
        val end = start.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
        repository.observeTasksBetween(start, end)
    }

    val uiState: StateFlow<CalendarUiState> = combine(
        modeState,
        currentDate,
        dayTasksFlow,
        weekTasksFlow,
        monthTasksFlow
    ) { mode, date, dayTasks, weekTasks, monthTasks ->
        CalendarUiState(
            isLoading = false,
            selectedMode = mode,
            currentDate = date,
            dayTasks = dayTasks,
            weekTasks = weekTasks.groupBy { it.date },
            monthTasks = monthTasks.groupBy { it.date }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState()
    )

    init {
        viewModelScope.launch {
            preferencesRepository.mode.collect { storedMode ->
                modeState.value = storedMode
            }
        }
    }

    fun goToToday() {
        currentDate.value = todayDate
    }

    fun goToPrevious() {
        currentDate.value = when (modeState.value) {
            CalendarMode.DAY -> currentDate.value.minus(DatePeriod(days = 1))
            CalendarMode.WEEK -> currentDate.value.minus(DatePeriod(days = 7))
            CalendarMode.MONTH -> currentDate.value.minus(DatePeriod(months = 1))
        }
    }

    fun goToNext() {
        currentDate.value = when (modeState.value) {
            CalendarMode.DAY -> currentDate.value.plus(DatePeriod(days = 1))
            CalendarMode.WEEK -> currentDate.value.plus(DatePeriod(days = 7))
            CalendarMode.MONTH -> currentDate.value.plus(DatePeriod(months = 1))
        }
    }

    fun onModeSelected(mode: CalendarMode) {
        if (modeState.value == mode) return
        modeState.value = mode
        viewModelScope.launch {
            preferencesRepository.setMode(mode)
        }
    }

    fun onDaySelected(date: LocalDate) {
        currentDate.value = date
        onModeSelected(CalendarMode.DAY)
    }

    fun onToggleTask(task: Task, done: Boolean) {
        viewModelScope.launch {
            runCatching { repository.setTaskDone(task.id, done) }
        }
    }

    fun onDeleteTask(task: Task) {
        viewModelScope.launch {
            runCatching { repository.deleteTask(task.id) }
        }
    }

    fun createTask(
        parentId: String?,
        title: String,
        description: String?,
        date: LocalDate,
        type: TaskType,
        start: LocalTime,
        end: LocalTime?,
        isImportant: Boolean
    ) {
        viewModelScope.launch {
            runCatching {
                repository.createTask(parentId, title, description, date, type, start, end, isImportant)
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            runCatching { repository.upsertTask(task) }
        }
    }

    private suspend fun runCatching(block: suspend () -> Unit) {
        try {
            block()
        } catch (error: Exception) {
            eventsFlow.emit(CalendarEvent.Error(error.message ?: "Произошла ошибка"))
        }
    }

    companion object {
        private fun today(): LocalDate = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date

        private fun startOfWeek(date: LocalDate): LocalDate {
            val isoDay = date.dayOfWeek.isoDayNumber
            return date.minus(DatePeriod(days = isoDay - 1))
        }
    }
}

sealed interface CalendarEvent {
    data class Error(val message: String) : CalendarEvent
}
