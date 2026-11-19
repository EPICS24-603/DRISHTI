package com.example.assistantapp

/** Sends a spoken question and optional recent scene description to Gemini. */
suspend fun askDrishti(message: String, frameData: String? = null): String {
    val context = frameData?.takeIf { it.isNotBlank() }
        ?.let { "Recent camera description: $it\n\n" }
        .orEmpty()
    return GeminiClient.ask("${context}User's question: $message", maxOutputTokens = 140)
}
