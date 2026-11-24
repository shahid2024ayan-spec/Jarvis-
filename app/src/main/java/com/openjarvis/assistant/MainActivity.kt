package com.openjarvis.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JarvisTheme {
                JarvisDashboard()
            }
        }
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
private fun JarvisDashboard() {
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
                Text("● READY", color = Color(0xFF65D6A3), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    Text(
                        "Your assistant is ready for its first setup.",
                        color = Color(0xFFB8C7D9),
                        fontSize = 14.sp
                    )
                    Button(onClick = { }) {
                        Text("Voice setup — coming next")
                    }
                }
            }

            Text("CAPABILITIES", color = Color(0xFF8EA6C2), fontSize = 12.sp, fontWeight = FontWeight.Bold)

            CapabilityCard("AI Conversation", "Multiple model providers")
            CapabilityCard("Phone Intelligence", "Permissions and device actions")
            CapabilityCard("Memory & Automation", "Private memory and confirmed tasks")

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
