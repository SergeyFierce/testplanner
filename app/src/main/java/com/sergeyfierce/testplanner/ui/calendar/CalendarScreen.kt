package com.sergeyfierce.testplanner.ui.calendar

import android.widget.EditText
import android.widget.NumberPicker
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
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
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                AnimatedCalendarContent(
                    mode = uiState.selectedMode,
                    dayContent = {
                        DayTimeline(
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
                    },
                    weekContent = {
                        WeekView(
                            currentDate = uiState.currentDate,
                            weekTasks = uiState.weekTasks,
                            onDayClick = viewModel::onDaySelected
                        )
                    },
                    monthContent = {
                        MonthView(
                            currentDate = uiState.currentDate,
                            monthTasks = uiState.monthTasks,
                            onDayClick = viewModel::onDaySelected
                        )
                    }
                )
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
                    Text(text = stringResource(id = R.string.dialog_delete_message))
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    onClick = onDateClick,
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .widthIn(min = 140.dp, max = 240.dp)
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = formatter.format(currentDate.toJavaLocalDate()),
                            modifier = Modifier.widthIn(max = 160.dp),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onNext) {
                    Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = null)
                }
            }
        },
        actions = {
            TextButton(onClick = onToday) {
                Text(text = stringResource(id = R.string.today))
            }
        },
        navigationIcon = {},
        windowInsets = WindowInsets(0, 0, 0, 0)
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedCalendarContent(
    mode: CalendarMode,
    dayContent: @Composable () -> Unit,
    weekContent: @Composable () -> Unit,
    monthContent: @Composable () -> Unit
) {
    AnimatedContent(
        targetState = mode,
        transitionSpec = {
            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
            (slideInHorizontally(animationSpec = tween(), initialOffsetX = { it * direction }) + fadeIn())
                .togetherWith(
                    slideOutHorizontally(animationSpec = tween(), targetOffsetX = { -it * direction }) + fadeOut()
                )
        }, label = "calendar-mode"
    ) { state ->
        when (state) {
            CalendarMode.DAY -> dayContent()
            CalendarMode.WEEK -> weekContent()
            CalendarMode.MONTH -> monthContent()
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
        }
    }
}

private sealed interface DayTimelineItem {
    data class FreeTime(val start: LocalTime, val endExclusive: LocalTime?) : DayTimelineItem
    data class TaskBlock(val task: Task, val nestedPoints: List<Task> = emptyList()) : DayTimelineItem
}

private const val MIN_FREE_SLOT_MINUTES = 60

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
                result.addFreeTimeIfNeeded(cursor, start)
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
                result.addFreeTimeIfNeeded(cursor, start)
            }
            result += DayTimelineItem.TaskBlock(task = task)
            if (start > cursor) {
                cursor = start
            }
        }
    }

    if (cursor < LocalTime(23, 59)) {
        result.addFreeTimeIfNeeded(cursor, null)
    }

    return result
}

private fun MutableList<DayTimelineItem>.addFreeTimeIfNeeded(start: LocalTime, endExclusive: LocalTime?) {
    if (endExclusive != null) {
        if (start >= endExclusive) return
        if (minutesBetween(start, endExclusive) < MIN_FREE_SLOT_MINUTES) return
    }
    this += DayTimelineItem.FreeTime(start, endExclusive)
}

private fun minutesBetween(start: LocalTime, end: LocalTime): Int {
    val startMinutes = start.hour * 60 + start.minute
    val endMinutes = end.hour * 60 + end.minute
    return endMinutes - startMinutes
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
private fun taskContainerColor(task: Task, useIntervalStyle: Boolean = false): Color = when {
    task.isDone -> TaskDoneGreen
    task.isImportant -> MaterialTheme.colorScheme.errorContainer
    task.isInterval || useIntervalStyle -> MaterialTheme.colorScheme.primaryContainer
    else -> MaterialTheme.colorScheme.secondaryContainer
}

@Composable
private fun taskContentColor(task: Task, useIntervalStyle: Boolean = false): Color = when {
    task.isDone -> Color.White
    task.isImportant -> MaterialTheme.colorScheme.onErrorContainer
    task.isInterval || useIntervalStyle -> MaterialTheme.colorScheme.onPrimaryContainer
    else -> MaterialTheme.colorScheme.onSecondaryContainer
}
@Composable
private fun IntervalTaskCard(
    task: Task,
    nestedPoints: List<Task>,
    onToggle: ((Task, Boolean) -> Unit)? = null,
    onEdit: ((Task) -> Unit)? = null,
    onDelete: ((Task) -> Unit)? = null,
    showActions: Boolean = onToggle != null
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
                    onCheckedChange = { onToggle?.invoke(task, it) },
                    enabled = showActions
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
                if (showActions) {
                    IconButton(onClick = { onEdit?.invoke(task) }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                    }
                    IconButton(onClick = { onDelete?.invoke(task) }) {
                        Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
                    }
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
                            onDelete = onDelete,
                            showActions = showActions
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
    onToggle: ((Task, Boolean) -> Unit)? = null,
    onEdit: ((Task) -> Unit)? = null,
    onDelete: ((Task) -> Unit)? = null,
    showActions: Boolean = onToggle != null
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp,
        color = taskContainerColor(task, useIntervalStyle = true),
        contentColor = taskContentColor(task, useIntervalStyle = true),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { onToggle?.invoke(task, it) },
                enabled = showActions
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.bodyMedium)
                Text(text = timeRangeLabel(task), style = MaterialTheme.typography.bodySmall)
            }
            if (showActions) {
                IconButton(onClick = { onEdit?.invoke(task) }) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                }
                IconButton(onClick = { onDelete?.invoke(task) }) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun NestedPointTaskCard(
    task: Task,
    onToggle: ((Task, Boolean) -> Unit)? = null,
    onEdit: ((Task) -> Unit)? = null,
    onDelete: ((Task) -> Unit)? = null,
    showActions: Boolean = onToggle != null
) {
    val (containerColor, contentColor, border) = when {
        task.isDone -> Triple(TaskDoneGreen, Color.White, null)
        task.isImportant -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            null
        )
        else -> Triple(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.onSurface,
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = if (task.isImportant || task.isDone) 0.dp else 1.dp,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { onToggle?.invoke(task, it) },
                enabled = showActions
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.bodyMedium)
                Text(text = task.start.toTimeLabel(), style = MaterialTheme.typography.bodySmall)
            }
            if (showActions) {
                IconButton(onClick = { onEdit?.invoke(task) }) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                }
                IconButton(onClick = { onDelete?.invoke(task) }) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun ImportantDot(
    visible: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.error
) {
    if (visible) {
        Box(
            modifier = modifier
                .size(10.dp)
                .background(color = color, shape = CircleShape)
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
            val dayContainerColor = when {
                isSelected -> MaterialTheme.colorScheme.surface
                isToday -> MaterialTheme.colorScheme.surface
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val dayBorder = when {
                isSelected -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                isToday -> BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                else -> null
            }
            Surface(
                onClick = { onDayClick(date) },
                shape = RoundedCornerShape(16.dp),
                color = dayContainerColor,
                tonalElevation = if (isSelected || isToday) 4.dp else 1.dp,
                border = dayBorder,
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
                        val timelineItems = remember(tasksForDay) {
                            buildTimeline(tasksForDay).filterIsInstance<DayTimelineItem.TaskBlock>()
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            timelineItems.forEach { item ->
                                if (item.task.isInterval) {
                                    IntervalTaskCard(
                                        task = item.task,
                                        nestedPoints = item.nestedPoints,
                                        showActions = false
                                    )
                                } else {
                                    PointTaskCard(
                                        task = item.task,
                                        showActions = false
                                    )
                                }
                            }
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
                border = if (isCurrentMonth) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                        modifier = Modifier.align(Alignment.TopEnd),
                        color = Color(0xFFD32F2F)
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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

    val nowTime = remember {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
        LocalTime(now.hour, now.minute)
    }
    val initialStart = remember(initialTask, defaultStart, nowTime) {
        initialTask?.start ?: defaultStart ?: nowTime
    }
    val initialEndCandidate = remember(initialTask, initialStart) {
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
                    },
                    windowInsets = WindowInsets(0, 0, 0, 0)
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
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
                AnimatedContent(
                    targetState = type,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween()) togetherWith fadeOut(animationSpec = tween()))
                    },
                    label = "task-type-time-pickers"
                ) { currentType ->
                    if (currentType == TaskType.INTERVAL) {
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
                            WheelTimePicker(
                                label = stringResource(id = R.string.field_end),
                                hour = endHour,
                                minute = endMinute,
                                onHourChanged = { endHour = it },
                                onMinuteChanged = { endMinute = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        WheelTimePicker(
                            label = stringResource(id = R.string.field_start),
                            hour = startHour,
                            minute = startMinute,
                            onHourChanged = { startHour = it },
                            onMinuteChanged = { startMinute = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                ImportantSelector(isImportant = isImportant, onChanged = { isImportant = it })
                scheduleError?.let {
                    ErrorMessageCard(text = it)
                }
                saveError?.takeIf { it != scheduleError }?.let {
                    ErrorMessageCard(text = it)
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
    val statusText = if (isImportant) {
        stringResource(id = R.string.task_status_important)
    } else {
        stringResource(id = R.string.task_status_regular)
    }
    Surface(
        onClick = { onChanged(!isImportant) },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.field_important),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            FilterChip(
                selected = isImportant,
                onClick = { onChanged(!isImportant) },
                label = {
                    Text(text = statusText)
                },
                leadingIcon = if (isImportant) {
                    {
                        Icon(imageVector = Icons.Filled.PriorityHigh, contentDescription = null)
                    }
                } else {
                    null
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.error,
                    selectedLabelColor = MaterialTheme.colorScheme.onError,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onError
                )
            )
        }
    }
}

@Composable
private fun ErrorMessageCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onError,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
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
    val textColor = MaterialTheme.colorScheme.onSurface

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
    val totalMinutes = start.hour * 60 + start.minute + 1
    val clamped = totalMinutes.coerceAtMost(23 * 60 + 59)
    return LocalTime(clamped / 60, clamped % 60)
}

private fun LocalDate.toEpochMillis(): Long =
    this.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

private fun Long.toLocalDate(): LocalDate =
    Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
