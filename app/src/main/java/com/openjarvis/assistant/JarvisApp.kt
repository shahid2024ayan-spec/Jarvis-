package com.openjarvis.assistant

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.openjarvis.assistant.ai.ChatMessage
import com.openjarvis.assistant.ai.OpenAiCompatibleClient
import com.openjarvis.assistant.security.EncryptedSettings
import com.openjarvis.assistant.security.ProviderSettings
import com.openjarvis.assistant.voice.SpeechController
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import java.util.Locale

@Composable
fun JarvisApp(speechController: SpeechController, context: Context) {
    var page by remember { mutableStateOf("home") }
    val secureSettings = remember { EncryptedSettings(context) }
    val saved = remember { secureSettings.loadProvider() }
    var baseUrl by remember { mutableStateOf(saved?.baseUrl ?: "https://api.openai.com/v1") }
    var apiKey by remember { mutableStateOf(saved?.apiKey.orEmpty()) }
    var model by remember { mutableStateOf(saved?.model ?: "gpt-4o-mini") }
    var setupStatus by remember { mutableStateOf("API credentials are encrypted on this device.") }

    when (page) {
        "settings" -> ProviderSetup(
            baseUrl = baseUrl,
            onBaseUrl = { baseUrl = it },
            apiKey = apiKey,
            onApiKey = { apiKey = it },
            model = model,
            onModel = { model = it },
            status = setupStatus,
            onSave = {
                if (baseUrl.isBlank() || apiKey.isBlank() || model.isBlank()) {
                    setupStatus = "Fill in the endpoint, API key and model name."
                } else {
                    runCatching { secureSettings.saveProvider(baseUrl, apiKey, model) }
                        .onSuccess { setupStatus = "Provider settings saved securely." }
                        .onFailure { setupStatus = "Could not save settings: ${it.message}" }
                }
            },
            onBack = { page = "home" }
        )
        "chat" -> ChatScreen(
            provider = secureSettings.loadProvider(),
            onBack = { page = "home" },
            speechController = speechController,
            context = context
        )
        else -> HomeScreen(
            onChat = { page = "chat" },
            onSettings = { page = "settings" },
            onVoice = { page = "chat" }
        )
    }
}

@Composable
private fun HomeScreen(onChat: () -> Unit, onSettings: () -> Unit, onVoice: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF080D18)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("OPEN JARVIS", color = Color(0xFF62D9FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Your personal AI", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13243A))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("◉", color = Color(0xFF62D9FF), fontSize = 64.sp)
                    Text("Jarvis is ready", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                    Text("Configure an AI provider, then start a conversation.", color = Color(0xFFB8C7D9))
                    Button(onClick = onChat) { Text("Open AI Chat") }
                    Button(onClick = onVoice) { Text("Voice Conversation") }
                }
            }
            Text("YOUR MODULES", color = Color(0xFF8EA6C2), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            HomeFeature("AI Chat", "Connect an OpenAI-compatible model")
            HomeFeature("Secure settings", "Provider key encrypted with Android Keystore")
            HomeFeature("Voice foundation", "Android speech recognition and speech output")
            HomeFeature("Next modules", "Wake word, memory, screen vision and automation")
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onSettings, modifier = Modifier.fillMaxWidth()) { Text("AI Provider Settings") }
            Text("Open Jarvis • Foundation", modifier = Modifier.align(Alignment.CenterHorizontally), color = Color(0xFF61738B), fontSize = 11.sp)
        }
    }
}

@Composable
private fun HomeFeature(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111A2B))
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color(0xFF91A3B9), fontSize = 12.sp)
        }
    }
}

@Composable
private fun ProviderSetup(
    baseUrl: String,
    onBaseUrl: (String) -> Unit,
    apiKey: String,
    onApiKey: (String) -> Unit,
    model: String,
    onModel: (String) -> Unit,
    status: String,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF080D18)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("AI PROVIDER", color = Color(0xFF62D9FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Connect your model", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Works with providers exposing the OpenAI Chat Completions API. API key stays encrypted locally.", color = Color(0xFFB8C7D9), fontSize = 13.sp)
            OutlinedTextField(baseUrl, onBaseUrl, modifier = Modifier.fillMaxWidth(), label = { Text("API Base URL") }, singleLine = true)
            OutlinedTextField(
                apiKey, onApiKey, modifier = Modifier.fillMaxWidth(),
                label = { Text("API Key (private)") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            OutlinedTextField(model, onModel, modifier = Modifier.fillMaxWidth(), label = { Text("Model ID") }, singleLine = true)
            Text(status, color = Color(0xFF8EDDB5), fontSize = 12.sp)
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("Save securely") }
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
            Text("Never paste your API key into chat or commit it to GitHub.", color = Color(0xFF8EA6C2), fontSize = 12.sp)
        }
    }
}

@Composable
private fun ChatScreen(
    provider: ProviderSettings?,
    onBack: () -> Unit,
    speechController: SpeechController,
    context: Context
) {
    val messages = remember {
        mutableStateListOf(
            ChatMessage("system", "You are Open Jarvis, a helpful personal assistant. Reply naturally in Roman Urdu or English based on the user's language. Never claim to control the phone unless an implemented, permission-granted tool actually did it.")
        )
    }
    var input by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(if (provider == null) "Set up an AI provider first." else "Connected configuration loaded. Send a message.") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val client = remember { OpenAiCompatibleClient() }

    fun sendMessage(text: String) {
        val prompt = text.trim()
        if (prompt.isEmpty() || busy) return
        if (provider == null) {
            status = "Open AI Provider Settings and save your key first."
            return
        }
        messages.add(ChatMessage("user", prompt))
        input = ""
        busy = true
        status = "Jarvis is thinking…"
        scope.launch {
            try {
                val answer = client.complete(provider, messages.toList())
                messages.add(ChatMessage("assistant", answer))
                status = "Reply received."
                speechController.speak(answer, Locale.US)
            } catch (error: Exception) {
                status = error.message ?: "AI request failed. Check internet and provider settings."
                messages.add(ChatMessage("assistant", "I couldn't complete that request: $status"))
            } finally {
                busy = false
            }
        }
    }

    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            speechController.startListening(
                locale = Locale("ur", "PK"),
                onText = { phrase -> sendMessage(phrase) },
                onStatus = { status = it }
            )
        } else status = "Microphone permission is needed for voice input."
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF080D18)) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBack) { Text("←") }
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text("JARVIS CHAT", color = Color(0xFF62D9FF), fontWeight = FontWeight.Bold)
                    Text(status, color = Color(0xFF9EACC0), fontSize = 11.sp)
                }
            }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(messages.filter { it.role != "system" }) { _, message ->
                    val isUser = message.role == "user"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) Color(0xFF16334A) else Color(0xFF111A2B)
                        )
                    ) {
                        Column(modifier = Modifier.padding(13.dp)) {
                            Text(if (isUser) "YOU" else "JARVIS", color = Color(0xFF62D9FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(message.content, color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message Jarvis…") },
                    maxLines = 4
                )
                Button(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        if (granted) speechController.startListening(
                            locale = Locale("ur", "PK"),
                            onText = { sendMessage(it) },
                            onStatus = { status = it }
                        ) else micPermission.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    enabled = !busy,
                    modifier = Modifier.padding(start = 5.dp)
                ) { Text("🎙") }
            }
            Button(
                onClick = { sendMessage(input) },
                enabled = !busy && input.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
            ) { Text(if (busy) "Thinking…" else "Send message") }
        }
    }
}
