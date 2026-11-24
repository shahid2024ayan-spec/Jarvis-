package com.openjarvis.assistant

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.openjarvis.assistant.voice.SpeechController
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var speechController: SpeechController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speechController = SpeechController(this)
        setContent {
            JarvisTheme {
                JarvisDashboard(speechController, this)
            }
        }
    }

    override fun onDestroy() {
        if (::speechController.isInitialized) speechController.release()
        super.onDestroy()
    }
}

@Composable
private fun JarvisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF62D9FF),
            secondary = Color(0xFF8B9DFF),
            background = Color(0xFF080D18),
            surface = Color(0xFF111A2B)
        ),
        content = content
    )
}

@Composable
private fun JarvisDashboard(speechController: SpeechController, context: Context) {
    var status by remember { mutableStateOf("Ready. Tap the microphone to speak.") }
    var recognizedText by remember { mutableStateOf("Your speech will appear here.") }

    val microphonePermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            speechController.startListening(
                locale = Locale("ur", "PK"),
                onText = { phrase ->
                    recognizedText = phrase
                    speechController.speak("Aap ne kaha: $phrase")
                },
                onStatus = { status = it }
            )
        } else {
            status = "Microphone permission was not granted."
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF080D18)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("OPEN JARVIS", color = Color(0xFF62D9FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Your personal AI", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                }
                Text("● FOUNDATION", color = Color(0xFF65D6A3), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                    Text("Hello, I'm Jarvis", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(status, color = Color(0xFFB8C7D9), fontSize = 13.sp)
                    Button(
                        onClick = {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) {
                                speechController.startListening(
                                    locale = Locale("ur", "PK"),
                                    onText = { phrase ->
                                        recognizedText = phrase
                                        speechController.speak("Aap ne kaha: $phrase")
                                    },
                                    onStatus = { status = it }
                                )
                            } else {
                                microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    ) {
                        Text("🎙  Tap to speak")
                    }
                }
            }

            Text("WHAT I HEARD", color = Color(0xFF8EA6C2), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111A2B))
            ) {
                Text(
                    recognizedText,
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Text("MODULES", color = Color(0xFF8EA6C2), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            CapabilityCard("Voice foundation", "Speech recognition and spoken output")
            CapabilityCard("AI conversation", "Multiple model providers — in development")
            CapabilityCard("Memory & automation", "Private memory and confirmed tasks — planned")

            Spacer(modifier = Modifier.weight(1f))
            Text(
                "FOUNDATION BUILD • v0.1.0",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = Color(0xFF61738B),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun CapabilityCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111A2B))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(subtitle, color = Color(0xFF91A3B9), fontSize = 12.sp)
        }
    }
}
