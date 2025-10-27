package com.sergeyfierce.testplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sergeyfierce.testplanner.R
import com.sergeyfierce.testplanner.lib.types.DayAgendaItem
import com.sergeyfierce.testplanner.lib.types.Task
import com.sergeyfierce.testplanner.lib.types.toReadableDuration
import com.sergeyfierce.testplanner.ui.DayUiState

@Composable
fun DayView(
    dayUiState: DayUiState,
    onTaskClick: (Task) -> Unit,
    onToggleComplete: (Task, Boolean) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (dayUiState.totalTasks == 0 && dayUiState.tasksWithoutTime.isEmpty()) {
        EmptyDayState(onAddTask = onAddTask, modifier = modifier.fillMaxSize())
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        ProgressHeader(dayUiState)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dayUiState.agenda, key = { it.key }) { item ->
                when (item) {
                    is DayAgendaItem.TaskEntry -> TaskItem(
                        task = item.task,
                        onToggleComplete = { completed -> onToggleComplete(item.task, completed) },
                        onClick = { onTaskClick(item.task) },
                        onDelete = { onDeleteTask(item.task) }
                    )

                    is DayAgendaItem.FreeSlot -> FreeSlotCard(item)
                }
            }

            if (dayUiState.tasksWithoutTime.isNotEmpty()) {
                item("header_without_time") {
                    Text(
                        text = "Задачи без времени",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(dayUiState.tasksWithoutTime, key = { it.id }) { task ->
                    TaskItem(
                        task = task,
                        onToggleComplete = { completed -> onToggleComplete(task, completed) },
                        onClick = { onTaskClick(task) },
                        onDelete = { onDeleteTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressHeader(dayUiState: DayUiState) {
    val progress = dayUiState.completionRate
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Прогресс дня", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.material3.LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${dayUiState.completedTasks} из ${dayUiState.totalTasks} задач выполнено",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FreeSlotCard(slot: DayAgendaItem.FreeSlot) {
    Card(
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFd1fae5), Color(0xFF86efac))
                    )
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconWithCircle()
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Свободное время", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${slot.startTime} - ${slot.endTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = slot.durationMinutes.toReadableDuration(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun IconWithCircle() {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.4f), shape = MaterialTheme.shapes.large)
            .padding(8.dp)
    ) {
        androidx.compose.material3.Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EmptyDayState(onAddTask: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "📅", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Нет задач на этот день",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Добавьте первую задачу, чтобы спланировать день",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddTask) {
            Text("Добавить задачу")
        }
    }
}

