package com.example.assistantapp

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun analyzeNavigationFrame(
    bitmap: Bitmap,
    onPartialResult: (String) -> Unit,
    onError: (String) -> Unit,
    context: Context? = null
) {
    try {
        val result = GeminiClient.ask(
            prompt = "Describe the immediate path ahead. Name the nearest hazard/obstacle with clock direction first, then one clear action. Under two short sentences.",
            image = bitmap,
            maxOutputTokens = 70,
            task = AiTask.NAVIGATION,
            context = context
        )
        withContext(Dispatchers.Main) { onPartialResult(result) }
    } catch (error: Exception) {
        Log.e("DrishtiAI", "Navigation analysis failed", error)
        withContext(Dispatchers.Main) { onError(error.message ?: "Could not analyze the image.") }
    }
}
