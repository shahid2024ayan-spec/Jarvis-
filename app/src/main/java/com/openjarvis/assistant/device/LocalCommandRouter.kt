package com.openjarvis.assistant.device

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.provider.MediaStore
import android.provider.CalendarContract
import android.provider.Settings
import java.text.DateFormat
import java.util.Date

/**
 * Small privacy-preserving offline command set. Only opens user-facing activities;
 * it does not silently call, message, delete, purchase, or change security settings.
 */
class LocalCommandRouter(private val context: Context) {
    fun handle(raw: String): String? {
        val command = raw.trim().lowercase()
        if (command.isEmpty()) return null

        if (command in setOf("help", "what can you do", "jarvis help", "madad")) {
            return "Offline commands: time, date, battery, phone info, open settings, open camera, open browser. Roman Urdu: waqt, tareekh, battery, phone info, settings kholo."
        }
        if (command.contains("battery") || command.contains("charge") || command.contains("charging")) {
            val battery = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val percent = battery.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val status = battery.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
            return "Battery: $percent%. ${if (charging) "Phone charging hai." else "Phone charging par nahi hai."}"
        }
        if (command.contains("time") || command.contains("waqt") || command.contains("kitne baj")) {
            return "Abhi ${DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())} bajay hain."
        }
        if (command.contains("date") || command.contains("tareekh") || command.contains("today")) {
            return "Aaj ${DateFormat.getDateInstance(DateFormat.FULL).format(Date())} hai."
        }
        if (command.contains("phone info") || command.contains("device info") || command.contains("mobile info")) {
            return "Device: ${Build.MANUFACTURER} ${Build.MODEL}. Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})."
        }
        if (command.contains("open settings") || command.contains("settings kholo") || command == "settings") {
            return launch(Intent(Settings.ACTION_SETTINGS), "Settings open kar raha hoon.")
        }
        if (command.contains("open camera") || command.contains("camera kholo")) {
            return launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE), "Camera open kar raha hoon.")
        }
        if (command.contains("open calendar") || command.contains("calendar kholo")) return launch(Intent(Intent.ACTION_VIEW, CalendarContract.CONTENT_URI), "Calendar open kar raha hoon.")
        if (command.contains("add calendar event") || command.contains("calendar event banao")) {
            val event = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI).putExtra(CalendarContract.Events.TITLE, "Jarvis reminder")
            return launch(event, "Calendar form open hai. Date/time check karke Save aap khud karen.")
        }
        if (command.contains("open browser") || command.contains("browser kholo")) {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com"))
            return launch(intent, "Browser khol raha hoon.")
        }
        if (command.startsWith("call ") || command.startsWith("message ") || command.startsWith("send ")) {
            return "Safety ke liye calling ya message bhejna abhi automatic nahi hai. Is action ke liye contact selection aur explicit confirmation module chahiye."
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
