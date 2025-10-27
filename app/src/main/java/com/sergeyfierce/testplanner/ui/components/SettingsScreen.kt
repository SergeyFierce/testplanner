package com.sergeyfierce.testplanner.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sergeyfierce.testplanner.lib.types.AccentColor
import com.sergeyfierce.testplanner.lib.types.Settings
import com.sergeyfierce.testplanner.lib.types.ThemeMode

@Composable
fun SettingsScreen(
    settings: Settings,
    onSettingsChanged: (Settings) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onClearAll: () -> Unit,
    onRequestNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localSettings by remember(settings) { mutableStateOf(settings) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsCard(title = "Уведомления") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Включить уведомления", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Напоминания перед началом задач",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = localSettings.notificationsEnabled,
                    onCheckedChange = {
                        localSettings = localSettings.copy(notificationsEnabled = it)
                        onSettingsChanged(localSettings)
                        if (it) onRequestNotifications()
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = localSettings.defaultReminderMinutes.toString(),
                onValueChange = { value ->
                    val minutes = value.toIntOrNull() ?: localSettings.defaultReminderMinutes
                    localSettings = localSettings.copy(defaultReminderMinutes = minutes)
                    onSettingsChanged(localSettings)
                },
                label = { Text("Напоминание по умолчанию (мин)") },
                singleLine = true
            )
        }

        SettingsCard(title = "Внешний вид") {
            ThemeSelector(localSettings.theme) { mode ->
                localSettings = localSettings.copy(theme = mode)
                onSettingsChanged(localSettings)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Акцентный цвет", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AccentColor.values().forEach { color ->
                    ColorPreview(
                        color = Color(android.graphics.Color.parseColor(color.hex)),
                        selected = localSettings.accentColor == color.hex,
                        onSelect = {
                            localSettings = localSettings.copy(accentColor = color.hex)
                            onSettingsChanged(localSettings)
                        }
                    )
                }
            }
        }

        SettingsCard(title = "Календарь и время") {
            WeekStartSelector(localSettings.weekStartsOn) { weekStart ->
                localSettings = localSettings.copy(weekStartsOn = weekStart)
                onSettingsChanged(localSettings)
            }
            Spacer(modifier = Modifier.height(12.dp))
            TimePickerField(
                label = "Начало рабочего дня",
                value = localSettings.workingHoursStart,
                leadingIcon = "🕘",
                fallbackHour = 9,
                fallbackMinute = 0
            ) { selected ->
                localSettings = localSettings.copy(workingHoursStart = selected)
                onSettingsChanged(localSettings)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TimePickerField(
                label = "Конец рабочего дня",
                value = localSettings.workingHoursEnd,
                leadingIcon = "🕕",
                fallbackHour = 18,
                fallbackMinute = 0
            ) { selected ->
                localSettings = localSettings.copy(workingHoursEnd = selected)
                onSettingsChanged(localSettings)
            }
        }

        SettingsCard(title = "Управление данными") {
            Button(onClick = onExport, modifier = Modifier.fillMaxWidth()) {
                Text("Экспортировать данные")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onImport, modifier = Modifier.fillMaxWidth()) {
                Text("Импортировать данные")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onClearAll,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFef4444)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Удалить все данные", color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Все данные хранятся локально на устройстве",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ThemeSelector(current: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Тема", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.values().forEach { mode ->
                val selected = mode == current
                Box(
                    modifier = Modifier
                        .background(
                            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { onSelect(mode) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = mode.name.lowercase().replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}

@Composable
private fun ColorPreview(color: Color, selected: Boolean, onSelect: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color, shape = CircleShape)
            .border(BorderStroke(width = if (selected) 3.dp else 1.dp, color = if (selected) Color.White else Color.White.copy(alpha = 0.3f)), CircleShape)
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(Color.White, CircleShape)
            )
        }
    }
}

@Composable
private fun WeekStartSelector(current: Int, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Начало недели", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1 to "Понедельник", 0 to "Воскресенье").forEach { (value, label) ->
                val selected = value == current
                Box(
                    modifier = Modifier
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { onSelect(value) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(text = label)
                }
            }
        }
    }
}

@Composable
private fun TimePickerField(
    label: String,
    value: String,
    leadingIcon: String,
    fallbackHour: Int,
    fallbackMinute: Int,
    onTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val (hour, minute) = remember(value) { parseTime(value, fallbackHour, fallbackMinute) }
    OutlinedTextField(
        value = value.ifBlank { formatTime(hour, minute) },
        onValueChange = {},
        label = { Text(label) },
        leadingIcon = { Text(leadingIcon) },
        trailingIcon = { androidx.compose.material3.Icon(Icons.Default.Schedule, contentDescription = null) },
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                TimePickerDialog(context, { _, selectedHour, selectedMinute ->
                    onTimeSelected(formatTime(selectedHour, selectedMinute))
                }, hour, minute, true).show()
            }
    )
}

private fun parseTime(value: String, fallbackHour: Int, fallbackMinute: Int): Pair<Int, Int> {
    val parts = value.split(":")
    val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: fallbackHour
    val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: fallbackMinute
    return hour to minute
}

private fun formatTime(hour: Int, minute: Int): String = String.format("%02d:%02d", hour, minute)

