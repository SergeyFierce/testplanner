package com.sergeyfierce.testplanner.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sergeyfierce.testplanner.R
import com.sergeyfierce.testplanner.domain.model.CalendarMode
import com.sergeyfierce.testplanner.domain.model.Task
import com.sergeyfierce.testplanner.domain.model.TaskType
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.minus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    var editorParentId by remember { mutableStateOf<String?>(null) }
    var editorInitialType by remember { mutableStateOf(TaskType.POINT) }

    val parentCandidates = uiState.dayTasks.filter { it.isMainActivity }
    val isToday = uiState.currentDate == today()

    if (isEditorVisible) {
        TaskEditorDialog(
            initialTask = editingTask,
            defaultDate = uiState.currentDate,
            parentCandidates = parentCandidates,
            defaultStart = editorDefaultStart,
            initialParentId = editorParentId,
            initialType = editorInitialType,
            onDismiss = {
                isEditorVisible = false
                editingTask = null
                editorDefaultStart = null
                editorParentId = null
                editorInitialType = TaskType.POINT
            },
            onCreate = { parentId, title, description, date, type, start, end, isImportant ->
                viewModel.createTask(parentId, title, description, date, type, start, end, isImportant)
                isEditorVisible = false
                editingTask = null
                editorDefaultStart = null
                editorParentId = null
                editorInitialType = TaskType.POINT
            },
            onUpdate = { task ->
                viewModel.updateTask(task)
                isEditorVisible = false
                editingTask = null
                editorDefaultStart = null
                editorParentId = null
                editorInitialType = TaskType.POINT
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CalendarTopBar(
                currentDate = uiState.currentDate,
                onPrevious = viewModel::goToPrevious,
                onNext = viewModel::goToNext,
                onToday = viewModel::goToToday
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingTask = null
                editorDefaultStart = null
                editorParentId = null
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
                    date = uiState.currentDate,
                    tasks = uiState.dayTasks,
                    isToday = isToday,
                    onToggle = viewModel::onToggleTask,
                    onAddFromSlot = { start ->
                        editorDefaultStart = start
                        editorParentId = null
                        editorInitialType = TaskType.POINT
                        isEditorVisible = true
                    },
                    onAddChild = { task ->
                        editingTask = null
                        editorParentId = task.id
                        editorInitialType = TaskType.POINT
                        editorDefaultStart = task.start
                        isEditorVisible = true
                    },
                    onEdit = { task ->
                        editingTask = task
                        editorParentId = task.parentId
                        editorInitialType = task.type
                        isEditorVisible = true
                    },
                    onDelete = viewModel::onDeleteTask
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
}

@Composable
private fun CalendarTopBar(
    currentDate: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()) }
    TopAppBar(
        title = {
            Text(text = formatter.format(currentDate.toJavaLocalDate()))
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
    date: LocalDate,
    tasks: List<Task>,
    isToday: Boolean,
    onToggle: (Task, Boolean) -> Unit,
    onAddFromSlot: (LocalTime) -> Unit,
    onAddChild: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(isToday) {
        if (isToday) {
            val currentHour = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).time.hour
            listState.scrollToItem(currentHour.coerceIn(0, 23))
        }
    }

    val mainActivities = tasks.filter { it.isMainActivity }.sortedBy { it.start }
    val childTasks = tasks.filter { it.parentId != null }.groupBy { it.parentId }
    val singleTasks = tasks.filter { it.parentId == null && it.type == TaskType.POINT }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        items((0 until 24).toList()) { hour ->
            val hourTime = LocalTime(hour, 0)
            val hourLabel = "%02d:00".format(hour)
            val tasksAtHour = mainActivities.filter { it.start.hour == hour } +
                singleTasks.filter { it.start.hour == hour }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onAddFromSlot(hourTime) }
                    .padding(12.dp)
            ) {
                Text(text = hourLabel, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (tasksAtHour.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.empty_slot_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    tasksAtHour.forEach { task ->
                        when {
                            task.isMainActivity -> MainActivityCard(
                                task = task,
                                children = childTasks[task.id].orEmpty(),
                                onToggle = onToggle,
                                onAddChild = onAddChild,
                                onEdit = onEdit,
                                onDelete = onDelete
                            )

                            else -> PointTaskChip(
                                task = task,
                                onToggle = onToggle,
                                onEdit = onEdit,
                                onDelete = onDelete
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MainActivityCard(
    task: Task,
    children: List<Task>,
    onToggle: (Task, Boolean) -> Unit,
    onAddChild: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    Surface(
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(16.dp),
        color = if (task.isDone) MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = task.isDone,
                    onCheckedChange = { onToggle(task, it) }
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (task.isImportant) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = timeRangeLabel(task),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = { onAddChild(task) }) {
                    Icon(imageVector = Icons.Outlined.TaskAlt, contentDescription = null)
                }
                IconButton(onClick = { onEdit(task) }) {
                    Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = null)
                }
                IconButton(onClick = { onDelete(task) }) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
                }
            }
            if (!task.description.isNullOrBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (children.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    children.sortedBy { it.start }.forEach { child ->
                        ChildTaskRow(task = child, onToggle = onToggle, onEdit = onEdit, onDelete = onDelete)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChildTaskRow(
    task: Task,
    onToggle: (Task, Boolean) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Checkbox(checked = task.isDone, onCheckedChange = { onToggle(task, it) })
        Column(modifier = Modifier.weight(1f)) {
            Text(text = task.title, style = MaterialTheme.typography.bodyMedium)
            Text(text = timeRangeLabel(task), style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = { onEdit(task) }) {
            Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = null)
        }
        IconButton(onClick = { onDelete(task) }) {
            Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
        }
    }
}

@Composable
private fun PointTaskChip(
    task: Task,
    onToggle: (Task, Boolean) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp,
        color = if (task.isImportant) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.secondaryContainer,
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
                Text(text = task.start.toString(), style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { onEdit(task) }) {
                Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = null)
            }
            IconButton(onClick = { onDelete(task) }) {
                Icon(imageVector = Icons.Outlined.Delete, contentDescription = null)
            }
        }
    }
}

@Composable
private fun WeekView(
    currentDate: LocalDate,
    weekTasks: Map<LocalDate, List<Task>>,
    onDayClick: (LocalDate) -> Unit
) {
    val start = startOfWeek(currentDate)
    val days = remember(start) { (0..6).map { start.plus(DatePeriod(days = it)) } }
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(days) { date ->
            val tasksForDay = weekTasks[date].orEmpty()
            val isToday = date == today()
            Surface(
                onClick = { onDayClick(date) },
                shape = RoundedCornerShape(12.dp),
                color = when {
                    date == currentDate -> MaterialTheme.colorScheme.primaryContainer
                    isToday -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "${date.dayOfMonth} ${date.month.name.take(3)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    tasksForDay.take(3).forEach { task ->
                        Text(
                            text = "${task.start} • ${task.title}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                    if (tasksForDay.size > 3) {
                        Text(
                            text = stringResource(id = R.string.more_tasks, tasksForDay.size - 3),
                            style = MaterialTheme.typography.labelSmall
                        )
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
    val totalDays = yearMonth.lengthOfMonth()
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
            val isCurrentMonth = date.monthNumber == currentDate.monthNumber
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
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCurrentMonth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (tasksForDay.isNotEmpty()) {
                            BadgedBox(badge = {
                                Badge { Text(text = tasksForDay.size.toString()) }
                            }) {
                                Spacer(modifier = Modifier.height(0.dp))
                            }
                        }
                        if (hasImportant) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskEditorDialog(
    initialTask: Task?,
    defaultDate: LocalDate,
    parentCandidates: List<Task>,
    defaultStart: LocalTime?,
    initialParentId: String?,
    initialType: TaskType,
    onDismiss: () -> Unit,
    onCreate: (String?, String, String?, LocalDate, TaskType, LocalTime, LocalTime?, Boolean) -> Unit,
    onUpdate: (Task) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialTask?.title.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(initialTask?.description.orEmpty()) }
    var dateInput by rememberSaveable { mutableStateOf((initialTask?.date ?: defaultDate).toString()) }
    var type by rememberSaveable { mutableStateOf(initialTask?.type ?: initialType) }
    var startInput by rememberSaveable { mutableStateOf(initialTask?.start?.toString() ?: defaultStart?.toString().orEmpty()) }
    var endInput by rememberSaveable { mutableStateOf(initialTask?.end?.toString().orEmpty()) }
    var parentId by rememberSaveable { mutableStateOf(initialTask?.parentId ?: initialParentId) }
    var isImportant by rememberSaveable { mutableStateOf(initialTask?.isImportant ?: false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                if (title.isBlank()) {
                    errorMessage = stringResource(id = R.string.error_title_required)
                    return@Button
                }
                val parsedDate = runCatching { LocalDate.parse(dateInput) }.getOrNull()
                if (parsedDate == null) {
                    errorMessage = stringResource(id = R.string.error_date_required)
                    return@Button
                }
                val start = startInput.ifBlank { null }
                val end = endInput.ifBlank { null }
                val parsedStart = start?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
                val parsedEnd = end?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
                if (parsedStart == null) {
                    errorMessage = stringResource(id = R.string.error_time_required)
                    return@Button
                }
                if (type == TaskType.INTERVAL && parsedEnd == null) {
                    errorMessage = stringResource(id = R.string.error_end_required)
                    return@Button
                }
                if (initialTask == null) {
                    onCreate(
                        parentId,
                        title,
                        description.takeIf { it.isNotBlank() },
                        parsedDate,
                        type,
                        parsedStart,
                        parsedEnd,
                        isImportant
                    )
                } else {
                    onUpdate(
                        initialTask.copy(
                            title = title,
                            description = description.takeIf { it.isNotBlank() },
                            date = parsedDate,
                            type = type,
                            start = parsedStart,
                            end = parsedEnd,
                            parentId = parentId,
                            isImportant = isImportant
                        )
                    )
                }
            }) {
                Text(text = stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        title = {
            Text(text = if (initialTask == null) stringResource(id = R.string.new_task) else stringResource(id = R.string.edit_task))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(text = stringResource(id = R.string.field_title)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = stringResource(id = R.string.field_description)) }
                )
                OutlinedTextField(
                    value = dateInput,
                    onValueChange = { dateInput = it },
                    label = { Text(text = stringResource(id = R.string.field_date)) },
                    placeholder = { Text(text = "YYYY-MM-DD") },
                    singleLine = true
                )
                TypeSelector(selected = type, onSelect = { type = it })
                TimeField(
                    label = stringResource(id = R.string.field_start),
                    value = startInput,
                    onValueChange = { startInput = it }
                )
                AnimatedVisibility(visible = type == TaskType.INTERVAL, enter = fadeIn(), exit = fadeOut()) {
                    TimeField(
                        label = stringResource(id = R.string.field_end),
                        value = endInput,
                        onValueChange = { endInput = it }
                    )
                }
                ImportantSelector(isImportant = isImportant, onChanged = { isImportant = it })
                ParentSelector(
                    parentCandidates = parentCandidates,
                    selectedParentId = parentId,
                    onParentSelected = { parentId = it },
                    initialTask = initialTask
                )
                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@Composable
private fun TypeSelector(selected: TaskType, onSelect: (TaskType) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        TaskType.values().forEachIndexed { index, type ->
            SegmentedButton(
                selected = selected == type,
                onClick = { onSelect(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = TaskType.values().size)
            ) {
                Text(text = if (type == TaskType.POINT) stringResource(id = R.string.type_point) else stringResource(id = R.string.type_interval))
            }
        }
    }
}

@Composable
private fun TimeField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        placeholder = { Text(text = "HH:mm") },
        singleLine = true
    )
}

@Composable
private fun ImportantSelector(isImportant: Boolean, onChanged: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = isImportant, onCheckedChange = onChanged)
        Text(text = stringResource(id = R.string.field_important))
    }
}

@Composable
private fun ParentSelector(
    parentCandidates: List<Task>,
    selectedParentId: String?,
    onParentSelected: (String?) -> Unit,
    initialTask: Task?
) {
    val options = parentCandidates.filter { it.id != initialTask?.id }
    Column {
        Text(text = stringResource(id = R.string.field_parent))
        Spacer(modifier = Modifier.height(4.dp))
        AssistChip(
            onClick = {
                onParentSelected(null)
            },
            label = { Text(text = stringResource(id = R.string.no_parent)) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (selectedParentId == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        options.forEach { parent ->
            AssistChip(
                onClick = { onParentSelected(parent.id) },
                label = { Text(text = parent.title) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selectedParentId == parent.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

private fun timeRangeLabel(task: Task): String {
    val endPart = task.end?.let { " - $it" } ?: ""
    return "${task.start}$endPart"
}

private fun startOfWeek(date: LocalDate): LocalDate {
    val isoDay = date.dayOfWeek.isoDayNumber
    return date.minus(DatePeriod(days = isoDay - 1))
}

private fun today(): LocalDate = Clock.System.now()
    .toLocalDateTime(TimeZone.currentSystemDefault()).date
