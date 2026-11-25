package com.openjarvis.assistant.ai

import com.openjarvis.assistant.security.ProviderSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class ChatMessage(val role: String, val content: String)

class OpenAiCompatibleClient {
    suspend fun complete(settings: ProviderSettings, messages: List<ChatMessage>): String =
        withContext(Dispatchers.IO) {
            require(settings.apiKey.isNotBlank()) { "API key is missing. Open AI Settings first." }
            require(settings.model.isNotBlank()) { "Model name is missing in AI Settings." }
            require(settings.baseUrl.startsWith("https://") || settings.baseUrl.startsWith("http://")) {
                "Provider URL must begin with http:// or https://"
            }

            val endpoint = settings.baseUrl.trimEnd('/').let {
                if (it.endsWith("/chat/completions")) it else "$it/chat/completions"
            }
            val requestMessages = JSONArray()
            messages.forEach { message ->
                requestMessages.put(JSONObject().put("role", message.role).put("content", message.content))
            }
            val body = JSONObject()
                .put("model", settings.model)
                .put("messages", requestMessages)
                .put("temperature", 0.65)
                .put("stream", false)
                .toString()

            val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 20_000
                readTimeout = 45_000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer ${settings.apiKey}")
            }

            try {
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val response = stream?.use { input ->
                    BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
                }.orEmpty()

                if (code !in 200..299) {
                    val providerMessage = runCatching {
                        JSONObject(response).optJSONObject("error")?.optString("message")
                    }.getOrNull().orEmpty()
                    throw IllegalStateException(
                        providerMessage.ifBlank { "AI provider returned HTTP $code. Check endpoint, model and API key." }
                    )
                }

                val json = JSONObject(response)
                val choices = json.optJSONArray("choices")
                    ?: throw IllegalStateException("Provider response did not include choices.")
                val message = choices.optJSONObject(0)?.optJSONObject("message")
                    ?: throw IllegalStateException("Provider response did not include a message.")
                val answer = message.opt("content")
                val text = when (answer) {
                    is String -> answer
                    is JSONArray -> buildString {
                        for (i in 0 until answer.length()) {
                            val item = answer.optJSONObject(i)
                            append(item?.optString("text").orEmpty())
                        }
                    }
                    else -> ""
                }.trim()
                if (text.isBlank()) throw IllegalStateException("The AI provider returned an empty answer.")
                text
            } finally {
                connection.disconnect()
            }
        }
}
