package com.sergeyfierce.testplanner.ui.calendar

import android.widget.EditText
import android.widget.NumberPicker
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.children
import com.sergeyfierce.testplanner.R
import com.sergeyfierce.testplanner.domain.model.CalendarMode
import com.sergeyfierce.testplanner.domain.model.Task
import com.sergeyfierce.testplanner.domain.model.TaskType
import com.sergeyfierce.testplanner.ui.theme.TaskDoneGreen
import com.sergeyfierce.testplanner.ui.theme.TaskDoneNestedGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.random.Random
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
    var selectedTaskIds by remember { mutableStateOf(setOf<String>()) }
    var isSelectionModeActive by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val isToday = uiState.currentDate == today()
    val isSelectionMode = isSelectionModeActive
    val showTodayButton = !isToday
    val selectedTasks = remember(uiState.dayTasks, selectedTaskIds) {
        uiState.dayTasks.filter { selectedTaskIds.contains(it.id) }
    }
    val cannotEditMessage = stringResource(id = R.string.edit_completed_not_allowed)

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

    fun openEditor(task: Task) {
        editingTask = task
        editorInitialType = task.type
        editorDefaultStart = task.start
        isEditorVisible = true
    }

    BackHandler(enabled = isEditorVisible) {
        resetEditorState()
    }

    BackHandler(enabled = isSelectionMode && !isEditorVisible) {
        selectedTaskIds = emptySet()
        isSelectionModeActive = false
    }

    LaunchedEffect(uiState.dayTasks) {
        val availableIds = uiState.dayTasks.map { it.id }.toSet()
        val filtered = selectedTaskIds.filter { availableIds.contains(it) }.toSet()
        if (filtered.size != selectedTaskIds.size) {
            selectedTaskIds = filtered
        }
        if (availableIds.isEmpty()) {
            isSelectionModeActive = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        val canEditSelection = selectedTasks.size == 1 && selectedTasks.firstOrNull()?.isDone == false
        val allTaskIds = remember(uiState.dayTasks) { uiState.dayTasks.map { it.id } }
        val allSelected = allTaskIds.isNotEmpty() && selectedTaskIds.containsAll(allTaskIds)
        val hasSelectableTasks = allTaskIds.isNotEmpty()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            snackbarHost = {},
            topBar = {
                AnimatedContent(
                    targetState = isSelectionMode,
                    transitionSpec = {
                        (slideInVertically(animationSpec = tween(durationMillis = 250)) { fullHeight ->
                            if (targetState) -fullHeight else fullHeight
                        } + fadeIn()) togetherWith
                            (slideOutVertically(animationSpec = tween(durationMillis = 250)) { fullHeight ->
                                if (targetState) fullHeight else -fullHeight
                            } + fadeOut())
                    },
                    label = "selection-top-bar"
                ) { selectionActive ->
                    if (selectionActive) {
                        SelectionTopBar(
                            selectedCount = selectedTasks.size,
                            canEdit = canEditSelection,
                            allSelected = allSelected,
                            hasSelectableTasks = hasSelectableTasks,
                            onToggleSelectAll = {
                                if (allSelected) {
                                    selectedTaskIds = emptySet()
                                } else {
                                    selectedTaskIds = allTaskIds.toSet()
                                    isSelectionModeActive = true
                                }
                            },
                            onCancel = {
                                selectedTaskIds = emptySet()
                                isSelectionModeActive = false
                            },
                            onEdit = {
                                selectedTasks.firstOrNull()?.let { task ->
                                    selectedTaskIds = emptySet()
                                    isSelectionModeActive = false
                                    openEditor(task)
                                }
                            },
                            onDelete = { showDeleteConfirmation = true }
                        )
                    } else {
                        CalendarTopBar(
                            currentDate = uiState.currentDate,
                            onPrevious = viewModel::goToPrevious,
                            onNext = viewModel::goToNext,
                            onDateClick = { isDatePickerVisible = true }
                        )
                    }
                }
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = if (uiState.selectedMode == CalendarMode.DAY) {
                null
            } else {
                {
                    val bottomBarReserve = 96.dp // можно подстроить на 88–104.dp под вкус

                    FloatingActionButton(
                        onClick = {
                            selectedTaskIds = emptySet()
                            isSelectionModeActive = false
                            editingTask = null
                            editorDefaultStart = null
                            editorInitialType = TaskType.POINT
                            isEditorVisible = true
                        },
                        modifier = Modifier
                            .padding(bottom = bottomBarReserve) // ⬅ вместо navigationBarsPadding()
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedVisibility(visible = showTodayButton) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FilledTonalButton(
                            onClick = viewModel::goToToday,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .heightIn(min = 40.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            Text(text = stringResource(id = R.string.go_to_today))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                CalendarModeSelector(
                    selectedMode = uiState.selectedMode,
                    onModeSelected = viewModel::onModeSelected
                )
                Spacer(modifier = Modifier.height(16.dp))
                fun toggleSelection(task: Task) {
                    val updated = selectedTaskIds.toMutableSet()
                    if (!updated.add(task.id)) {
                        updated.remove(task.id)
                    }
                    selectedTaskIds = updated
                    if (updated.isNotEmpty()) {
                        isSelectionModeActive = true
                    }
                }

                fun handleTaskClick(task: Task) {
                    if (isSelectionMode) {
                        toggleSelection(task)
                    } else if (task.isDone) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(cannotEditMessage)
                        }
                    } else {
                        openEditor(task)
                    }
                }

                fun handleTaskLongPress(task: Task) {
                    if (isSelectionMode) {
                        toggleSelection(task)
                    } else {
                        selectedTaskIds = setOf(task.id)
                        isSelectionModeActive = true
                    }
                }

                AnimatedCalendarContent(
                    mode = uiState.selectedMode,
                    currentDate = uiState.currentDate,
                    dayContent = {
                        DayTimeline(
                            tasks = uiState.dayTasks,
                            isToday = isToday,
                            onToggle = if (isSelectionMode) null else viewModel::onToggleTask,
                            selectedTaskIds = selectedTaskIds,
                            onTaskClick = { task -> handleTaskClick(task) },
                            onTaskLongPress = { task -> handleTaskLongPress(task) },
                            onAddTaskAt = { time ->
                                selectedTaskIds = emptySet()
                                isSelectionModeActive = false
                                editingTask = null
                                editorDefaultStart = time
                                editorInitialType = TaskType.POINT
                                isEditorVisible = true
                            }
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

        AnimatedVisibility(
            visible = isEditorVisible,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(
                    durationMillis = 260,
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(animationSpec = tween(durationMillis = 200)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(
                    durationMillis = 220,
                    easing = FastOutSlowInEasing
                )
            ) + fadeOut(animationSpec = tween(durationMillis = 200)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp)
        )

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

        if (showDeleteConfirmation && selectedTasks.isNotEmpty()) {
            val count = selectedTasks.size
            val title = if (count > 1) {
                stringResource(id = R.string.dialog_delete_selected_title)
            } else {
                stringResource(id = R.string.dialog_delete_title)
            }
            val message = if (count > 1) {
                pluralStringResource(id = R.plurals.dialog_delete_selected_message, count, count)
            } else {
                stringResource(id = R.string.dialog_delete_message)
            }
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text(text = title) },
                text = { Text(text = message) },
                confirmButton = {
                    TextButton(onClick = {
                        selectedTasks.forEach { task ->
                            viewModel.onDeleteTask(task)
                        }
                        showDeleteConfirmation = false
                        selectedTaskIds = emptySet()
                        isSelectionModeActive = false
                    }) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) {
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
    onDateClick: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()) }
    val dateShape = RoundedCornerShape(24.dp)
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick = onDateClick,
                    shape = dateShape,
                    tonalElevation = 2.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = formatter.format(currentDate.toJavaLocalDate()),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(imageVector = Icons.Filled.KeyboardArrowLeft, contentDescription = null)
            }
        },
        actions = {
            IconButton(
                onClick = onNext,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(imageVector = Icons.Filled.KeyboardArrowRight, contentDescription = null)
            }
        },
        windowInsets = WindowInsets(0, 0, 0, 0)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    canEdit: Boolean,
    allSelected: Boolean,
    hasSelectableTasks: Boolean,
    onToggleSelectAll: () -> Unit,
    onCancel: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = {
            Text(text = stringResource(id = R.string.selection_count, selectedCount))
        },
        navigationIcon = {
            IconButton(onClick = onCancel) {
                Icon(imageVector = Icons.Filled.Close, contentDescription = stringResource(id = R.string.action_cancel))
            }
        },
        actions = {
            if (selectedCount == 1) {
                IconButton(onClick = onEdit, enabled = canEdit) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = stringResource(id = R.string.edit_task))
                }
            }
            if (selectedCount > 0) {
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = stringResource(id = R.string.action_delete))
                }
                Checkbox(
                    checked = allSelected && selectedCount > 0,
                    onCheckedChange = { onToggleSelectAll() },
                    enabled = hasSelectableTasks
                )
            }
        },
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
    currentDate: LocalDate,
    dayContent: @Composable () -> Unit,
    weekContent: @Composable () -> Unit,
    monthContent: @Composable () -> Unit
) {
    AnimatedContent(
        targetState = mode to currentDate,
        transitionSpec = {
            val (initialMode, initialDate) = initialState
            val (targetMode, targetDate) = targetState
            val slideSpec = tween<IntOffset>(durationMillis = 320)
            val fadeSpec = tween<Float>(durationMillis = 220)

            if (initialMode == targetMode) {
                val comparison = targetDate.compareTo(initialDate)
                if (comparison == 0) {
                    fadeIn(animationSpec = fadeSpec) togetherWith fadeOut(animationSpec = fadeSpec)
                } else {
                    val direction = if (comparison > 0) 1 else -1
                    (
                            slideInHorizontally(
                                animationSpec = slideSpec,
                                initialOffsetX = { fullWidth -> fullWidth * direction }
                            ) + fadeIn(animationSpec = fadeSpec)
                            ).togetherWith(
                            slideOutHorizontally(
                                animationSpec = slideSpec,
                                targetOffsetX = { fullWidth -> -fullWidth * direction }
                            ) + fadeOut(animationSpec = fadeSpec)
                        )
                }
            } else {
                val direction = if (targetMode.ordinal > initialMode.ordinal) 1 else -1
                (
                        slideInHorizontally(
                            animationSpec = slideSpec,
                            initialOffsetX = { fullWidth -> fullWidth * direction }
                        ) + fadeIn(animationSpec = fadeSpec)
                        ).togetherWith(
                        slideOutHorizontally(
                            animationSpec = slideSpec,
                            targetOffsetX = { fullWidth -> -fullWidth * direction }
                        ) + fadeOut(animationSpec = fadeSpec)
                    )
            }
        }, label = "calendar-mode-date"
    ) { (stateMode, _) ->
        when (stateMode) {
            CalendarMode.DAY -> dayContent()
            CalendarMode.WEEK -> weekContent()
            CalendarMode.MONTH -> monthContent()
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayTimeline(
    tasks: List<Task>,
    isToday: Boolean,
    onToggle: ((Task, Boolean) -> Unit)?,
    selectedTaskIds: Set<String>,
    onTaskClick: (Task) -> Unit,
    onTaskLongPress: (Task) -> Unit,
    onAddTaskAt: (LocalTime) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var scale by rememberSaveable { mutableFloatStateOf(1f) }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val minuteHeight = remember(scale) { BASE_MINUTE_HEIGHT * scale }
    val minuteHeightPx = with(density) { minuteHeight.toPx() }
    val totalHeight = remember(minuteHeight) { minuteHeight * MINUTES_PER_DAY.toFloat() }

    val sortedTasks = remember(tasks) {
        tasks.sortedWith(compareBy<Task> { it.start }.thenBy { it.title })
    }

    val currentTime by produceState(
        initialValue = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    ) {
        while (true) {
            value = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
            delay(60_000L)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val viewportHeight = maxHeight
        val viewportHeightPx = with(density) { viewportHeight.toPx() }
        val totalHeightPx = with(density) { totalHeight.toPx() }
        val contentHeight = if (totalHeight < viewportHeight) viewportHeight else totalHeight

        fun adjustScale(targetScale: Float, focus: Float? = null) {
            val clamped = targetScale.coerceIn(MIN_ZOOM_SCALE, MAX_ZOOM_SCALE)
            if (clamped == scale) return
            val focusCenter = focus ?: (scrollState.value + viewportHeightPx / 2f)
            val focusFraction = if (totalHeightPx <= 0f) 0f else focusCenter / totalHeightPx
            scale = clamped
            val newMinuteHeightPx = with(density) { (BASE_MINUTE_HEIGHT * clamped).toPx() }
            val newTotalHeightPx = newMinuteHeightPx * MINUTES_PER_DAY
            val targetScroll = (focusFraction * newTotalHeightPx - viewportHeightPx / 2f)
                .coerceIn(0f, (newTotalHeightPx - viewportHeightPx).coerceAtLeast(0f))
            coroutineScope.launch { scrollState.scrollTo(targetScroll.roundToInt()) }
        }

        val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
            if (zoomChange != 1f) {
                adjustScale(scale * zoomChange)
            }
            if (panChange.y != 0f) {
                scrollState.dispatchRawDelta(-panChange.y)
            }
        }

        var autoPositioned by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(isToday, minuteHeight) {
            if (isToday && !autoPositioned) {
                val nowMinutes = currentTime.hour * 60 + currentTime.minute
                val target = (nowMinutes * minuteHeightPx - viewportHeightPx / 2f)
                    .coerceIn(0f, (totalHeightPx - viewportHeightPx).coerceAtLeast(0f))
                scrollState.scrollTo(target.roundToInt())
                autoPositioned = true
            }
        }

        val scrollProgress by remember {
            derivedStateOf {
                val maxScroll = (totalHeightPx - viewportHeightPx).coerceAtLeast(0f)
                if (maxScroll == 0f) 0f else scrollState.value / maxScroll
            }
        }
        val viewportFraction by remember {
            derivedStateOf {
                if (totalHeightPx == 0f) 1f else (viewportHeightPx / totalHeightPx).coerceIn(0f, 1f)
            }
        }

        val addTask: () -> Unit = remember(onAddTaskAt, minuteHeightPx, viewportHeightPx, scrollState.value) {
            {
                val centerMinutes = if (minuteHeightPx == 0f) {
                    0f
                } else {
                    (scrollState.value + viewportHeightPx / 2f) / minuteHeightPx
                }
                val snapped = snapToSmartInterval(centerMinutes, minuteHeightPx)
                onAddTaskAt(minutesToLocalTime(snapped))
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 96.dp)
                    .verticalScroll(scrollState)
                    .transformable(transformableState)
            ) {
                Row(
                    modifier = Modifier
                        .height(contentHeight)
                        .fillMaxWidth()
                ) {
                    TimeLabelColumn(
                        minuteHeight = minuteHeight,
                        totalMinutes = MINUTES_PER_DAY,
                        modifier = Modifier
                            .width(TIME_LABEL_WIDTH)
                            .fillMaxHeight()
                    )
                    TimelineTrack(
                        tasks = sortedTasks,
                        minuteHeight = minuteHeight,
                        isToday = isToday,
                        currentTime = currentTime,
                        onToggle = onToggle,
                        selectedTaskIds = selectedTaskIds,
                        onTaskClick = onTaskClick,
                        onTaskLongPress = onTaskLongPress,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }

            ZoomControlPanel(
                scale = scale,
                onZoomIn = { adjustScale(scale * 1.2f) },
                onZoomOut = { adjustScale(scale / 1.2f) },
                onLevelSelected = { adjustScale(it) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
            )

            AnimatedVisibility(
                visible = viewportFraction < 0.995f,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                DayMiniMap(
                    tasks = sortedTasks,
                    scrollProgress = scrollProgress,
                    viewportFraction = viewportFraction,
                    onSeek = { fraction ->
                        val maxScroll = (totalHeightPx - viewportHeightPx).coerceAtLeast(0f)
                        val target = (fraction * maxScroll).coerceIn(0f, maxScroll)
                        coroutineScope.launch {
                            scrollState.animateScrollTo(target.roundToInt())
                        }
                    }
                )
            }

            FloatingActionButton(
                onClick = addTask,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(id = R.string.timeline_add_task))
            }
        }
    }
}

private sealed interface DayTimelineItem {
    data class FreeTime(val start: LocalTime, val endExclusive: LocalTime?) : DayTimelineItem
    data class TaskBlock(val task: Task, val nestedPoints: List<Task> = emptyList()) : DayTimelineItem
}

private val TIME_LABEL_WIDTH = 64.dp
private val BASE_MINUTE_HEIGHT = 0.75.dp
private val MIN_TASK_HEIGHT = 36.dp
private val MINI_MAP_WIDTH = 96.dp
private val MINI_MAP_HEIGHT = 200.dp
private const val MINUTES_PER_DAY = 24 * 60
private const val DEFAULT_POINT_DURATION_MINUTES = 15
private const val MIN_ZOOM_SCALE = 0.35f
private const val MAX_ZOOM_SCALE = 3.2f

@Composable
private fun TimeLabelColumn(
    minuteHeight: Dp,
    totalMinutes: Int,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val minuteHeightPx = with(density) { minuteHeight.toPx() }
    val stepMinutes = when {
        minuteHeightPx >= 140f -> 15
        minuteHeightPx >= 80f -> 30
        minuteHeightPx >= 35f -> 60
        else -> 120
    }

    Box(modifier = modifier) {
        for (minute in 0 until totalMinutes step stepMinutes) {
            val offset = minuteHeight * minute.toFloat()
            val labelOffset = (offset - 10.dp).coerceAtLeast(0.dp)
            Text(
                text = minutesToLocalTime(minute).toTimeLabel(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = labelOffset)
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun TimelineTrack(
    tasks: List<Task>,
    minuteHeight: Dp,
    isToday: Boolean,
    currentTime: LocalTime,
    onToggle: ((Task, Boolean) -> Unit)?,
    selectedTaskIds: Set<String>,
    onTaskClick: (Task) -> Unit,
    onTaskLongPress: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val minuteHeightPx = with(density) { minuteHeight.toPx() }
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val gridMajorColor = MaterialTheme.colorScheme.outline
    val currentLineColor = MaterialTheme.colorScheme.primary
    val indicatorRadius = with(density) { 6.dp.toPx() }

    val entries = remember(tasks) {
        tasks.map { task ->
            TimelineVisualTask(
                task = task,
                startMinutes = task.start.hour * 60 + task.start.minute,
                durationMinutes = taskDurationMinutes(task)
            )
        }
    }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .clipToBounds()
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val majorStep = computeMajorStep(minuteHeightPx)
            val minorStep = computeMinorStep(minuteHeightPx)
            for (minute in 0..MINUTES_PER_DAY step minorStep) {
                val y = minute * minuteHeightPx
                if (y > size.height) break
                val isMajor = minute % majorStep == 0
                val color = if (isMajor) gridMajorColor else gridColor
                val strokeWidth = if (isMajor) 2f else 1f
                drawLine(
                    color = color,
                    start = Offset(x = 0f, y = y),
                    end = Offset(x = width, y = y),
                    strokeWidth = strokeWidth
                )
            }
        }

        entries.forEach { entry ->
            val topOffset = minuteHeight * entry.startMinutes.toFloat()
            val height = (minuteHeight * entry.durationMinutes.toFloat()).coerceAtLeast(MIN_TASK_HEIGHT)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .offset(y = topOffset)
                    .height(height)
            ) {
                TimelineTaskCard(
                    task = entry.task,
                    onToggle = onToggle,
                    isSelected = selectedTaskIds.contains(entry.task.id),
                    onClick = { onTaskClick(entry.task) },
                    onLongPress = { onTaskLongPress(entry.task) }
                )
            }
        }

        if (isToday) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val nowMinutes = currentTime.hour * 60 + currentTime.minute
                val y = nowMinutes * minuteHeightPx
                if (y in 0f..size.height) {
                    drawLine(
                        color = currentLineColor,
                        start = Offset(x = 0f, y = y),
                        end = Offset(x = size.width, y = y),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = currentLineColor,
                        radius = indicatorRadius,
                        center = Offset(x = indicatorRadius * 1.5f, y = y)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineTaskCard(
    task: Task,
    onToggle: ((Task, Boolean) -> Unit)?,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    val containerColor = taskContainerColor(task, useIntervalStyle = !task.isInterval)
    val contentColor = taskContentColor(task, useIntervalStyle = !task.isInterval)
    val border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    val tonalElevation = if (task.isInterval) 6.dp else 3.dp
    val contentAlpha by animateFloatAsState(
        targetValue = if (task.isDone) 0.55f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "timeline-task-alpha"
    )

    Surface(
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        border = border,
        modifier = Modifier
            .fillMaxSize()
            .clip(shape)
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .alpha(contentAlpha),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = timeRangeLabel(task),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                ImportantDot(visible = task.isImportant)
                if (onToggle != null) {
                    Checkbox(
                        checked = task.isDone,
                        onCheckedChange = { checked -> onToggle(task, checked) }
                    )
                } else if (task.isDone) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            task.description?.takeIf { it.isNotBlank() }?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ZoomControlPanel(
    scale: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onLevelSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val zoomLevels = remember { listOf(0.35f, 0.6f, 0.9f, 1.2f, 1.8f, 2.4f, 3.2f) }
        .map { it.coerceIn(MIN_ZOOM_SCALE, MAX_ZOOM_SCALE) }
        .distinct()
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 6.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onZoomOut, enabled = scale > MIN_ZOOM_SCALE) {
                    Icon(imageVector = Icons.Filled.ZoomOut, contentDescription = stringResource(id = R.string.zoom_out))
                }
                IconButton(onClick = onZoomIn, enabled = scale < MAX_ZOOM_SCALE) {
                    Icon(imageVector = Icons.Filled.ZoomIn, contentDescription = stringResource(id = R.string.zoom_in))
                }
            }
            zoomLevels.chunked(3).forEach { chunk ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    chunk.forEach { level ->
                        val isSelected = isSameZoomLevel(scale, level)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onLevelSelected(level) },
                            label = { Text(text = zoomLabel(level)) },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DayMiniMap(
    tasks: List<Task>,
    scrollProgress: Float,
    viewportFraction: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var dragProgress by remember { mutableFloatStateOf(scrollProgress) }
    LaunchedEffect(scrollProgress) { dragProgress = scrollProgress }

    val draggableState = rememberDraggableState { delta ->
        val heightPx = with(density) { MINI_MAP_HEIGHT.toPx() }
        if (heightPx > 0f) {
            val fractionDelta = delta / heightPx
            dragProgress = (dragProgress + fractionDelta).coerceIn(0f, 1f)
            onSeek(dragProgress)
        }
    }

    val entries = tasks.map { task ->
        MiniMapEntry(
            startMinutes = task.start.hour * 60 + task.start.minute,
            durationMinutes = taskDurationMinutes(task),
            color = taskContainerColor(task)
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 6.dp,
        modifier = modifier
            .width(MINI_MAP_WIDTH)
            .height(MINI_MAP_HEIGHT)
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                startDragImmediately = true,
                onDragStarted = { dragProgress = scrollProgress }
            )
            .pointerInput(scrollProgress) {
                detectTapGestures { offset ->
                    val fraction = if (size.height == 0f) 0f else (offset.y / size.height).coerceIn(0f, 1f)
                    dragProgress = fraction
                    onSeek(fraction)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val viewportHeight = (viewportFraction * height).coerceAtLeast(8f)
            entries.forEach { entry ->
                val top = (entry.startMinutes.toFloat() / MINUTES_PER_DAY) * height
                val blockHeight = (entry.durationMinutes.toFloat() / MINUTES_PER_DAY) * height
                val clampedHeight = blockHeight.coerceAtLeast(3f)
                val topClamped = top.coerceIn(0f, height - clampedHeight)
                drawRoundRect(
                    color = entry.color.copy(alpha = 0.6f),
                    topLeft = Offset(x = width * 0.2f, y = topClamped),
                    size = Size(width * 0.6f, clampedHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
            val indicatorTop = (scrollProgress * height).coerceIn(0f, height - viewportHeight)
            val indicatorColor = MaterialTheme.colorScheme.primary
            drawRoundRect(
                color = indicatorColor.copy(alpha = 0.12f),
                topLeft = Offset(0f, indicatorTop),
                size = Size(width, viewportHeight),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = indicatorColor,
                topLeft = Offset(0f, indicatorTop),
                size = Size(width, viewportHeight),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = 3f)
            )
        }
    }
}

private data class TimelineVisualTask(
    val task: Task,
    val startMinutes: Int,
    val durationMinutes: Int
)

private data class MiniMapEntry(
    val startMinutes: Int,
    val durationMinutes: Int,
    val color: Color
)

private fun computeMajorStep(minuteHeightPx: Float): Int = when {
    minuteHeightPx >= 140f -> 15
    minuteHeightPx >= 80f -> 30
    minuteHeightPx >= 35f -> 60
    else -> 120
}

private fun computeMinorStep(minuteHeightPx: Float): Int = when {
    minuteHeightPx >= 140f -> 5
    minuteHeightPx >= 80f -> 15
    minuteHeightPx >= 35f -> 30
    else -> 60
}

private fun taskDurationMinutes(task: Task): Int {
    val raw = task.end?.let { minutesBetween(task.start, it) } ?: DEFAULT_POINT_DURATION_MINUTES
    return raw.coerceAtLeast(1)
}

private fun minutesToLocalTime(minutes: Int): LocalTime {
    val clamped = minutes.coerceIn(0, MINUTES_PER_DAY - 1)
    val hour = clamped / 60
    val minute = clamped % 60
    return LocalTime(hour, minute)
}

private fun snapToSmartInterval(minutes: Float, minuteHeightPx: Float): Int {
    val step = when {
        minuteHeightPx >= 140f -> 1
        minuteHeightPx >= 80f -> 5
        minuteHeightPx >= 40f -> 10
        else -> 15
    }
    val snapped = ((minutes / step).roundToInt()) * step
    return snapped.coerceIn(0, MINUTES_PER_DAY - 1)
}

private fun isSameZoomLevel(current: Float, target: Float): Boolean = abs(current - target) < 0.05f

private fun zoomLabel(level: Float): String {
    val digits = if (level >= 1f) 1 else 2
    return "×${level.format(digits)}"
}

private fun Float.format(digits: Int): String = String.format(Locale.getDefault(), "%0.${digits}f", this)

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TaskCardContainer(
    task: Task,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    shape: RoundedCornerShape,
    tonalElevation: Dp,
    containerColor: Color,
    contentColor: Color,
    baseBorder: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val border = when {
        isSelected -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else -> baseBorder
    }

    val animatedContainerColor by animateColorAsState(
        targetValue = containerColor,
        animationSpec = tween(durationMillis = 250),
        label = "containerColor"
    )
    val animatedContentColor by animateColorAsState(
        targetValue = contentColor,
        animationSpec = tween(durationMillis = 250),
        label = "contentColor"
    )
    val animatedElevation by animateDpAsState(
        targetValue = tonalElevation,
        animationSpec = tween(durationMillis = 250),
        label = "tonalElevation"
    )

    Surface(
        shape = shape,
        tonalElevation = animatedElevation,
        color = animatedContainerColor,
        contentColor = animatedContentColor,
        border = border,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .animateContentSize(animationSpec = tween(durationMillis = 250))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            )
    ) {
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun TaskHeaderRow(
    task: Task,
    onToggle: ((Task, Boolean) -> Unit)?,
    subtitle: String? = null
) {
    val timeLabel = remember(task.start, task.end) {
        task.end?.let { end ->
            "${task.start.toTimeLabel()} - ${end.toTimeLabel()}"
        } ?: task.start.toTimeLabel()
    }
    val contentAlpha by animateFloatAsState(
        targetValue = if (task.isDone) 0.6f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "taskAlpha"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(contentAlpha),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = timeLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (onToggle != null) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { checked -> onToggle(task, checked) }
            )
        }
    }
}

private data class EditorSnapshot(
    val title: String,
    val description: String,
    val date: LocalDate,
    val type: TaskType,
    val isImportant: Boolean,
    val start: LocalTime,
    val end: LocalTime?
)
@Composable
private fun IntervalTaskCard(
    task: Task,
    nestedPoints: List<Task>,
    onToggle: ((Task, Boolean) -> Unit)?,
    showCheckbox: Boolean,
    isSelected: Boolean,
    selectedTaskIds: Set<String>,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onNestedClick: (Task) -> Unit,
    onNestedLongPress: (Task) -> Unit
) {
    val checkboxHandler = if (showCheckbox) onToggle else null
    TaskCardContainer(
        task = task,
        isSelected = isSelected,
        onClick = onClick,
        onLongPress = onLongPress,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 6.dp,
        containerColor = taskContainerColor(task),
        contentColor = taskContentColor(task)
    ) {
        TaskHeaderRow(
            task = task,
            onToggle = checkboxHandler,
            subtitle = null
        )
        if (!task.description.isNullOrBlank()) {
            Text(
                text = task.description!!,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (nestedPoints.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                nestedPoints.forEach { nested ->
                    NestedPointTaskCard(
                        task = nested,
                        onToggle = checkboxHandler,
                        showCheckbox = showCheckbox,
                        isSelected = selectedTaskIds.contains(nested.id),
                        onClick = { onNestedClick(nested) },
                        onLongPress = { onNestedLongPress(nested) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PointTaskCard(
    task: Task,
    onToggle: ((Task, Boolean) -> Unit)?,
    showCheckbox: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val checkboxHandler = if (showCheckbox) onToggle else null
    TaskCardContainer(
        task = task,
        isSelected = isSelected,
        onClick = onClick,
        onLongPress = onLongPress,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp,
        containerColor = taskContainerColor(task, useIntervalStyle = true),
        contentColor = taskContentColor(task, useIntervalStyle = true)
    ) {
        TaskHeaderRow(task = task, onToggle = checkboxHandler)
        if (!task.description.isNullOrBlank()) {
            Text(
                text = task.description!!,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun NestedPointTaskCard(
    task: Task,
    onToggle: ((Task, Boolean) -> Unit)?,
    showCheckbox: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val checkboxHandler = if (showCheckbox) onToggle else null
    val (containerColor, contentColor, baseBorder) = when {
        task.isDone -> Triple(TaskDoneNestedGreen, Color.White, null)
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

    TaskCardContainer(
        task = task,
        isSelected = isSelected,
        onClick = onClick,
        onLongPress = onLongPress,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = if (task.isImportant || task.isDone) 0.dp else 1.dp,
        containerColor = containerColor,
        contentColor = contentColor,
        baseBorder = baseBorder,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        TaskHeaderRow(task = task, onToggle = checkboxHandler)
        task.description?.takeIf { it.isNotBlank() }?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall)
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        items(days) { date ->
            val tasksForDay = weekTasks[date].orEmpty().sortedBy { it.start }
            val isToday = date == today()
            val isSelected = date == currentDate
            val hasTasks = tasksForDay.isNotEmpty()
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
            var isExpanded by rememberSaveable(date.toString()) { mutableStateOf(false) }
            LaunchedEffect(hasTasks) {
                if (!hasTasks) {
                    isExpanded = false
                }
            }
            val dayShape = RoundedCornerShape(16.dp)
            Surface(
                onClick = { onDayClick(date) },
                shape = dayShape,
                color = dayContainerColor,
                tonalElevation = if (isSelected || isToday) 4.dp else 1.dp,
                border = dayBorder,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(dayShape)
                    .animateContentSize(animationSpec = tween(durationMillis = 250))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val formatted = dateFormatter.format(date.toJavaLocalDate()).replaceFirstChar { char ->
                        if (char.isLowerCase()) char.titlecase(locale) else char.toString()
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatted,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (hasTasks) {
                            Text(
                                text = stringResource(id = R.string.tasks_count, tasksForDay.size),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            IconButton(
                                onClick = { isExpanded = !isExpanded },
                                modifier = Modifier.size(32.dp)
                            ) {
                                val (icon, description) = if (isExpanded) {
                                    Icons.Filled.ExpandLess to stringResource(id = R.string.collapse_day_tasks)
                                } else {
                                    Icons.Filled.ExpandMore to stringResource(id = R.string.expand_day_tasks)
                                }
                                Icon(imageVector = icon, contentDescription = description)
                            }
                        }
                    }
                    if (!hasTasks) {
                        Text(
                            text = stringResource(id = R.string.free_day_title),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(animationSpec = tween(durationMillis = 250)) + fadeIn(),
                            exit = shrinkVertically(animationSpec = tween(durationMillis = 250)) + fadeOut(),
                            modifier = Modifier.clipToBounds()
                        ) {
                            val timelineItems = remember(tasksForDay) {
                                buildTimeline(tasksForDay).filterIsInstance<DayTimelineItem.TaskBlock>()
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                timelineItems.forEach { item ->
                                    if (item.task.isInterval) {
                                        IntervalTaskCard(
                                            task = item.task,
                                            nestedPoints = item.nestedPoints,
                                            onToggle = null,
                                            showCheckbox = false,
                                            isSelected = false,
                                            selectedTaskIds = emptySet(),
                                            onClick = {},
                                            onLongPress = {},
                                            onNestedClick = {},
                                            onNestedLongPress = {}
                                        )
                                    } else {
                                        PointTaskCard(
                                            task = item.task,
                                            onToggle = null,
                                            showCheckbox = false,
                                            isSelected = false,
                                            onClick = {},
                                            onLongPress = {}
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
            val hasTasks = tasksForDay.isNotEmpty()
            val allTasksDone = hasTasks && tasksForDay.all { it.isDone }
            val dayShape = RoundedCornerShape(10.dp)
            Surface(
                onClick = { if (isCurrentMonth) onDayClick(date) },
                shape = dayShape,
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
                    .clip(dayShape)
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
                        visible = hasTasks,
                        modifier = Modifier.align(Alignment.TopEnd),
                        color = when {
                            allTasksDone -> TaskDoneGreen
                            hasImportant -> Color(0xFFD32F2F)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
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
    val sessionKey = listOf(
        initialTask?.id ?: "new",
        defaultDate.toString(),
        defaultStart?.toString() ?: "none",
        initialType.name
    ).joinToString(separator = "|")

    var title by rememberSaveable(sessionKey) { mutableStateOf(initialTask?.title.orEmpty()) }
    var description by rememberSaveable(sessionKey) { mutableStateOf(initialTask?.description.orEmpty()) }
    var type by rememberSaveable(sessionKey) { mutableStateOf(initialTask?.type ?: initialType) }
    var isImportant by rememberSaveable(sessionKey) { mutableStateOf(initialTask?.isImportant ?: false) }
    var saveError by rememberSaveable(sessionKey) { mutableStateOf<String?>(null) }
    var showDiscardDialog by rememberSaveable(sessionKey) { mutableStateOf(false) }

    var selectedDate by rememberSaveable(sessionKey, stateSaver = LocalDateSaver) {
        mutableStateOf(initialTask?.date ?: defaultDate)
    }

    val nowTime = remember {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
        LocalTime(now.hour, now.minute)
    }
    val initialStart = remember(sessionKey) {
        initialTask?.start ?: defaultStart ?: nowTime
    }
    val initialEndCandidate = remember(sessionKey, initialStart) {
        initialTask?.end ?: defaultEndFor(initialStart)
    }

    val initialSnapshot = remember(sessionKey, initialTask, defaultDate, initialStart, initialEndCandidate, initialType) {
        EditorSnapshot(
            title = initialTask?.title.orEmpty(),
            description = initialTask?.description.orEmpty(),
            date = initialTask?.date ?: defaultDate,
            type = initialTask?.type ?: initialType,
            isImportant = initialTask?.isImportant ?: false,
            start = initialStart,
            end = if ((initialTask?.type ?: initialType) == TaskType.INTERVAL) initialEndCandidate else null
        )
    }

    val currentRealTime by produceState(
        initialValue = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    ) {
        while (true) {
            value = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
            delay(30_000L)
        }
    }

    var startHour by rememberSaveable(sessionKey) { mutableIntStateOf(initialStart.hour) }
    var startMinute by rememberSaveable(sessionKey) { mutableIntStateOf(initialStart.minute) }
    var endHour by rememberSaveable(sessionKey) { mutableIntStateOf(initialEndCandidate.hour) }
    var endMinute by rememberSaveable(sessionKey) { mutableIntStateOf(initialEndCandidate.minute) }

    val startTime = remember(startHour, startMinute) { LocalTime(startHour, startMinute) }
    val endTime = remember(endHour, endMinute, type) {
        if (type == TaskType.INTERVAL) LocalTime(endHour, endMinute) else null
    }

    val hasChanges by remember(
        title,
        description,
        selectedDate,
        type,
        isImportant,
        startTime,
        endTime,
        initialSnapshot
    ) {
        derivedStateOf {
            title != initialSnapshot.title ||
                description != initialSnapshot.description ||
                selectedDate != initialSnapshot.date ||
                type != initialSnapshot.type ||
                isImportant != initialSnapshot.isImportant ||
                startTime != initialSnapshot.start ||
                endTime != initialSnapshot.end
        }
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

    LaunchedEffect(selectedDate, type, endHour, endMinute, startTime, currentRealTime, initialTask) {
        if (initialTask == null && type == TaskType.INTERVAL && selectedDate == today()) {
            val nowLimit = currentRealTime
            val currentEnd = LocalTime(endHour, endMinute)
            if (currentEnd <= nowLimit) {
                val base = if (startTime > nowLimit) startTime else nowLimit
                val adjusted = defaultEndFor(base)
                endHour = adjusted.hour
                endMinute = adjusted.minute
            }
        }
    }

    var scheduleError by remember(sessionKey) { mutableStateOf<String?>(null) }
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

    var isDatePickerVisible by rememberSaveable(sessionKey) { mutableStateOf(false) }
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

    fun attemptClose() {
        if (hasChanges) {
            showDiscardDialog = true
        } else {
            onDismiss()
        }
    }

    BackHandler { attemptClose() }

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
                        IconButton(onClick = { attemptClose() }) {
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        if (saveError != null && saveError != scheduleError) saveError = null
                    },
                    label = { Text(text = stringResource(id = R.string.field_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
                val dateShape = RoundedCornerShape(12.dp)
                Surface(
                    onClick = { isDatePickerVisible = true },
                    shape = dateShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(dateShape)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.field_date),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.CalendarToday, contentDescription = null)
                                Text(
                                    text = dateFormatter.format(selectedDate.toJavaLocalDate()),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
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

    if (showDiscardDialog) {
        if (!hasChanges) {
            showDiscardDialog = false
        } else {
            AlertDialog(
                onDismissRequest = { showDiscardDialog = false },
                title = { Text(text = stringResource(id = R.string.unsaved_changes_title)) },
                text = { Text(text = stringResource(id = R.string.unsaved_changes_message)) },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.End)
                    ) {
                        TextButton(onClick = { showDiscardDialog = false }) {
                            Text(text = stringResource(android.R.string.cancel))
                        }
                        TextButton(onClick = {
                            showDiscardDialog = false
                            onDismiss()
                        }) {
                            Text(text = stringResource(id = R.string.action_discard))
                        }
                        TextButton(
                            onClick = {
                                showDiscardDialog = false
                                handleSave()
                            },
                            enabled = scheduleError == null && title.isNotBlank()
                        ) {
                            Text(text = stringResource(id = R.string.save))
                        }
                    }
                },
                modifier = Modifier.widthIn(min = 360.dp)
            )
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
    val shape = RoundedCornerShape(12.dp)
    Surface(
        onClick = { onChanged(!isImportant) },
        shape = shape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
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
    this.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

private fun Long.toLocalDate(): LocalDate =
    Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.UTC)
        .date
