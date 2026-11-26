package com.openjarvis.assistant

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/** Schedules a one-shot task reminder. Uses inexact alarms to avoid special exact-alarm access. */
object TaskReminderScheduler {
    fun schedule(context: Context, taskId: Long, title: String, atMillis: Long) {
        require(atMillis > System.currentTimeMillis()) { "Choose a future reminder time." }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pendingIntent(context, taskId, title))
    }

    fun cancel(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = pendingIntent(context, taskId, "")
        alarmManager.cancel(intent)
        intent.cancel()
    }

    private fun pendingIntent(context: Context, taskId: Long, title: String): PendingIntent {
        val intent = Intent(context, TaskReminderReceiver::class.java)
            .putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
            .putExtra(TaskReminderReceiver.EXTRA_TITLE, title)
        return PendingIntent.getBroadcast(
            context, taskId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
