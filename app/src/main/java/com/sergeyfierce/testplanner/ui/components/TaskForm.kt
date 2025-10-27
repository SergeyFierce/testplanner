package com.sergeyfierce.testplanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sergeyfierce.testplanner.lib.types.Task
import com.sergeyfierce.testplanner.lib.types.TaskPriority
import com.sergeyfierce.testplanner.lib.types.TaskRecurrence
import com.sergeyfierce.testplanner.ui.TaskFormInput

@Composable
fun TaskForm(
    selectedDate: String,
    settingsDefaultReminder: Int,
    editingTask: Task?,
    onDismiss: () -> Unit,
    onSave: (TaskFormInput) -> Unit
) {
    var title by remember { mutableStateOf(editingTask?.title ?: "") }
    var description by remember { mutableStateOf(editingTask?.description ?: "") }
    var date by remember { mutableStateOf(editingTask?.date ?: selectedDate) }
    var time by remember { mutableStateOf(editingTask?.time ?: "") }
    var duration by remember { mutableStateOf(editingTask?.duration?.toString() ?: "60") }
    var priority by remember { mutableStateOf(editingTask?.priority ?: TaskPriority.MEDIUM) }
    var recurrence by remember { mutableStateOf(editingTask?.recurrence ?: TaskRecurrence.NONE) }
    var category by remember { mutableStateOf(editingTask?.category ?: "") }
    var reminder by remember { mutableStateOf((editingTask?.reminderMinutes ?: settingsDefaultReminder).toString()) }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (editingTask == null) "Новая задача" else "Редактирование") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название задачи *") },
                    leadingIcon = { Text("📝") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    leadingIcon = { Text("🗒️") },
                    minLines = 3
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Дата (YYYY-MM-DD)") },
                    leadingIcon = { Text("📅") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Время (HH:mm)") },
                    leadingIcon = { Text("🕐") },
                    singleLine = true
                )
                if (time.isNotBlank()) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { input -> duration = input.filter { it.isDigit() } },
                        label = { Text("Длительность (мин)") },
                        supportingText = { Text("Используется для расчета свободного времени") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                PrioritySelector(priority) { priority = it }
                RecurrenceSelector(recurrence) { recurrence = it }
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Категория") },
                    leadingIcon = { Text("🏷️") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = reminder,
                    onValueChange = { reminder = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Напоминание (минуты)") },
                    leadingIcon = { Text("🔔") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isBlank()) return@Button
                val input = TaskFormInput(
                    id = editingTask?.id,
                    title = title,
                    description = description,
                    date = date,
                    time = time.ifBlank { null },
                    duration = duration.toIntOrNull(),
                    priority = priority,
                    recurrence = recurrence,
                    category = category,
                    reminderMinutes = reminder.toIntOrNull()
                )
                onSave(input)
            }) {
                Text(text = if (editingTask == null) "Создать" else "Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun PrioritySelector(priority: TaskPriority, onPriorityChange: (TaskPriority) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Приоритет", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskPriority.entries.forEach { level ->
                val selected = level == priority
                Button(onClick = { onPriorityChange(level) }, enabled = !selected) {
                    val label = when (level) {
                        TaskPriority.LOW -> "🟢 Низкий"
                        TaskPriority.MEDIUM -> "🟡 Средний"
                        TaskPriority.HIGH -> "🔴 Высокий"
                    }
                    Text(label)
                }
            }
        }
    }
}

@Composable
private fun RecurrenceSelector(recurrence: TaskRecurrence, onRecurrenceChange: (TaskRecurrence) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Повторение", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskRecurrence.entries.forEach { option ->
                val label = when (option) {
                    TaskRecurrence.NONE -> "Без повтора"
                    TaskRecurrence.DAILY -> "Ежедневно"
                    TaskRecurrence.WEEKLY -> "Еженедельно"
                    TaskRecurrence.MONTHLY -> "Ежемесячно"
                }
                Button(onClick = { onRecurrenceChange(option) }, enabled = option != recurrence) {
                    Text(label)
                }
            }
        }
    }
}

