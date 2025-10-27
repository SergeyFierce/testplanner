package com.sergeyfierce.testplanner.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sergeyfierce.testplanner.lib.types.TaskPriority
import com.sergeyfierce.testplanner.ui.MonthDaySummary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonthView(
    monthSummaries: List<MonthDaySummary>,
    onSelectDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(monthSummaries, key = { it.date }) { summary ->
            MonthDayCell(summary = summary, onSelect = { onSelectDate(summary.date) })
        }
    }
}

@Composable
private fun MonthDayCell(summary: MonthDaySummary, onSelect: () -> Unit) {
    val background = when {
        summary.isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        summary.isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        !summary.isCurrentMonth -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = if (summary.isSelected) 6.dp else 1.dp,
        color = background
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = summary.dayNumber.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = if (summary.isCurrentMonth) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (summary.totalTasks > 0) {
                Text(
                    text = summary.totalTasks.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            PriorityIndicatorRow(summary)
        }
    }
}

@Composable
private fun PriorityIndicatorRow(summary: MonthDaySummary) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        TaskPriority.entries.forEach { priority ->
            val color = when (priority) {
                TaskPriority.HIGH -> Color(0xFFef4444)
                TaskPriority.MEDIUM -> Color(0xFFeab308)
                TaskPriority.LOW -> Color(0xFF22c55e)
            }
            val hasTasks = (summary.priorityIndicators[priority] ?: 0) > 0
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color = if (hasTasks) color else color.copy(alpha = 0.2f), shape = CircleShape)
            )
        }
    }
}

