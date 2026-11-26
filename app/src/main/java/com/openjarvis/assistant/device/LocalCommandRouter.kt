package com.openjarvis.assistant.device

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.Settings
import java.text.DateFormat
import java.util.Date

/**
 * Offline, privacy-preserving commands. This router only launches user-facing
 * activities; it never silently sends messages, calls, purchases, or changes
 * protected settings.
 */
class LocalCommandRouter(private val context: Context) {
    fun handle(raw: String): String? {
        val command = raw.trim().lowercase()
        if (command.isEmpty()) return null

        if (command in setOf("help", "what can you do", "jarvis help", "madad", "jarvis kya kar sakta hai")) {
            return "Offline: time/waqt, date/tareekh, battery, phone info, settings, Wi-Fi/Bluetooth settings, camera, calendar/event form, browser, Maps search, YouTube, WhatsApp, Gmail. Sensitive actions always require your confirmation."
        }
        if (command.contains("battery") || command.contains("charge") || command.contains("charging")) {
            val battery = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val percent = battery.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val status = battery.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            return "Battery: $percent%. ${if (charging) "Phone charging hai." else "Phone charging par nahi hai."}"
        }
        if (command.contains("time") || command.contains("waqt") || command.contains("kitne baj")) {
            return "Abhi ${DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())} bajay hain."
        }
        if (command.contains("date") || command.contains("tareekh") || command.contains("today") || command.contains("aaj ki date")) {
            return "Aaj ${DateFormat.getDateInstance(DateFormat.FULL).format(Date())} hai."
        }
        if (command.contains("phone info") || command.contains("device info") || command.contains("mobile info")) {
            return "Device: ${Build.MANUFACTURER} ${Build.MODEL}. Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})."
        }

        // Specific system panels must be checked before the generic Settings phrase.
        if (command.contains("wifi settings") || command.contains("wi-fi settings") || command.contains("wifi kholo") || command.contains("wifi setting")) {
            return launch(Intent(Settings.ACTION_WIFI_SETTINGS), "Wi-Fi settings open kar raha hoon.")
        }
        if (command.contains("bluetooth settings") || command.contains("bluetooth kholo") || command.contains("bluetooth setting")) {
            return launch(Intent(Settings.ACTION_BLUETOOTH_SETTINGS), "Bluetooth settings open kar raha hoon.")
        }
        if (command.contains("open camera") || command.contains("camera kholo")) {
            return launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE), "Camera open kar raha hoon.")
        }
        if (command.contains("open calendar") || command.contains("calendar kholo")) {
            return launch(Intent(Intent.ACTION_VIEW, CalendarContract.CONTENT_URI), "Calendar open kar raha hoon.")
        }
        if (command.contains("add calendar event") || command.contains("calendar event banao") || command.contains("reminder calendar mein")) {
            val event = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, "Jarvis reminder")
            return launch(event, "Calendar form open hai. Date/time verify karke Save aap khud karen.")
        }
        if (command.contains("open maps") || command.contains("maps kholo") || command.startsWith("search maps ") || command.startsWith("maps par ")) {
            val query = command.removePrefix("search maps ").removePrefix("maps par ").trim()
            val uri = if (query.isBlank()) Uri.parse("geo:0,0") else Uri.parse("geo:0,0?q=" + Uri.encode(query))
            return launch(Intent(Intent.ACTION_VIEW, uri), "Maps khol raha hoon.")
        }
        if (command.contains("open browser") || command.contains("browser kholo")) {
            return launch(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")), "Browser khol raha hoon.")
        }

        val app = when {
            command in setOf("open youtube", "youtube kholo", "youtube khol do") -> Triple("com.google.android.youtube", "https://www.youtube.com", "YouTube")
            command in setOf("open whatsapp", "whatsapp kholo", "whatsapp khol do") -> Triple("com.whatsapp", "https://wa.me/", "WhatsApp")
            command in setOf("open gmail", "gmail kholo", "gmail khol do") -> Triple("com.google.android.gm", "https://mail.google.com", "Gmail")
            command in setOf("open chrome", "chrome kholo") -> Triple("com.android.chrome", "https://www.google.com", "Chrome")
            else -> null
        }
        if (app != null) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.first)
            return if (launchIntent != null) launch(launchIntent, app.third + " open kar raha hoon.")
            else launch(Intent(Intent.ACTION_VIEW, Uri.parse(app.second)), app.third + " ka link open kar raha hoon.")
        }

        if (command.contains("open settings") || command.contains("settings kholo") || command == "settings" || command == "phone settings") {
            return launch(Intent(Settings.ACTION_SETTINGS), "Settings open kar raha hoon.")
        }
        if (command.startsWith("call ") || command.startsWith("message ") || command.startsWith("send ")) {
            return "Safety ke liye calling ya message automatic nahi bheja. Contact choose karne aur confirmation ka dedicated flow zaroori hai."
        }
        return null
    }

    private fun launch(intent: Intent, success: String): String {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            if (intent.resolveActivity(context.packageManager) == null) {
                "Is action ke liye phone par compatible app nahi mili."
            } else {
                context.startActivity(intent)
                success
            }
        } catch (error: Exception) {
            "App open nahi ho saki: ${error.localizedMessage ?: "unknown error"}"
        }
    }
}
