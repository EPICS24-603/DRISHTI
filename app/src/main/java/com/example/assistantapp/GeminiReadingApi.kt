package com.example.assistantapp

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Image-to-text mode shares the same configured Gemini client as navigation. */
suspend fun readTextFromFrame(
    bitmap: Bitmap,
    onPartialResult: (String) -> Unit,
    onError: (String) -> Unit,
    context: Context? = null
) {
    try {
        val result = GeminiClient.ask(
            prompt = "Read all visible text from this image in natural reading order. Preserve paragraphs and numbers. If no text is legible, say 'No legible text found.'",
            image = bitmap,
            maxOutputTokens = 350,
            context = context
        )
        withContext(Dispatchers.Main) { onPartialResult(result) }
    } catch (error: Exception) {
        withContext(Dispatchers.Main) { onError(error.message ?: "Could not read the text. Please try again.") }
    }
}
