package com.openjarvis.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.openjarvis.assistant.voice.SpeechController

class MainActivity : ComponentActivity() {
    private lateinit var speechController: SpeechController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        speechController = SpeechController(this)
        setContent {
            JarvisTheme {
                JarvisApp(speechController, this)
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
