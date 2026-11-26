package com.openjarvis.assistant.device

import android.content.Context
import android.content.Intent

/** Launches an installed app by its visible launcher label; never performs actions inside it. */
class InstalledAppLauncher(private val context: Context) {
    fun launchByName(rawName: String): String? {
        val query = rawName.trim().lowercase().take(80)
        if (query.isBlank()) return null
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val matches = context.packageManager.queryIntentActivities(launcherIntent, 0)
            .mapNotNull { info ->
                val label = info.loadLabel(context.packageManager).toString()
                val normalized = label.lowercase()
                if (normalized == query || normalized.contains(query) || query.contains(normalized)) {
                    Triple(label, info.activityInfo.packageName, info.activityInfo.name)
                } else null
            }
            .distinctBy { it.second }
            .sortedBy { it.first.length }
        val selected = matches.firstOrNull() ?: return "Installed apps mein '$rawName' nahi mili. App ka naam check karen."
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(selected.second)
                ?: Intent().setClassName(selected.second, selected.third)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            "${selected.first} open kar raha hoon."
        } catch (error: Exception) {
            "App launch nahi hui: ${error.localizedMessage ?: "unknown error"}"
        }
    }
}
