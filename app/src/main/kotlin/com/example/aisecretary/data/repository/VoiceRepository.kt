package com.example.aisecretary.data.repository

import android.content.Context
import com.example.aisecretary.ai.voice.SpeechRecognizerManager
import com.example.aisecretary.ai.voice.SpeechState
import com.example.aisecretary.ai.voice.TextToSpeechManager
import com.example.aisecretary.ai.voice.TtsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * This class handles all voice-related features like:
 * - Listening to the user's speech
 * - Speaking text using text-to-speech
 *
 * @param context The app context, needed to initialize speech services.
 */
class VoiceRepository(private val context: Context) {

    // Managers for speech recognition and text-to-speech
    private val speechRecognizerManager = SpeechRecognizerManager(context)
    private val textToSpeechManager = TextToSpeechManager(context)

    /**
     * Current state of speech recognition (e.g., listening, error, etc.).
     */
    val speechState: StateFlow<SpeechState> = speechRecognizerManager.speechState

    /**
     * Current state of text-to-speech (e.g., speaking, idle, etc.).
     */
    val ttsState: StateFlow<TtsState> = textToSpeechManager.ttsState

    // Initialize speech recognition when the repository is created
    init {
        speechRecognizerManager.initialize()
    }

    /**
     * Starts listening to the user's voice.
     */
    fun startListening() {
        speechRecognizerManager.startListening()
    }

    /**
     * Stops listening to the user's voice.
     */
    fun stopListening() {
        speechRecognizerManager.stopListening()
    }

    /**
     * Speaks the given text using the text-to-speech engine.
     *
     * @param text The text to speak out loud.
     */
    fun speak(text: String) {
        textToSpeechManager.speak(text)
    }

    /**
     * Stops any ongoing text-to-speech playback.
     */
    fun stopSpeaking() {
        textToSpeechManager.stop()
    }

    /**
     * Cleans up and shuts down both speech and TTS components.
     * Call this when the feature is no longer needed.
     */
    fun cleanup() {
        speechRecognizerManager.destroy()
        textToSpeechManager.shutdown()
    }
}