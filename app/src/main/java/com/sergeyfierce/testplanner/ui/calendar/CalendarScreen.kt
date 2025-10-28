package com.sergeyfierce.testplanner.ui.calendar

import android.graphics.Paint
import android.widget.EditText
import android.widget.NumberPicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.children
import com.sergeyfierce.testplanner.R
import com.sergeyfierce.testplanner.domain.model.CalendarMode
import com.sergeyfierce.testplanner.domain.model.Task
import com.sergeyfierce.testplanner.domain.model.TaskType
import com.sergeyfierce.testplanner.ui.theme.TaskDoneGreen
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.Instant
import kotlinx.datetime.plus
import kotlinx.datetime.minus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CalendarEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    var isEditorVisible by rememberSaveable { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var editorDefaultStart by remember { mutableStateOf<LocalTime?>(null) }
    var editorInitialType by remember { mutableStateOf(TaskType.POINT) }
    var pendingDeleteTask by remember { mutableStateOf<Task?>(null) }

    val isToday = uiState.currentDate == today()

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.currentDate.toEpochMillis())
    var isDatePickerVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.currentDate) {
        datePickerState.selectedDateMillis = uiState.currentDate.toEpochMillis()
    }

    fun resetEditorState() {
        isEditorVisible = false
        editingTask = null
        editorDefaultStart = null
        editorInitialType = TaskType.POINT
    }

    BackHandler(enabled = isEditorVisible) {
        resetEditorState()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CalendarTopBar(
                    currentDate = uiState.currentDate,
                    onPrevious = viewModel::goToPrevious,
                    onNext = viewModel::goToNext,
                    onToday = viewModel::goToToday,
                    onDateClick = { isDatePickerVisible = true }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    editingTask = null
                    editorDefaultStart = null
                    editorInitialType = TaskType.POINT
                    isEditorVisible = true
                }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                CalendarModeSelector(
                    selectedMode = uiState.selectedMode,
                    onModeSelected = viewModel::onModeSelected
                )
                Spacer(modifier = Modifier.height(16.dp))
                when (uiState.selectedMode) {
                    CalendarMode.DAY -> DayTimeline(
                        tasks = uiState.dayTasks,
                        isToday = isToday,
                        onToggle = viewModel::onToggleTask,
                        onAddFromSlot = { start ->
                            editingTask = null
                            editorDefaultStart = start
                            editorInitialType = TaskType.POINT
                            isEditorVisible = true
                        },
                        onEdit = { task ->
                            editingTask = task
                            editorInitialType = task.type
                            editorDefaultStart = task.start
                            isEditorVisible = true
                        },
                        onDelete = { task -> pendingDeleteTask = task }
                    )
                    CalendarMode.WEEK -> WeekView(
                        currentDate = uiState.currentDate,
                        weekTasks = uiState.weekTasks,
                        onDayClick = viewModel::onDaySelected
                    )
                    CalendarMode.MONTH -> MonthView(
                        currentDate = uiState.currentDate,
                        monthTasks = uiState.monthTasks,
                        onDayClick = viewModel::onDaySelected
                    )
                }
            }
        }

        if (isEditorVisible) {
            TaskEditorScreen(
                initialTask = editingTask,
                defaultDate = uiState.currentDate,
                defaultStart = editorDefaultStart,
                initialType = editorInitialType,
                onValidateSchedule = { id, date, type, start, end ->
                    viewModel.validateSchedule(id, date, type, start, end)
                },
                onDismiss = { resetEditorState() },
                onCreate = { title, description, date, type, start, end, isImportant ->
                    viewModel.createTask(title, description, date, type, start, end, isImportant)
                    resetEditorState()
                },
                onUpdate = { task ->
                    viewModel.updateTask(task)
                    resetEditorState()
                }
            )
        }

        if (isDatePickerVisible) {
            DatePickerDialog(
                onDismissRequest = { isDatePickerVisible = false },
                confirmButton = {
                    TextButton(onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            viewModel.onDaySelected(selectedMillis.toLocalDate())
                        }
                        isDatePickerVisible = false
                    }) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isDatePickerVisible = false }) {
                        Text(text = stringResource(android.R.string.cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        pendingDeleteTask?.let { task ->
            AlertDialog(
                onDismissRequest = { pendingDeleteTask = null },
                title = { Text(text = stringResource(id = R.string.dialog_delete_title)) },
                text = {
                    Text(
                        text = stringResource(id = R.string.dialog_delete_message, task.title)
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onDeleteTask(task)
                        pendingDeleteTask = null
                    }) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDeleteTask = null }) {
                        Text(text = stringResource(android.R.string.cancel))
                    }
                }
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarTopBar(
    currentDate: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onDateClick: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()) }
    TopAppBar(
        title = {
            Text(
                text = formatter.format(currentDate.toJavaLocalDate()),
                modifier = Modifier.clickable(onClick = onDateClick)
            )
        },
        actions = {
            TextButton(onClick = onToday) {
                Text(text = stringResource(id = R.string.today))
            }
        },
        navigationIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevious) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
                IconButton(onClick = onNext) {
                    Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarModeSelector(
    selectedMode: CalendarMode,
    onModeSelected: (CalendarMode) -> Unit
) {
    val items = listOf(
        CalendarMode.DAY to stringResource(id = R.string.mode_day),
        CalendarMode.WEEK to stringResource(id = R.string.mode_week),
        CalendarMode.MONTH to stringResource(id = R.string.mode_month)
    )
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        items.forEachIndexed { index, (mode, label) ->
            SegmentedButton(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = items.size)
            ) {
                Text(text = label)
            }
        }
    }
}
@Composable
private fun DayTimeline(
    tasks: List<Task>,
    isToday: Boolean,
    onToggle: (Task, Boolean) -> Unit,
    onAddFromSlot: (LocalTime) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    val listState = rememberLazyListState()
    val sortedTasks = remember(tasks) {
        tasks.sortedWith(compareBy<Task> { it.start }.thenBy { it.title })
    }
    val timelineItems = remember(sortedTasks) {
        buildTimeline(sortedTasks)
    }

    LaunchedEffect(isToday, timelineItems) {
        if (isToday) {
            val currentHour = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).time.hour
            val index = timelineItems.indexOfFirst { item ->
                item is DayTimelineItem.TaskBlock && item.task.start.hour >= currentHour
            }.takeIf { it >= 0 } ?: 0
            listState.scrollToItem(index)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = timelineItems,
            key = { item ->
                when (item) {
                    is DayTimelineItem.FreeTime -> "free-${item.start}-${item.endExclusive}"
                    is DayTimelineItem.TaskBlock -> item.task.id
                }
            }
        ) { item ->
            when (item) {
                is DayTimelineItem.FreeTime -> FreeTimeCard(
                    freeTime = item,
                    onAddTask = onAddFromSlot
                )
                is DayTimelineItem.TaskBlock -> {
                    if (item.task.isInterval) {
                        IntervalTaskCard(
                            task = item.task,
                            nestedPoints = item.nestedPoints,
                            onToggle = onToggle,
                            onEdit = onEdit,
                            onDelete = onDelete
                        )
                    } else {
                        PointTaskCard(
                            task = item.task,
                            onToggle = onToggle,
                            onEdit = onEdit,
                            onDelete = onDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FreeTimeCard(
    freeTime: DayTimelineItem.FreeTime,
    onAddTask: (LocalTime) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAddTask(freeTime.start) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val isWholeDay = freeTime.start == LocalTime(0, 0) && freeTime.endExclusive == null
            Text(
                text = if (isWholeDay) {
                    stringResource(id = R.string.free_day_title)
                } else {
                    stringResource(id = R.string.free_time_title)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (isWholeDay) {
                Text(
                    text = stringResource(id = R.string.free_day_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val endLabel = freeTime.endExclusive?.toTimeLabel()
                    ?: stringResource(id = R.string.free_time_until_end)
                Text(
                    text = stringResource(
                        id = R.string.free_time_range,
                        freeTime.start.toTimeLabel(),
                        endLabel
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.free_time_add),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private sealed interface DayTimelineItem {
    data class FreeTime(val start: LocalTime, val endExclusive: LocalTime?) : DayTimelineItem
    data class TaskBlock(val task: Task, val nestedPoints: List<Task> = emptyList()) : DayTimelineItem
}

private fun buildTimeline(tasks: List<Task>): List<DayTimelineItem> {
    if (tasks.isEmpty()) {
        return listOf(DayTimelineItem.FreeTime(LocalTime(0, 0), null))
    }

    val sortedTasks = tasks.sortedWith(compareBy<Task> { it.start }.thenBy { it.title })
    val pointTasks = sortedTasks.filter { !it.isInterval }
    val consumedPointIds = mutableSetOf<String>()

    val result = mutableListOf<DayTimelineItem>()
    var cursor = LocalTime(0, 0)

    sortedTasks.forEach { task ->
        if (task.isInterval) {
            val start = task.start
            val end = task.end ?: task.start
            if (cursor < start) {
                result += DayTimelineItem.FreeTime(cursor, start)
            }
            val nested = pointTasks
                .filter { candidate ->
                    candidate.id !in consumedPointIds && candidate.start >= start && candidate.start < end
                }
                .sortedBy { it.start }
            consumedPointIds += nested.map { it.id }
            result += DayTimelineItem.TaskBlock(task = task, nestedPoints = nested)
            if (end > cursor) {
                cursor = end
            }
        } else if (task.id !in consumedPointIds) {
            val start = task.start
            if (cursor < start) {
                result += DayTimelineItem.FreeTime(cursor, start)
            }
            result += DayTimelineItem.TaskBlock(task = task)
            if (start > cursor) {
                cursor = start
            }
        }
    }

    if (cursor < LocalTime(23, 59)) {
        result += DayTimelineItem.FreeTime(cursor, null)
    }

    return result
}

private fun LocalTime.toTimeLabel(): String = "%02d:%02d".format(hour, minute)

private fun timeRangeLabel(task: Task): String {
    val endPart = task.end?.let { " - ${it.toTimeLabel()}" } ?: ""
    return "${task.start.toTimeLabel()}$endPart"
}

private val LocalDateSaver = Saver<LocalDate, String>(
    save = { it.toString() },
    restore = { LocalDate.parse(it) }
)

@Composable
private fun taskContainerColor(task: Task): Color = when {
    task.isDone -> TaskDoneGreen
    task.isImportant -> MaterialTheme.colorScheme.errorContainer
    task.isInterval -> MaterialTheme.colorScheme.primaryContainer
    else -> MaterialTheme.colorScheme.secondaryContainer
}

@Composable
private fun taskContentColor(task: Task): Color = when {
    task.isDone -> MaterialTheme.colorScheme.onPrimaryContainer
    task.isImportant -> MaterialTheme.colorScheme.onErrorContainer
    task.isInterval -> MaterialTheme.colorScheme.onPrimaryContainer
    else -> MaterialTheme.colorScheme.onSecondaryContainer
}
@Composable
private fun IntervalTaskCard(
    task: Task,
    nestedPoints: List<Task>,
    onToggle: (Task, Boolean) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    Surface(
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(16.dp),
        color = taskContainerColor(task),
        contentColor = taskContentColor(task),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = task.isDone,
                    onCheckedChange = { onToggle(task, it) }
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timeRangeLabel(task),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = { onEdit(task) }) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                }
                IconButton(onClick = { onDelete(task) }) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
                }
            }
            if (!task.description.isNullOrBlank()) {
                Text(
                    text = task.description!!,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (nestedPoints.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    nestedPoints.forEach { nested ->
                        NestedPointTaskCard(
                            task = nested,
                            onToggle = onToggle,
                            onEdit = onEdit,
                            onDelete = onDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PointTaskCard(
    task: Task,
    onToggle: (Task, Boolean) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp,
        color = taskContainerColor(task),
        contentColor = taskContentColor(task),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Checkbox(checked = task.isDone, onCheckedChange = { onToggle(task, it) })
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.bodyMedium)
                Text(text = timeRangeLabel(task), style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { onEdit(task) }) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
            }
            IconButton(onClick = { onDelete(task) }) {
                Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
            }
        }
    }
}

@Composable
private fun NestedPointTaskCard(
    task: Task,
    onToggle: (Task, Boolean) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp,
        color = taskContainerColor(task),
        contentColor = taskContentColor(task),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .fillMaxWidth()
        ) {
            Checkbox(checked = task.isDone, onCheckedChange = { onToggle(task, it) })
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.bodyMedium)
                Text(text = task.start.toTimeLabel(), style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { onEdit(task) }) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
            }
            IconButton(onClick = { onDelete(task) }) {
                Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
            }
        }
    }
}

@Composable
private fun WeekTaskPreview(task: Task) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        color = taskContainerColor(task),
        contentColor = taskContentColor(task),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = timeRangeLabel(task),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ImportantDot(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (visible) {
        Box(
            modifier = modifier
                .size(10.dp)
                .background(color = MaterialTheme.colorScheme.error, shape = CircleShape)
        )
    }
}
@Composable
private fun WeekView(
    currentDate: LocalDate,
    weekTasks: Map<LocalDate, List<Task>>,
    onDayClick: (LocalDate) -> Unit
) {
    val start = startOfWeek(currentDate)
    val days = remember(start) { (0..6).map { offset -> start.plus(DatePeriod(days = offset)) } }
    val locale = Locale.getDefault()
    val dateFormatter = remember(locale) {
        DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(days) { date ->
            val tasksForDay = weekTasks[date].orEmpty().sortedBy { it.start }
            val isToday = date == today()
            val isSelected = date == currentDate
            Surface(
                onClick = { onDayClick(date) },
                shape = RoundedCornerShape(16.dp),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    isToday -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                tonalElevation = if (isSelected || isToday) 4.dp else 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val formatted = dateFormatter.format(date.toJavaLocalDate()).replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase(locale) else char.toString()
                    }
                    Text(
                        text = formatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (tasksForDay.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.free_day_title),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            tasksForDay.take(4).forEach { task ->
                                WeekTaskPreview(task = task)
                            }
                        }
                        if (tasksForDay.size > 4) {
                            Text(
                                text = stringResource(id = R.string.more_tasks, tasksForDay.size - 4),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun MonthView(
    currentDate: LocalDate,
    monthTasks: Map<LocalDate, List<Task>>,
    onDayClick: (LocalDate) -> Unit
) {
    val yearMonth = remember(currentDate) { YearMonth.of(currentDate.year, currentDate.monthNumber) }
    val firstDay = LocalDate(currentDate.year, currentDate.monthNumber, 1)
    val firstDayOfWeek = startOfWeek(firstDay)
    val days = remember(currentDate) {
        (0 until 42).map { offset -> firstDayOfWeek.plus(DatePeriod(days = offset)) }
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(days) { date ->
            val isCurrentMonth = date.monthNumber == yearMonth.monthValue
            val tasksForDay = monthTasks[date].orEmpty()
            val hasImportant = tasksForDay.any { it.isImportant }
            Surface(
                onClick = { if (isCurrentMonth) onDayClick(date) },
                shape = RoundedCornerShape(10.dp),
                color = when {
                    date == currentDate -> MaterialTheme.colorScheme.primaryContainer
                    date == today() -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surface
                },
                tonalElevation = if (isCurrentMonth) 2.dp else 0.dp,
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize()
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCurrentMonth) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                    ImportantDot(
                        visible = hasImportant,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                    if (tasksForDay.isNotEmpty()) {
                        BadgedBox(
                            badge = { Badge { Text(text = tasksForDay.size.toString()) } },
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            Spacer(modifier = Modifier.height(0.dp))
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskEditorScreen(
    initialTask: Task?,
    defaultDate: LocalDate,
    defaultStart: LocalTime?,
    initialType: TaskType,
    onValidateSchedule: suspend (String?, LocalDate, TaskType, LocalTime, LocalTime?) -> String?,
    onDismiss: () -> Unit,
    onCreate: (String, String?, LocalDate, TaskType, LocalTime, LocalTime?, Boolean) -> Unit,
    onUpdate: (Task) -> Unit
) {
    val context = LocalContext.current

    var title by rememberSaveable { mutableStateOf(initialTask?.title.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(initialTask?.description.orEmpty()) }
    var type by rememberSaveable { mutableStateOf(initialTask?.type ?: initialType) }
    var isImportant by rememberSaveable { mutableStateOf(initialTask?.isImportant ?: false) }
    var saveError by rememberSaveable { mutableStateOf<String?>(null) }

    var selectedDate by rememberSaveable(stateSaver = LocalDateSaver) {
        mutableStateOf(initialTask?.date ?: defaultDate)
    }

    val initialStart = remember(initialTask, defaultStart) {
        initialTask?.start ?: defaultStart ?: LocalTime(9, 0)
    }
    val initialEndCandidate = remember(initialTask) {
        initialTask?.end ?: defaultEndFor(initialStart)
    }

    var startHour by rememberSaveable { mutableIntStateOf(initialStart.hour) }
    var startMinute by rememberSaveable { mutableIntStateOf(initialStart.minute) }
    var endHour by rememberSaveable { mutableIntStateOf(initialEndCandidate.hour) }
    var endMinute by rememberSaveable { mutableIntStateOf(initialEndCandidate.minute) }

    val startTime = remember(startHour, startMinute) { LocalTime(startHour, startMinute) }
    val endTime = remember(endHour, endMinute, type) {
        if (type == TaskType.INTERVAL) LocalTime(endHour, endMinute) else null
    }

    LaunchedEffect(startTime, type) {
        if (type == TaskType.INTERVAL) {
            val currentEnd = LocalTime(endHour, endMinute)
            if (currentEnd <= startTime) {
                val adjusted = defaultEndFor(startTime)
                endHour = adjusted.hour
                endMinute = adjusted.minute
            }
        }
    }

    var scheduleError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(selectedDate, type, startTime, endTime) {
        scheduleError = if (type == TaskType.INTERVAL && endTime == null) {
            context.getString(R.string.error_end_required)
        } else {
            onValidateSchedule(initialTask?.id, selectedDate, type, startTime, endTime)
        }
    }

    LaunchedEffect(scheduleError) {
        if (scheduleError == null) {
            saveError = null
        }
    }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())
    }

    var isDatePickerVisible by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.toEpochMillis())

    LaunchedEffect(selectedDate) {
        datePickerState.selectedDateMillis = selectedDate.toEpochMillis()
    }

    fun handleSave() {
        saveError = null
        if (title.isBlank()) {
            saveError = context.getString(R.string.error_title_required)
            return
        }
        if (scheduleError != null) {
            saveError = scheduleError
            return
        }
        val trimmedDescription = description.takeIf { it.isNotBlank() }
        val end = if (type == TaskType.INTERVAL) LocalTime(endHour, endMinute) else null
        if (initialTask == null) {
            onCreate(title, trimmedDescription, selectedDate, type, startTime, end, isImportant)
        } else {
            onUpdate(
                initialTask.copy(
                    title = title,
                    description = trimmedDescription,
                    date = selectedDate,
                    type = type,
                    start = startTime,
                    end = end,
                    isImportant = isImportant
                )
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 8.dp
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (initialTask == null) {
                                stringResource(id = R.string.new_task)
                            } else {
                                stringResource(id = R.string.edit_task)
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { handleSave() },
                            enabled = scheduleError == null && title.isNotBlank()
                        ) {
                            Text(text = stringResource(id = R.string.save))
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (saveError != null) saveError = null
                    },
                    label = { Text(text = stringResource(id = R.string.field_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        if (saveError != null && saveError != scheduleError) saveError = null
                    },
                    label = { Text(text = stringResource(id = R.string.field_description)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Surface(
                    onClick = { isDatePickerVisible = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = null)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.field_date),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dateFormatter.format(selectedDate.toJavaLocalDate()),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                TypeSelector(selected = type, onSelect = { type = it })
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WheelTimePicker(
                        label = stringResource(id = R.string.field_start),
                        hour = startHour,
                        minute = startMinute,
                        onHourChanged = { startHour = it },
                        onMinuteChanged = { startMinute = it },
                        modifier = Modifier.weight(1f)
                    )
                    if (type == TaskType.INTERVAL) {
                        WheelTimePicker(
                            label = stringResource(id = R.string.field_end),
                            hour = endHour,
                            minute = endMinute,
                            onHourChanged = { endHour = it },
                            onMinuteChanged = { endMinute = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                ImportantSelector(isImportant = isImportant, onChanged = { isImportant = it })
                scheduleError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                saveError?.takeIf { it != scheduleError }?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    if (isDatePickerVisible) {
        DatePickerDialog(
            onDismissRequest = { isDatePickerVisible = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        selectedDate = selectedMillis.toLocalDate()
                    }
                    isDatePickerVisible = false
                }) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { isDatePickerVisible = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeSelector(selected: TaskType, onSelect: (TaskType) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        TaskType.values().forEachIndexed { index, type ->
            SegmentedButton(
                selected = selected == type,
                onClick = { onSelect(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = TaskType.values().size)
            ) {
                Text(
                    text = if (type == TaskType.POINT) {
                        stringResource(id = R.string.type_point)
                    } else {
                        stringResource(id = R.string.type_interval)
                    }
                )
            }
        }
    }
}

@Composable
private fun ImportantSelector(isImportant: Boolean, onChanged: (Boolean) -> Unit) {
    FilterChip(
        selected = isImportant,
        onClick = { onChanged(!isImportant) },
        label = { Text(text = stringResource(id = R.string.field_important)) },
        leadingIcon = { Icon(imageVector = Icons.Filled.PriorityHigh, contentDescription = null) },
        colors = FilterChipDefaults.filterChipColors(
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,            // ← было leadingIconColor
            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onErrorContainer
        )
    )
}


@Composable
private fun WheelTimePicker(
    label: String,
    hour: Int,
    minute: Int,
    onHourChanged: (Int) -> Unit,
    onMinuteChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onSurface // или Color.White, если строго

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.titleSmall)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumberWheel(
                range = 0..23,
                value = hour,
                onValueChange = onHourChanged,
                modifier = Modifier.weight(1f),
                textColor = textColor
            )
            Text(
                text = ":",
                style = MaterialTheme.typography.titleLarge,
                color = textColor
            )
            NumberWheel(
                range = 0..59,
                value = minute,
                onValueChange = onMinuteChanged,
                modifier = Modifier.weight(1f),
                textColor = textColor
            )
        }
    }
}

@Composable
private fun NumberWheel(
    range: IntRange,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface // по умолчанию из темы
) {
    AndroidView(
        modifier = modifier
            .height(140.dp)
            .clipToBounds()
        ,
        factory = { context ->
            NumberPicker(context).apply {
                minValue = range.first
                maxValue = range.last
                this.value = value.coerceIn(range)
                wrapSelectorWheel = true
                descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                setFormatter { "%02d".format(it) }
                setOnValueChangedListener { _, _, newVal -> onValueChange(newVal) }

                // === БЕЗОПАСНАЯ стилизация ===
                // 1. Цвет текста (API 29+)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    setTextColor(textColor.toArgb())
                }

                // 2. Принудительно белый текст для EditText (всегда работает)
                children.forEach { child ->
                    if (child is EditText) {
                        child.setTextColor(textColor.toArgb())
                        child.highlightColor = textColor.copy(alpha = 0.3f).toArgb()
                    }
                }

                // 3. Отключаем стандартную анимацию и фон
                setBackgroundColor(Color.Transparent.toArgb())
            }
        },
        update = { picker ->
            if (picker.minValue != range.first || picker.maxValue != range.last) {
                picker.minValue = range.first
                picker.maxValue = range.last
            }
            if (picker.value != value.coerceIn(range)) {
                picker.value = value.coerceIn(range)
            }

            // Обновляем цвет при смене темы
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                picker.setTextColor(textColor.toArgb())
            }
            picker.children.forEach { child ->
                if (child is EditText) {
                    child.setTextColor(textColor.toArgb())
                }
            }
        }
    )
}

private fun defaultEndFor(start: LocalTime): LocalTime {
    return if (start.hour == 23) {
        if (start.minute >= 59) start else LocalTime(23, 59)
    } else {
        LocalTime((start.hour + 1).coerceAtMost(23), start.minute)
    }
}

private fun LocalDate.toEpochMillis(): Long =
    this.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

private fun Long.toLocalDate(): LocalDate =
    Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
