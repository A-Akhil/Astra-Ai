package com.example.aisecretary.ai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * A helper class that manages Text-to-Speech (TTS) functionality using Android's [TextToSpeech] API.
 *
 * @param context The application context used to initialize the TTS engine.
 */
class TextToSpeechManager(
    context: Context
) {
    private var textToSpeech: TextToSpeech? = null
    private val _ttsState = MutableStateFlow<TtsState>(TtsState.Idle)

    /**
     * Public read-only state flow that represents the current state of the TTS engine.
     */
    val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    _ttsState.value = TtsState.Error("Language not supported")
                } else {
                    _ttsState.value = TtsState.Ready
                    setupTtsListener()
                }
            } else {
                _ttsState.value = TtsState.Error("Initialization failed")
            }
        }
    }

    /**
     * Sets up a listener to track the progress of speech output.
     */
    private fun setupTtsListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _ttsState.value = TtsState.Speaking
            }

            override fun onDone(utteranceId: String?) {
                _ttsState.value = TtsState.Ready
            }

            /**
             * Called when an error occurs while speaking.
             */
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _ttsState.value = TtsState.Error("Error during speech")
            }

            /**
             * Called with an error code when an error occurs.
             *
             * @param errorCode Error code returned by the TTS engine.
             */
            override fun onError(utteranceId: String?, errorCode: Int) {
                super.onError(utteranceId, errorCode)
                _ttsState.value = TtsState.Error("Error code: $errorCode")
            }
        })
    }

    /**
     * Speaks the provided [text] using the TTS engine.
     *
     * @param text The text to be converted to speech.
     */
    fun speak(text: String) {
        if (_ttsState.value is TtsState.Ready) {
            val utteranceId = UUID.randomUUID().toString()
            textToSpeech?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId
            )
        }
    }

    /**
     * Stops the current speech immediately.
     */
    fun stop() {
        textToSpeech?.stop()
        _ttsState.value = TtsState.Ready
    }

    /**
     * Shuts down the TTS engine and releases resources.
     */
    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}

/**
 * Represents the current state of the Text-to-Speech engine.
 */
sealed class TtsState {

    /** TTS is not yet initialized. */
    object Idle : TtsState()

    /** TTS is ready and idle. */
    object Ready : TtsState()

    /** TTS is currently speaking. */
    object Speaking : TtsState()

    /**
     * TTS encountered an error.
     *
     * @property message A description of the error.
     */
    data class Error(val message: String) : TtsState()
}