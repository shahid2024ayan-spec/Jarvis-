package com.openjarvis.assistant

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
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
fun TaskScreen(context: Context, onBack: () -> Unit) {
    val store = remember { TaskStore(context) }
    var tasks by remember { mutableStateOf(store.load()) }
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Tasks are stored locally on this device.") }
    fun save(next: List<JarvisTask>) {
        runCatching { store.save(next); tasks = next; status = "Saved" }
            .onFailure { status = "Could not save: " + it.message }
    }
    Surface(Modifier.fillMaxSize(), color = Color(0xFF080D18)) {
        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("JARVIS TASKS", color = Color(0xFF62D9FF), fontWeight = FontWeight.Bold)
            Text("Personal task manager", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Text(status, color = Color(0xFF9EACC0), fontSize = 12.sp)
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Task title") }, singleLine = true)
            OutlinedTextField(detail, { detail = it }, Modifier.fillMaxWidth(), label = { Text("Details (optional)") }, maxLines = 2)
            Button(onClick = {
                val clean = title.trim()
                if (clean.isNotEmpty()) {
                    save((tasks + JarvisTask(System.currentTimeMillis(), clean, detail.trim(), false)).takeLast(300))
                    title = ""; detail = ""
                }
            }, enabled = title.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Add task") }
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tasks, key = { it.id }) { task ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF111A2B))) {
                        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text((if (task.done) "✓ " else "") + task.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                            if (task.detail.isNotBlank()) Text(task.detail, color = Color(0xFF9EACC0), fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { save(tasks.map { if (it.id == task.id) it.copy(done = !it.done) else it }) }) { Text(if (task.done) "Reopen" else "Complete") }
                                Button(onClick = { save(tasks.filterNot { it.id == task.id }) }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
            Button(onClick = { save(tasks.filterNot { it.done }) }, enabled = tasks.any { it.done }, modifier = Modifier.fillMaxWidth()) { Text("Remove completed") }
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Jarvis") }
        }
    }
}
