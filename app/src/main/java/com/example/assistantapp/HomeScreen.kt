package com.example.assistantapp

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.util.Locale

private val Ink = Color(0xFF102A2A)
private val Teal = Color(0xFF087E78)
private val Mint = Color(0xFFE3F5EF)
private val Paper = Color(0xFFF5F7F4)

@Composable
fun MainPage(navController: NavHostController) {
    val context = LocalContext.current
    val helpVisible = remember { mutableStateOf(false) }
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }

    LaunchedEffect(Unit) {
        tts.value = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.value?.language = Locale.getDefault()
                tts.value?.setSpeechRate(0.92f)
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            tts.value?.stop()
            tts.value?.shutdown()
        }
    }

    Box(Modifier.fillMaxSize().background(Paper)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Mint, modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.AccessibilityNew, contentDescription = null, tint = Teal, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("DRISHTI", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    Text("YOUR WORLD, WITH MORE CLARITY", color = Teal, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                TextButton(onClick = { helpVisible.value = true }) {
                    Icon(Icons.Filled.HelpOutline, contentDescription = "Open help", tint = Ink)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF123837), Color(0xFF176B65))), RoundedCornerShape(28.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(color = Color.White.copy(alpha = 0.14f), shape = RoundedCornerShape(50)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color(0xFFBCEADF), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(7.dp))
                        Text("AI ASSISTANCE, MADE ACCESSIBLE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    }
                }
                Text("Move through\nyour day with confidence.", color = Color.White, fontSize = 29.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold)
                Text("Choose a tool to understand what’s around you, ask a question, or read printed text aloud.", color = Color(0xFFE1F2ED), fontSize = 15.sp, lineHeight = 22.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Headphones, contentDescription = null, tint = Color(0xFFBCEADF), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Voice guidance is available throughout the app", color = Color.White, fontSize = 12.sp)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("CHOOSE A MODE", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp)
                ModeCard(
                    icon = Icons.Filled.CameraAlt,
                    title = "Live navigation",
                    description = "Hear a short description of the scene ahead.",
                    badge = "CAMERA",
                    onClick = { navController.navigate("blindMode") }
                )
                ModeCard(
                    icon = Icons.Filled.Mic,
                    title = "Ask DRISHTI",
                    description = "Ask a question by voice and hear the answer.",
                    badge = "VOICE",
                    onClick = { navController.navigate("voiceAssistant") }
                )
                ModeCard(
                    icon = Icons.Filled.Book,
                    title = "Read text",
                    description = "Point the camera at a sign, label, or page.",
                    badge = "READ ALOUD",
                    onClick = { navController.navigate("readText") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp)).padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Filled.AccessibilityNew, contentDescription = null, tint = Teal, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("A helpful companion, not a replacement for a cane or guide dog.", color = Ink, fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold)
                    Text("Use your usual mobility tools and follow local safety signals.", color = Color(0xFF526563), fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
            Text("VIT BHOPAL · GROUP 36", color = Color(0xFF71807D), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(12.dp))
        }
    }

    if (helpVisible.value) {
        AlertDialog(
            onDismissRequest = { helpVisible.value = false },
            title = { Text("Getting started", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Live navigation uses the camera to describe the scene in short spoken updates.")
                    Text("Ask DRISHTI listens for a question and reads its answer aloud.")
                    Text("Read text captures a page or sign and speaks the words it can recognize.")
                    Text("Allow camera and microphone access when prompted. An internet connection and a Gemini API key are needed for AI features.")
                }
            },
            confirmButton = { Button(onClick = { helpVisible.value = false }) { Text("Done") } }
        )
    }
}

@Composable
private fun ModeCard(icon: ImageVector, title: String, description: String, badge: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = Mint, shape = RoundedCornerShape(16.dp), modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Teal, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(description, color = Color(0xFF526563), fontSize = 12.sp, lineHeight = 17.sp)
                Text(badge, color = Teal, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp)
            }
            Icon(Icons.Filled.ArrowForward, contentDescription = "Open $title", tint = Teal)
        }
    }
}
