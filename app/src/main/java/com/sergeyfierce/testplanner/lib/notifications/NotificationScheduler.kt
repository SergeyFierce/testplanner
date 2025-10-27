package com.sergeyfierce.testplanner.lib.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sergeyfierce.testplanner.lib.types.Task
import com.sergeyfierce.testplanner.lib.types.combineDateTime
import com.sergeyfierce.testplanner.lib.types.toLocalDateOrNull
import com.sergeyfierce.testplanner.lib.types.toLocalTimeOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class NotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Напоминания о задачах",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления перед началом задач"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun scheduleReminder(task: Task) {
        ensureChannel()
        val reminderMinutes = task.reminderMinutes ?: return
        val date = task.date.toLocalDateOrNull() ?: return
        val time = task.time?.toLocalTimeOrNull() ?: return

        val triggerDateTime = combineDateTime(date, time).minus(DateTimePeriod(minutes = reminderMinutes))
        val triggerMillis = triggerDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        if (triggerMillis <= Clock.System.now().toEpochMilliseconds()) return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TASK_ID, task.id)
            putExtra(ReminderReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(ReminderReceiver.EXTRA_REMINDER_MINUTES, reminderMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            pendingIntent
        )
    }

    fun cancelReminder(taskId: String) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    companion object {
        const val CHANNEL_ID = "planner_notifications"
    }
}

