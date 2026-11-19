package com.example.assistantapp

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Image-to-text mode using the high-accuracy Gemini 2.5 Flash model for OCR. */
suspend fun readTextFromFrame(
    bitmap: Bitmap,
    onPartialResult: (String) -> Unit,
    onError: (String) -> Unit,
    context: Context? = null
) {
    try {
        val result = GeminiClient.ask(
            prompt = "Transcribe all visible text from this image accurately in reading order. Mention what kind of item/document it is if clear.",
            image = bitmap,
            maxOutputTokens = 350,
            task = AiTask.READING,
            context = context
        )
        withContext(Dispatchers.Main) { onPartialResult(result) }
    } catch (error: Exception) {
        withContext(Dispatchers.Main) { onError(error.message ?: "Could not read text. Please hold camera closer.") }
    }
}
