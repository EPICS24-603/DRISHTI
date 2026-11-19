package com.example.assistantapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.util.Locale

private val VoiceInk = Color(0xFF102A2A)
private val VoiceTeal = Color(0xFF087E78)
private val VoiceMint = Color(0xFFE3F5EF)

@Composable
fun VoiceAssistantScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var listening by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ready when you are") }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val recognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val requestPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        status = if (granted) "Microphone ready" else "Microphone permission is needed to ask a spoken question."
    }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask DRISHTI a question")
        }
    }

    LaunchedEffect(Unit) {
        tts = TextToSpeech(context) { result ->
            if (result != TextToSpeech.ERROR) tts?.language = Locale.getDefault()
        }
    }
    DisposableEffect(recognizer) {
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { status = "Listening — ask your question" }
            override fun onBeginningOfSpeech() { status = "I’m listening" }
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() { listening = false; status = "Thinking…" }
            override fun onError(error: Int) {
                listening = false
                status = if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) "I didn’t catch that. Tap Ask by voice to try again." else "Voice input stopped. Tap Ask by voice to try again."
            }
            override fun onResults(results: Bundle?) {
                listening = false
                val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (spoken.isNullOrBlank()) {
                    status = "I didn’t catch that. Tap Ask by voice to try again."
                    return
                }
                question = spoken
                status = "Getting an answer…"
                scope.launch {
                    answer = try {
                        askDrishti(spoken)
                    } catch (error: Exception) {
                        error.message ?: "Could not get an answer. Please try again."
                    }
                    status = "Answer ready"
                    tts?.speak(answer, TextToSpeech.QUEUE_FLUSH, null, "drishti-answer")
                }
            }
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        onDispose {
            recognizer.cancel()
            recognizer.destroy()
            tts?.stop()
            tts?.shutdown()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7F4)).padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Text("ASK DRISHTI", color = VoiceTeal, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.6.sp)
        Text("What would you\nlike to know?", color = VoiceInk, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("Ask about what you see or any other question. Your answer will be spoken aloud.", color = Color(0xFF526563), fontSize = 15.sp, lineHeight = 22.sp, textAlign = TextAlign.Center)

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("STATUS", color = VoiceTeal, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
                Text(status, color = VoiceInk, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                if (question.isNotBlank()) {
                    Text("YOU ASKED", color = Color(0xFF71807D), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    Text(question, color = VoiceInk, fontSize = 15.sp)
                }
                if (answer.isNotBlank()) {
                    Text("DRISHTI", color = Color(0xFF71807D), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                    Text(answer, color = VoiceInk, fontSize = 16.sp, lineHeight = 23.sp)
                    OutlinedButton(onClick = { tts?.speak(answer, TextToSpeech.QUEUE_FLUSH, null, "drishti-answer") }) {
                        Text("Repeat answer")
                    }
                }
            }
        }

        Button(
            onClick = {
                if (!hasPermission) requestPermission.launch(Manifest.permission.RECORD_AUDIO)
                else {
                    question = ""
                    answer = ""
                    listening = true
                    try { recognizer.startListening(speechIntent) } catch (_: Exception) {
                        listening = false
                        status = "Voice input isn’t available on this device."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VoiceTeal)
        ) {
            Icon(Icons.Filled.Mic, contentDescription = null)
            Text(if (listening) " Listening…" else " Ask by voice", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        if (listening) {
            OutlinedButton(onClick = { recognizer.cancel(); listening = false; status = "Ready when you are" }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Stop, contentDescription = null)
                Text(" Stop listening")
            }
        }
        Text("Your question is sent to Gemini to create an answer.", color = Color(0xFF71807D), fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}
