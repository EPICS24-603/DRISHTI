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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    val epicsDialogVisible = remember { mutableStateOf(false) }
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }

    if (epicsDialogVisible.value) {
        AboutEpicsDialog(onDismiss = { epicsDialogVisible.value = false })
    }

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
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            
            // Top Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Mint, modifier = Modifier.size(46.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.AccessibilityNew, contentDescription = null, tint = Teal, modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("DRISHTI", color = Ink, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                    Text("EPICS GROUP 603 • VIT BHOPAL", color = Teal, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                IconButton(onClick = { epicsDialogVisible.value = true }) {
                    Icon(Icons.Filled.Info, contentDescription = "EPICS Team Info", tint = Teal)
                }
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Open Settings", tint = Ink)
                }
            }

            // Hero Banner
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF103635), Color(0xFF146A64))), RoundedCornerShape(26.dp))
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(color = Color.White.copy(alpha = 0.16f), shape = RoundedCornerShape(50)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color(0xFFBCEADF), modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("POWERED BY GEMINI AI", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    }
                }
                Text("Your world, with more clarity.", color = Color.White, fontSize = 27.sp, lineHeight = 33.sp, fontWeight = FontWeight.Bold)
                Text("Select one of the 3 accessibility modes below for real-time guidance, voice questions, or text reading.", color = Color(0xFFE1F2ED), fontSize = 14.sp, lineHeight = 20.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Headphones, contentDescription = null, tint = Color(0xFFBCEADF), modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("High-contrast and voice-guided assistance", color = Color.White, fontSize = 12.sp)
                }
            }

            // The 3 Distinct Modes
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("3 ACCESSIBILITY MODES", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
                    Spacer(Modifier.weight(1f))
                    Text("TAP TO START", color = Teal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                ModeCard(
                    icon = Icons.Filled.CameraAlt,
                    title = "Mode 1: AI Navigation",
                    description = "Fast real-time obstacle & hazard detection with directional speech guidance.",
                    badge = "LIVE CAMERA",
                    badgeColor = Color(0xFF00796B),
                    onClick = { navController.navigate("blindMode") }
                )
                ModeCard(
                    icon = Icons.Filled.Mic,
                    title = "Mode 2: AI Voice Assistant",
                    description = "Ask questions by voice about your immediate surroundings or general queries.",
                    badge = "TWO-WAY VOICE",
                    badgeColor = Color(0xFF303F9F),
                    onClick = { navController.navigate("voiceAssistant") }
                )
                ModeCard(
                    icon = Icons.Filled.Book,
                    title = "Mode 3: Read Text Aloud",
                    description = "Point at signs, documents, labels, or medicine packaging to hear them read aloud.",
                    badge = "OCR READER",
                    badgeColor = Color(0xFFE65100),
                    onClick = { navController.navigate("readText") }
                )
            }

            // EPICS Project Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F3F0)),
                onClick = { epicsDialogVisible.value = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.VolunteerActivism, contentDescription = null, tint = Teal, modifier = Modifier.padding(10.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("EPICS Engineering Team (Group 603)", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Sumit Prasad, Divyanshu, Bhavya, Faiz, Dhruv", color = Color(0xFF527471), fontSize = 12.sp)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Teal, modifier = Modifier.size(18.dp))
                }
            }

            // Safety Warning Notice
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp)).padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Filled.AccessibilityNew, contentDescription = null, tint = Teal, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Safety Notice", color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("DRISHTI is an assistive companion, not a replacement for a white cane, guide dog, or personal judgment.", color = Color(0xFF526563), fontSize = 12.sp, lineHeight = 18.sp)
                }
            }

            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun ModeCard(
    icon: ImageVector,
    title: String,
    description: String,
    badge: String,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = Mint, modifier = Modifier.size(42.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = Teal, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(badge, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
                Surface(shape = CircleShape, color = Color(0xFFF0F4F3), modifier = Modifier.size(34.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Teal, modifier = Modifier.size(18.dp))
                    }
                }
            }
            Text(description, color = Color(0xFF47625F), fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}
