package com.example.assistantapp

import android.content.Context

/** Sends a spoken question and optional recent scene description to Gemini 2.5 Flash. */
suspend fun askDrishti(
    message: String,
    frameData: String? = null,
    context: Context? = null
): String {
    val sceneContext = frameData?.takeIf { it.isNotBlank() }
        ?.let { "Recent camera description: $it\n\n" }
        .orEmpty()
    return GeminiClient.ask(
        prompt = "${sceneContext}User's question: $message",
        maxOutputTokens = 120,
        task = AiTask.ASSISTANT,
        context = context
    )
}
