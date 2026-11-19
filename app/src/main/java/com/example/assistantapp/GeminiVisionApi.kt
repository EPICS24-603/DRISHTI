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
            prompt = "Briefly describe the scene ahead. Mention any immediate obstacle or hazard first with direction, then one short actionable instruction. Keep it strictly under two short spoken sentences.",
            image = bitmap,
            maxOutputTokens = 70,
            context = context
        )
        withContext(Dispatchers.Main) { onPartialResult(result) }
    } catch (error: Exception) {
        Log.e("DrishtiAI", "Image analysis failed", error)
        withContext(Dispatchers.Main) { onError(error.message ?: "Could not analyze the image. Please try again.") }
    }
}
