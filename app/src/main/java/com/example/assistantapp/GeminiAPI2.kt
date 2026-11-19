package com.example.assistantapp

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Image-to-text mode shares the same configured Gemini client as navigation. */
suspend fun sendFrameToGemini2AI(
    bitmap: Bitmap,
    onPartialResult: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val result = GeminiClient.ask(
            "Read all visible text from this image in natural reading order. Preserve paragraphs and numbers. If no text is legible, say so briefly.",
            bitmap,
            maxOutputTokens = 500
        )
        withContext(Dispatchers.Main) { onPartialResult(result) }
    } catch (error: Exception) {
        withContext(Dispatchers.Main) { onError(error.message ?: "Could not read the text. Please try again.") }
    }
}
