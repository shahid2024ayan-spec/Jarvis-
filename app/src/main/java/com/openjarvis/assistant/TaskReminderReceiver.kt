package com.openjarvis.assistant

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L)
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty().ifBlank { "Your Jarvis task" }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Task reminders", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val openApp = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Jarvis reminder")
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle().bigText(title))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        manager.notify(taskId.toInt(), notification)
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TITLE = "task_title"
        private const val CHANNEL_ID = "jarvis_task_reminders"
    }
}
