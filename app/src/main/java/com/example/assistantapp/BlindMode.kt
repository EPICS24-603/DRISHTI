package com.example.assistantapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun BlindModeScreen(
    startInReadMode: Boolean = false,
    navController: NavHostController? = null
) {
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    var currentMode by remember { mutableStateOf(if (startInReadMode) "reading" else "navigation") }
    val coroutineScope = rememberCoroutineScope()
    var isAssistantMode by remember { mutableStateOf(false) }
    var sessionStarted by remember { mutableStateOf(true) }
    var analysisResult by remember { mutableStateOf("") }
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    var lastProcessedTimestamp by remember { mutableStateOf(0L) }
    
    // Configurable navigation cadence (default 3s instead of 6s)
    val frameInterval = remember { AppPreferences.getNavIntervalMs(context) }
    val motionDetector = remember { MotionDetector() }
    val motionFilterEnabled = remember { AppPreferences.isMotionFilterEnabled(context) }
    var isAnalyzing by remember { mutableStateOf(false) }

    var navigationPaused by remember { mutableStateOf(startInReadMode) }
    var isMicActive by remember { mutableStateOf(false) }
    var chatResponse by remember { mutableStateOf("") }
    var isVoiceMode by remember { mutableStateOf(startInReadMode) }
    var voiceModeResult by remember { mutableStateOf("") }
    var showInstructions by remember { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        hasPermission = grants.values.all { it }
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
    }

    LaunchedEffect(context) {
        tts.value = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.value?.language = Locale.US
                tts.value?.setSpeechRate(0.92f)
                tts.value?.setPitch(1.0f)
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(600)
        if (tts.value != null) {
            val intervalSec = frameInterval / 1000
            val welcomeText = if (startInReadMode) {
                "Text Reading mode activated. Point camera at documents, signs, or labels."
            } else {
                "Navigation started. Real-time guidance every $intervalSec seconds. Tap top for assistant, bottom for text reader."
            }
            tts.value?.speak(welcomeText, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    LaunchedEffect(hasPermission) {
        if (!hasPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            tts.value?.stop()
            tts.value?.shutdown()
            speechRecognizer.destroy()
        }
    }

    LaunchedEffect(Unit) {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val spokenText = matches[0]
                    coroutineScope.launch {
                        chatResponse = try {
                            askDrishti(spokenText, analysisResult, context = context)
                        } catch (error: Exception) {
                            error.message ?: "I could not get an answer. Please try again."
                        }
                        tts.value?.speak(chatResponse, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                }
                isMicActive = false
            }
            override fun onReadyForSpeech(params: Bundle?) { isMicActive = true }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isMicActive = false }
            override fun onError(error: Int) { isMicActive = false }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    LaunchedEffect(isAssistantMode) {
        if (isAssistantMode) {
            isMicActive = true
            speechRecognizer.startListening(speechIntent)
        } else {
            isMicActive = false
            speechRecognizer.stopListening()
            chatResponse = ""
        }
    }

    if (hasPermission) {
        if (sessionStarted) {
            if (isVoiceMode) {
                VoiceModeCamera(
                    onImageCaptured = { bitmap: Bitmap ->
                        coroutineScope.launch {
                            voiceModeResult = ""
                            tts.value?.speak("Reading text. Please hold still.", TextToSpeech.QUEUE_FLUSH, null, null)
                            readTextFromFrame(bitmap, { text ->
                                voiceModeResult = text
                                tts.value?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                            }, { error ->
                                tts.value?.speak("Could not read text. Please hold camera closer.", TextToSpeech.QUEUE_FLUSH, null, null)
                            }, context = context)
                        }
                    },
                    cameraExecutor = cameraExecutor
                )
            } else if (!navigationPaused) {
                CameraPreviewWithAnalysis { imageProxy ->
                    val currentTimestamp = System.currentTimeMillis()
                    if (!isAnalyzing && currentTimestamp - lastProcessedTimestamp >= frameInterval) {
                        val bitmap = imageProxy.toBitmap()
                        imageProxy.close()
                        if (bitmap != null) {
                            // Smart Token Optimization: Skip redundant analysis if camera is static
                            val shouldAnalyze = !motionFilterEnabled || motionDetector.hasSceneChanged(bitmap)
                            if (shouldAnalyze) {
                                lastProcessedTimestamp = currentTimestamp
                                isAnalyzing = true
                                coroutineScope.launch {
                                    try {
                                        analyzeNavigationFrame(bitmap, { result ->
                                            analysisResult = result
                                            tts.value?.speak(result, TextToSpeech.QUEUE_FLUSH, null, null)
                                        }, {
                                            // Silently ignore transient errors
                                        }, context = context)
                                    } finally {
                                        isAnalyzing = false
                                    }
                                }
                            }
                        }
                    } else {
                        imageProxy.close()
                    }
                }
            }
        }
    }

    // Accessible Screen Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        val screenHeight = size.height
                        val y = offset.y
                        val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator

                        when {
                            y < screenHeight * 0.33 -> {
                                // Top third - Toggle Assistant Mode
                                if (!isVoiceMode) {
                                    navigationPaused = !navigationPaused
                                    isAssistantMode = navigationPaused
                                    vibrate(vibrator, 100)
                                    if (navigationPaused) {
                                        tts.value?.stop()
                                        currentMode = "assistant"
                                        tts.value?.speak("Assistant mode activated. Speak your question.", TextToSpeech.QUEUE_FLUSH, null, null)
                                    } else {
                                        tts.value?.stop()
                                        currentMode = "navigation"
                                        chatResponse = ""
                                        tts.value?.speak("Navigation resumed.", TextToSpeech.QUEUE_FLUSH, null, null)
                                    }
                                }
                            }
                            y > screenHeight * 0.66 -> {
                                // Bottom third - Toggle Reading Mode
                                if (!isAssistantMode) {
                                    isVoiceMode = !isVoiceMode
                                    vibrate(vibrator, 200)
                                    if (isVoiceMode) {
                                        tts.value?.stop()
                                        currentMode = "reading"
                                        navigationPaused = true
                                        tts.value?.speak("Text reader activated. Point at text to hear it.", TextToSpeech.QUEUE_FLUSH, null, null)
                                    } else {
                                        tts.value?.stop()
                                        currentMode = "navigation"
                                        voiceModeResult = ""
                                        navigationPaused = false
                                        tts.value?.speak("Navigation resumed.", TextToSpeech.QUEUE_FLUSH, null, null)
                                    }
                                }
                            }
                            else -> {
                                // Middle third - Status readout
                                vibrate(vibrator, 50)
                                tts.value?.speak(
                                    "Current mode: $currentMode. Navigation is ${if (navigationPaused) "paused" else "active"}.",
                                    TextToSpeech.QUEUE_FLUSH, null, null
                                )
                            }
                        }
                    }
                )
            }
    ) {
        if (sessionStarted) {
            AIResponseOverlay(
                currentMode = currentMode,
                navigationResponse = analysisResult,
                response = analysisResult,
                chatResponse = chatResponse,
                readingModeResult = voiceModeResult,
                tts = tts.value,
                lastSpokenIndex = 0
            )
        }

        // Top Navigation Bar with Back Button
        if (navController != null) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .size(46.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit to Home", tint = Color.White)
                }
            }
        }

        // Live Mode Pill at Top Center
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = if (isVoiceMode) Icons.Filled.Book else Icons.Filled.Navigation
                val label = if (isVoiceMode) "READ TEXT MODE" else if (navigationPaused) "ASSISTANT PAUSED" else "LIVE NAVIGATION"
                Icon(icon, contentDescription = null, tint = Color(0xFF80CBC4), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }

        // Status Indicators
        if (isMicActive) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF00C853),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
                    .size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Mic, contentDescription = "Listening", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }

        // First-run instructions overlay
        if (showInstructions) {
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF0102A2A))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("DRISHTI Touch Controls", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("• Tap TOP: Pause & Ask questions by voice", color = Color(0xFFB2DFDB), fontSize = 13.sp)
                    Text("• Tap CENTER: Speak current status", color = Color(0xFFB2DFDB), fontSize = 13.sp)
                    Text("• Tap BOTTOM: Switch to Read Text mode", color = Color(0xFFB2DFDB), fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { showInstructions = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B))
                    ) {
                        Text("Start Navigation", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun vibrate(vibrator: Vibrator, ms: Long) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(ms)
    }
}

@Composable
fun VoiceModeCamera(
    onImageCaptured: (Bitmap) -> Unit,
    cameraExecutor: ExecutorService
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture
        )
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val captureFile = java.io.File.createTempFile("drishti-capture-", ".jpg", context.cacheDir)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(captureFile).build()
        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(captureFile.absolutePath)
                    captureFile.delete()
                    if (bitmap != null) onImageCaptured(bitmap)
                }

                override fun onError(exception: ImageCaptureException) {
                    captureFile.delete()
                }
            }
        )
    }

    AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
}
