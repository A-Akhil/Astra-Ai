package com.example.aisecretary.ai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SpeechRecognizerManager handles speech-to-text operations using Android's SpeechRecognizer API.
 * It manages initialization, starting/stopping recognition, and maintaining recognition state.
 *
 * @param context The Android Context used to access system services.
 */
class SpeechRecognizerManager(private val context: Context) {

    // Android's native SpeechRecognizer instance
    private var speechRecognizer: SpeechRecognizer? = null
    // Mutable state to track current speech recognition status
    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)

    /**
     * Publicly exposed immutable state of the current speech recognition.
     */
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    /**
     * Initializes the SpeechRecognizer and sets its listener.
     * If speech recognition is not available on the device, updates state with an error.
     */

    fun initialize() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(recognitionListener)
        } else {
            _speechState.value = SpeechState.Error("Speech recognition is not available on this device")
        }
    }

    /**
     * Starts listening for voice input using Android's SpeechRecognizer.
     * Initializes the recognizer if not already initialized.
     */
    fun startListening() {
        if (speechRecognizer == null) {
            initialize()
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        _speechState.value = SpeechState.Listening
        speechRecognizer?.startListening(intent)
    }

    /**
     * Stops the speech recognition process and sets the state to Idle.
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        _speechState.value = SpeechState.Idle
    }

    /**
     * Releases the speech recognizer resources and sets it to null.
     * Should be called when the speech recognizer is no longer needed.
     */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    /**
     * Listener that handles callbacks from Android's speech recognition system.
     */
    private val recognitionListener = object : RecognitionListener {
        /**
         * Called when the system is ready to start listening.
         */
        override fun onReadyForSpeech(params: Bundle?) {
            _speechState.value = SpeechState.Listening
        }

        /**
         * Called when the user starts speaking.
         */
        override fun onBeginningOfSpeech() {
            _speechState.value = SpeechState.Listening
        }

        /**
         * Called when the sound level in the audio stream has changed.
         * Useful for visual feedback (e.g., waveform).
         */
        override fun onRmsChanged(rmsdB: Float) {
            // Optional: Update UI based on sound level
        }
        /**
         * Called with raw audio data (not used).
         */

        override fun onBufferReceived(buffer: ByteArray?) {
            // Not used in this implementation
        }

        /**
         * Called when the user stops speaking.
         */
        override fun onEndOfSpeech() {
            _speechState.value = SpeechState.Processing
        }

        /**
         * Called when an error occurs during recognition.
         *
         * @param error The error code from [SpeechRecognizer].
         */
        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Unknown error"
            }
            _speechState.value = SpeechState.Error(errorMessage)
        }

        /**
         * Called when final recognition results are ready.
         *
         * @param results A [Bundle] containing the recognized speech results.
         */
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                _speechState.value = SpeechState.Result(matches[0])
            } else {
                _speechState.value = SpeechState.Error("No speech results returned")
            }
        }

        /**
         * Called with intermediate (partial) recognition results.
         */
        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                _speechState.value = SpeechState.PartialResult(matches[0])
            }
        }

        /**
         * Called when a non-standard event occurs (not used).
         */
        override fun onEvent(eventType: Int, params: Bundle?) {
            // Not used in this implementation
        }
    }
}

/**
 * Represents the various states during the speech recognition process.
 */
sealed class SpeechState {
    /**
     * No active speech recognition.
     */
    object Idle : SpeechState()

    /**
     * The system is currently listening for voice input.
     */
    object Listening : SpeechState()

    /**
     * The input speech is being processed.
     */
    object Processing : SpeechState()

    /**
     * A partial recognition result has been received.
     *
     * @param text The partially recognized text.
     */
    data class PartialResult(val text: String) : SpeechState()

    /**
     * Final recognized text result.
     *
     * @param text The recognized speech converted to text.
     */
    data class Result(val text: String) : SpeechState()

    /**
     * An error occurred during recognition.
     *
     * @param message A human-readable error message.
     */
    data class Error(val message: String) : SpeechState()
}