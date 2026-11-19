package com.example.assistantapp

import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun analyzeNavigationFrame(
    bitmap: Bitmap,
    onPartialResult: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val result = GeminiClient.ask(
            "Briefly describe the scene ahead. Mention an immediate obstacle first, then one useful action. Keep it to two short spoken sentences.",
            bitmap,
            maxOutputTokens = 80
        )
        withContext(Dispatchers.Main) { onPartialResult(result) }
    } catch (error: Exception) {
        Log.e("DrishtiAI", "Image analysis failed", error)
        withContext(Dispatchers.Main) { onError(error.message ?: "Could not analyze the image. Please try again.") }
    }
}
