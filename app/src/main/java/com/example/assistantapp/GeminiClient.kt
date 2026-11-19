package com.example.assistantapp

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Small REST client shared by navigation, text reading, and voice questions. */
object GeminiClient {
    private const val MODEL = "gemini-3.5-flash-lite"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun ask(prompt: String, image: Bitmap? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isBlank()) {
            throw IllegalStateException("Gemini is not configured. Add GEMINI_API_KEY to local.properties and rebuild.")
        }

        val parts = JSONArray()
        if (image != null) {
            val imageBytes = java.io.ByteArrayOutputStream().use { output ->
                image.compress(Bitmap.CompressFormat.JPEG, 82, output)
                output.toByteArray()
            }
            parts.put(JSONObject().put("inline_data", JSONObject()
                .put("mime_type", "image/jpeg")
                .put("data", Base64.encodeToString(imageBytes, Base64.NO_WRAP))))
        }
        parts.put(JSONObject().put("text", prompt))

        val body = JSONObject()
            .put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT))))
            .put("contents", JSONArray().put(JSONObject().put("role", "user").put("parts", parts)))
            .put("generationConfig", JSONObject().put("temperature", 0.25).put("maxOutputTokens", 220))

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent")
            .header("x-goog-api-key", apiKey)
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val responseText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                Log.w("DrishtiAI", "Gemini request failed with HTTP ${response.code}")
                throw IOException(when (response.code) {
                    400 -> "The AI could not process that request. Please try again."
                    401, 403 -> "The Gemini API key is invalid or does not have access. Check local.properties."
                    429 -> "The AI service is busy. Please wait a moment and try again."
                    in 500..599 -> "The AI service is temporarily unavailable. Please try again."
                    else -> "The AI request failed. Please try again."
                })
            }
            val candidates = JSONObject(responseText).optJSONArray("candidates")
            val responseParts = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
            val answer = buildString {
                if (responseParts != null) for (index in 0 until responseParts.length()) {
                    append(responseParts.optJSONObject(index)?.optString("text").orEmpty())
                }
            }.trim()
            answer.ifBlank { "I could not find a clear answer. Please try again." }
        }
    }

    private const val SYSTEM_PROMPT = """
        You are DRISHTI, a calm and concise visual assistant for people who are blind or have low vision.
        For camera images, describe only details you can see. Start with the most important immediate hazard, then give one short, actionable suggestion. Never claim that a road is safe to cross or that an area is clear when you cannot verify it. Do not guess distances or object properties. Keep spoken answers to two short sentences.
        For reading requests, transcribe visible text in reading order and say when text is unclear. For questions, answer plainly using the provided scene context. Never replace a cane, guide dog, traffic signal, or trusted mobility aid.
    """.trimIndent()
}
