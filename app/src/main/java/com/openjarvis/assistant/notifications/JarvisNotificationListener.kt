package com.openjarvis.assistant.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/** Notification metadata only: message text and notification content are never persisted. */
class JarvisNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        if (sbn.packageName == packageName) return
        val prefs = getSharedPreferences("jarvis_notification_stats", MODE_PRIVATE)
        prefs.edit().putInt("count", (prefs.getInt("count", 0) + 1).coerceAtMost(100000))
            .putString("last_app", sbn.packageName.take(160)).putLong("last_time", sbn.postTime).apply()
    }
}
