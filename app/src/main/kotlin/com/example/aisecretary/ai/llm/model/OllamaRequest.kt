package com.example.aisecretary.ai.llm.model

import com.example.aisecretary.BuildConfig

/**
 * Represents a request sent to the Ollama AI model for generating a response.
 *
 * This data class is used to encapsulate the necessary and optional parameters
 * for querying the AI system, including the model name, input prompt, system instructions,
 * streaming preference, and generation options.
 *
 * Example usage:
 * ```
 * val request = OllamaRequest(
 *     prompt = "What are the benefits of Kotlin?",
 *     system = "You are a helpful programming assistant.",
 *     stream = false
 * )
 * ```
 *
 * @param model The name of the AI model to use.
 * @param prompt The question or message for the AI.
 * @param system (Optional) Extra instructions for how the AI should behave.
 * @param stream If true, the AI sends its reply in parts.
 * @param options (Optional) Settings to control how the AI answers.
 * @param keep_alive (Optional) Used to keep the session alive longer.
 */
data class OllamaRequest(
    val model: String = BuildConfig.LLAMA_MODEL_NAME,
    val prompt: String,
    val system: String? = null,
    val stream: Boolean = false,
    val options: OllamaOptions? = OllamaOptions(),
    val keep_alive: Any? = null
)

/**
 * Extra settings that change how the AI replies.
 * These parameters are useful when fine-tuning how deterministic or exploratory
 * the language generation should be.
 *
 * Example usage:
 * ```
 * val options = OllamaOptions(
 *     temperature = 0.9f,
 *     maxTokens = 1000,
 *     topP = 0.85f
 * )
 * ```
 * @param temperature How creative the AI should be (higher = more random).
 * @param topP Limits choices to the most likely words.
 * @param topK Picks from the top K words.
 * @param maxTokens Maximum length of the AI's answer.
 * @param presencePenalty Avoids repeating the same topics.
 * @param frequencyPenalty Avoids repeating the same words.
 */
data class OllamaOptions(
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val topK: Int = 40,
    val maxTokens: Int = 800,
    val presencePenalty: Float = 0.0f,
    val frequencyPenalty: Float = 0.0f
)