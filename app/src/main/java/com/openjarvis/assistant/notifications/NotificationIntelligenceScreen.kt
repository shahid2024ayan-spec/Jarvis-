package com.openjarvis.assistant.notifications

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.provider.Settings.Secure
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationIntelligenceScreen(context: Context, onBack: () -> Unit) {
    val prefs = remember { context.getSharedPreferences("jarvis_notification_stats", Context.MODE_PRIVATE) }
    var refresh by remember { mutableStateOf(0) }
    val enabled = remember(refresh) {
        Secure.getString(context.contentResolver, "enabled_notification_listeners").orEmpty()
            .split(":").any { it.contains(context.packageName) }
    }
    Surface(Modifier.fillMaxSize(), color = Color(0xFF080D18)) {
        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("NOTIFICATION INTELLIGENCE", color = Color(0xFF62D9FF), fontWeight = FontWeight.Bold)
            Text("Permission-controlled insight", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF111A2B))) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (enabled) "Access enabled" else "Access not enabled", color = if (enabled) Color(0xFF8EDDB5) else Color(0xFFFFC66D), fontWeight = FontWeight.Bold)
                    Text("Jarvis stores notification count, app package and timestamp only. It does not save notification titles, text or message content.", color = Color(0xFFB8C7D9))
                    Text("Captured: " + prefs.getInt("count", 0), color = Color.White)
                    Text("Last app: " + (prefs.getString("last_app", "—") ?: "—"), color = Color.White)
                    val time = prefs.getLong("last_time", 0L)
                    Text("Last received: " + if (time > 0) java.text.DateFormat.getDateTimeInstance().format(java.util.Date(time)) else "—", color = Color.White)
                }
            }
            Button(onClick = { runCatching { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }, modifier = Modifier.fillMaxWidth()) { Text("Open notification access settings") }
            Button(onClick = { refresh++ }, modifier = Modifier.fillMaxWidth()) { Text("Refresh status") }
            Button(onClick = { prefs.edit().putInt("count", 0).remove("last_app").remove("last_time").apply(); refresh++ }, modifier = Modifier.fillMaxWidth()) { Text("Clear local stats") }
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Jarvis") }
            Text("Android asks you to enable access manually. Revoke it anytime in system Settings.", color = Color(0xFF8EA6C2), fontSize = 12.sp)
        }
    }
}
