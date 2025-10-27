package com.sergeyfierce.testplanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sergeyfierce.testplanner.lib.types.CategoryDistribution
import com.sergeyfierce.testplanner.lib.types.PlannerStatistics
import com.sergeyfierce.testplanner.lib.types.PriorityDistribution
import com.sergeyfierce.testplanner.lib.types.WeekdayProductivity
import kotlin.math.abs

@Composable
fun StatisticsScreen(statistics: PlannerStatistics, modifier: Modifier = Modifier) {
    if (statistics.totalTasks == 0) {
        Column(
            modifier = modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Нет статистики", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Начните выполнять задачи, чтобы увидеть аналитику",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 80.dp)
    ) {
        item { SummaryCards(statistics) }
        item { ProductivitySection(statistics.productivityByWeekday) }
        item { PrioritySection(statistics.priorityDistribution) }
        if (statistics.categoryDistribution.isNotEmpty()) {
            item { CategorySection(statistics.categoryDistribution) }
        }
        if (statistics.completionHistory.isNotEmpty()) {
            item { HistorySection(statistics.completionHistory) }
        }
    }
}

@Composable
private fun SummaryCards(statistics: PlannerStatistics) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(title = "Всего", value = statistics.totalTasks.toString(), accent = MaterialTheme.colorScheme.primary)
            StatCard(title = "Выполнено", value = statistics.completedTasks.toString(), accent = Color(0xFF22c55e))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = "Процент",
                value = "${(statistics.completionRate * 100).toInt()}%",
                accent = Color(0xFF8b5cf6)
            )
            StatCard(title = "Серия дней", value = statistics.streakDays.toString(), accent = Color(0xFFf59e0b))
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, accent: Color) {
    Card(colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.1f)), modifier = Modifier.weight(1f)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = accent)
            Text(text = value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun ProductivitySection(productivity: List<WeekdayProductivity>) {
    Card(shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Продуктивность по дням недели", style = MaterialTheme.typography.titleMedium)
            val max = productivity.maxOf { it.completed }.coerceAtLeast(1)
            productivity.forEach { day ->
                Column {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(text = day.label, style = MaterialTheme.typography.bodyMedium)
                        Text(text = day.completed.toString(), style = MaterialTheme.typography.bodyMedium)
                    }
                    LinearProgressIndicator(
                        progress = day.completed.toFloat() / max,
                        modifier = Modifier.fillMaxWidth(),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PrioritySection(priorityDistribution: List<PriorityDistribution>) {
    Card(shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Распределение по приоритетам", style = MaterialTheme.typography.titleMedium)
            priorityDistribution.forEach { entry ->
                val color = when (entry.priority) {
                    com.sergeyfierce.testplanner.lib.types.TaskPriority.HIGH -> Color(0xFFef4444)
                    com.sergeyfierce.testplanner.lib.types.TaskPriority.MEDIUM -> Color(0xFFeab308)
                    com.sergeyfierce.testplanner.lib.types.TaskPriority.LOW -> Color(0xFF22c55e)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(text = entry.priority.name.lowercase().replaceFirstChar { it.uppercase() }, color = color)
                        Text(text = "${entry.total} (${(entry.percentage * 100).toInt()}%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    LinearProgressIndicator(
                        progress = entry.percentage,
                        modifier = Modifier.fillMaxWidth(),
                        color = color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySection(categories: List<CategoryDistribution>) {
    Card(shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Категории задач", style = MaterialTheme.typography.titleMedium)
            categories.forEach { category ->
                val color = rememberCategoryColor(category.category)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(text = category.category, color = color)
                        Text(text = "${category.total} (${(category.percentage * 100).toInt()}%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    LinearProgressIndicator(
                        progress = category.percentage,
                        modifier = Modifier.fillMaxWidth(),
                        color = color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HistorySection(history: List<com.sergeyfierce.testplanner.lib.types.CompletionHistoryEntry>) {
    Card(shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "История выполнений", style = MaterialTheme.typography.titleMedium)
            history.forEach { entry ->
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = entry.date)
                    Text(text = entry.completed.toString(), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun rememberCategoryColor(category: String): Color {
    val palette = listOf(
        Color(0xFF3b82f6),
        Color(0xFF8b5cf6),
        Color(0xFFec4899),
        Color(0xFFf59e0b),
        Color(0xFF10b981),
        Color(0xFF06b6d4)
    )
    val index = abs(category.hashCode()) % palette.size
    return palette[index]
}

