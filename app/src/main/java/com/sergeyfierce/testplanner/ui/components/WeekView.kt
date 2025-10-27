package com.sergeyfierce.testplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import com.sergeyfierce.testplanner.lib.types.TaskPriority
import com.sergeyfierce.testplanner.ui.WeekDaySummary

@Composable
fun WeekView(
    weekSummaries: List<WeekDaySummary>,
    onDaySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(weekSummaries, key = { it.date }) { summary ->
            WeekDayCard(summary = summary, onClick = { onDaySelected(summary.date) })
        }
    }
}

@Composable
private fun WeekDayCard(summary: WeekDaySummary, onClick: () -> Unit) {
    val borderColor = if (summary.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Card(
        modifier = Modifier
            .size(width = 140.dp, height = 200.dp)
            .graphicsLayer { alpha = if (summary.isSelected) 1f else 0.95f }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (summary.isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(width = 2.dp, color = borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = summary.dayLabel, style = MaterialTheme.typography.titleMedium)
            Text(
                text = summary.dateLabel,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${summary.totalTasks} задач",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            PriorityDots(summary)
            LinearProgressIndicator(
                progress = if (summary.totalTasks == 0) 0f else summary.completedTasks.toFloat() / summary.totalTasks,
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = "${summary.completedTasks} выполнено",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PriorityDots(summary: WeekDaySummary) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        TaskPriority.entries.forEach { priority ->
            val color = when (priority) {
                TaskPriority.HIGH -> Color(0xFFef4444)
                TaskPriority.MEDIUM -> Color(0xFFeab308)
                TaskPriority.LOW -> Color(0xFF22c55e)
            }
            val count = summary.priorityIndicators[priority] ?: 0
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(color.copy(alpha = if (count > 0) 1f else 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (count > 0) {
                    Text(text = count.toString(), style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
        }
    }
}

