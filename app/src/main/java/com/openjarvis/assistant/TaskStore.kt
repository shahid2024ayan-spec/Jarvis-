package com.openjarvis.assistant

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class JarvisTask(val id: Long, val title: String, val detail: String, val done: Boolean)

class TaskStore(context: Context) {
    private val prefs = context.getSharedPreferences("jarvis_tasks", Context.MODE_PRIVATE)

    fun load(): List<JarvisTask> {
        val array = runCatching { JSONArray(prefs.getString("items", "[]")) }.getOrElse { JSONArray() }
        return (0 until array.length()).mapNotNull { i ->
            runCatching {
                val item = array.getJSONObject(i)
                JarvisTask(item.getLong("id"), item.getString("title"), item.optString("detail"), item.optBoolean("done"))
            }.getOrNull()
        }
    }

    fun save(tasks: List<JarvisTask>) {
        val array = JSONArray()
        tasks.take(300).forEach { task ->
            array.put(JSONObject().put("id", task.id).put("title", task.title)
                .put("detail", task.detail).put("done", task.done))
        }
        check(prefs.edit().putString("items", array.toString()).commit()) { "Could not save tasks" }
    }
}
