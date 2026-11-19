package com.example.assistantapp

import android.content.Context
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
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

enum class AiTask {
    NAVIGATION,
    READING,
    ASSISTANT
}

/**
 * Task-optimized Gemini client that divides and selects the best model for each specific case:
 * - NAVIGATION: gemini-2.5-flash-lite (fastest inference, lowest latency for obstacle warnings)
 * - READING: gemini-2.5-flash (highest OCR accuracy for small print, labels, signs)
 * - ASSISTANT: gemini-2.5-flash (strong reasoning and conversational dialogue)
 */
object GeminiClient {
    private const val MODEL_FAST = "gemini-2.5-flash-lite"
    private const val MODEL_ACCURATE = "gemini-2.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    suspend fun ask(
        prompt: String,
        image: Bitmap? = null,
        maxOutputTokens: Int = 80,
        task: AiTask = AiTask.NAVIGATION,
        context: Context? = null,
        apiKeyOverride: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = apiKeyOverride?.trim()?.ifBlank { null }
            ?: context?.let { AppPreferences.getEffectiveApiKey(it) }?.ifBlank { null }
            ?: BuildConfig.GEMINI_API_KEY.trim()

        if (apiKey.isBlank()) {
            throw IllegalStateException("API Key not found. Please tap Settings on the home screen to paste your Gemini API key.")
        }

        // Select the ideal model for each use case
        val primaryModel = when (task) {
            AiTask.NAVIGATION -> MODEL_FAST
            AiTask.READING -> MODEL_ACCURATE
            AiTask.ASSISTANT -> MODEL_ACCURATE
        }

        try {
            executeRequest(primaryModel, apiKey, prompt, image, maxOutputTokens, task)
        } catch (e: Exception) {
            // Automatic fallback to accurate model if fast-lite encounters rate limit or temporary outage
            if (primaryModel != MODEL_ACCURATE) {
                Log.w("DrishtiAI", "Primary model $primaryModel failed, attempting fallback to $MODEL_ACCURATE", e)
                executeRequest(MODEL_ACCURATE, apiKey, prompt, image, maxOutputTokens, task)
            } else {
                throw e
            }
        }
    }

    private fun executeRequest(
        model: String,
        apiKey: String,
        prompt: String,
        image: Bitmap?,
        maxOutputTokens: Int,
        task: AiTask
    ): String {
        val parts = JSONArray()
        if (image != null) {
            val uploadImage = scaleForUpload(image, task)
            val imageBytes = ByteArrayOutputStream().use { output ->
                uploadImage.compress(Bitmap.CompressFormat.JPEG, if (task == AiTask.READING) 85 else 75, output)
                output.toByteArray()
            }
            parts.put(
                JSONObject().put(
                    "inlineData", JSONObject()
                        .put("mimeType", "image/jpeg")
                        .put("data", Base64.encodeToString(imageBytes, Base64.NO_WRAP))
                )
            )
        }
        parts.put(JSONObject().put("text", prompt))

        val systemPrompt = when (task) {
            AiTask.NAVIGATION -> SYSTEM_PROMPT_NAV
            AiTask.READING -> SYSTEM_PROMPT_READING
            AiTask.ASSISTANT -> SYSTEM_PROMPT_ASSISTANT
        }

        val body = JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt))))
            .put("contents", JSONArray().put(JSONObject().put("role", "user").put("parts", parts)))
            .put("generationConfig", JSONObject().put("temperature", if (task == AiTask.READING) 0.05 else 0.15).put("maxOutputTokens", maxOutputTokens.coerceIn(32, 512)))

        val request = Request.Builder()
            .url("$BASE_URL/$model:generateContent?key=$apiKey")
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val responseText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                Log.w("DrishtiAI", "Gemini request on $model failed with HTTP ${response.code}: $responseText")
                throw IOException(when (response.code) {
                    400 -> "Request could not be processed. Please try again."
                    401, 403 -> "Invalid API key. Please check your key in Settings."
                    429 -> "AI rate limit reached. Pausing momentarily."
                    in 500..599 -> "AI service temporarily unavailable. Retrying."
                    else -> "Connection error (${response.code})."
                })
            }
            val candidates = JSONObject(responseText).optJSONArray("candidates")
            val responseParts = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
            val answer = buildString {
                if (responseParts != null) for (index in 0 until responseParts.length()) {
                    append(responseParts.optJSONObject(index)?.optString("text").orEmpty())
                }
            }.trim()
            return answer.ifBlank {
                if (task == AiTask.READING) "No legible text found." else "Path is clear ahead."
            }
        }
    }

    suspend fun testConnection(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        val key = apiKey.trim()
        if (key.isBlank()) return@withContext Result.failure(IllegalArgumentException("API Key cannot be blank."))
        try {
            val body = JSONObject()
                .put("contents", JSONArray().put(JSONObject().put("role", "user").put("parts", JSONArray().put(JSONObject().put("text", "ping")))))
                .put("generationConfig", JSONObject().put("maxOutputTokens", 5))

            val request = Request.Builder()
                .url("$BASE_URL/$MODEL_ACCURATE:generateContent?key=$key")
                .header("Content-Type", "application/json")
                .post(body.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success("Connection Successful! Gemini 2.5 Flash is active and verified.")
                } else {
                    Result.failure(IOException("Verification failed with HTTP ${response.code}. Check the key."))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun scaleForUpload(bitmap: Bitmap, task: AiTask): Bitmap {
        // Navigation uses ultra-fast 512px tile; Reading uses 768px for fine text resolution
        val maxDim = if (task == AiTask.READING) 768f else 512f
        val longestSide = maxOf(bitmap.width, bitmap.height)
        if (longestSide <= maxDim) return bitmap
        val scale = maxDim / longestSide
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1),
            (bitmap.height * scale).toInt().coerceAtLeast(1),
            true
        )
    }

    private val SYSTEM_PROMPT_NAV = """
        You are DRISHTI, a real-time visual safety assistant for a walking blind person.
        RULES:
        1. Keep responses strictly under 2 short sentences.
        2. Immediate hazards (stairs, obstacles, holes, moving objects, drop-offs) must be reported FIRST with clock position (e.g. "Step down 2 feet ahead", "Chair at 11 o'clock").
        3. Give 1 direct actionable guidance (e.g. "Walk straight", "Veer slightly right").
        4. Never guess or say an unconfirmed area is safe.
    """.trimIndent()

    private val SYSTEM_PROMPT_READING = """
        You are DRISHTI OCR reader. Transcribe all visible text from the image accurately in natural reading order.
        Preserve numbers, labels, signs, and headings. If text is unreadable or blurry, say 'Text is blurry. Please hold camera closer.'
    """.trimIndent()

    private val SYSTEM_PROMPT_ASSISTANT = """
        You are DRISHTI, an intelligent conversational assistant for the visually impaired.
        Provide calm, concise, and helpful answers to the user's questions about their environment or general queries.
    """.trimIndent()
}
