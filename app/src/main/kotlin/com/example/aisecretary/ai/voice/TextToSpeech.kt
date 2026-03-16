package com.example.aisecretary.ai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * Enhanced Text-to-Speech manager with queue support and streaming integration
 */
class TextToSpeechManager(
    context: Context
) {
    private var textToSpeech: TextToSpeech? = null
    private val _ttsState = MutableStateFlow<TtsState>(TtsState.Idle)
    val ttsState: StateFlow<TtsState> = _ttsState.asStateFlow()
    
    private var utteranceCounter = 0
    private val utteranceQueue = mutableListOf<String>()

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

    private fun setupTtsListener() {
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _ttsState.value = TtsState.Speaking
                Log.d("TTS", "Started speaking: $utteranceId")
            }

            override fun onDone(utteranceId: String?) {
                _ttsState.value = TtsState.Ready
                Log.d("TTS", "Finished speaking: $utteranceId")
                
                // Process next item in queue if any
                processQueue()
            }

            override fun onError(utteranceId: String?) {
                _ttsState.value = TtsState.Error("Error during speech")
                Log.e("TTS", "TTS Error for utterance: $utteranceId")
            }

            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                _ttsState.value = TtsState.Ready
                Log.d("TTS", "TTS stopped, interrupted: $interrupted")
            }
        })
    }

    enum class QueueMode {
        FLUSH,    // Clear queue and speak immediately
        QUEUE_ADD // Add to queue
    }

    fun speak(text: String, queueMode: QueueMode = QueueMode.FLUSH) {
        if (text.isBlank()) return
        
        val utteranceId = "utterance_${++utteranceCounter}"
        
        when (queueMode) {
            QueueMode.FLUSH -> {
                // Clear queue and speak immediately
                utteranceQueue.clear()
                stop()
                speakImmediately(text, utteranceId)
            }
            QueueMode.QUEUE_ADD -> {
                // Add to queue for sequential playback
                if (_ttsState.value == TtsState.Speaking) {
                    utteranceQueue.add(text)
                } else {
                    speakImmediately(text, utteranceId)
                }
            }
        }
    }

    private fun speakImmediately(text: String, utteranceId: String) {
        try {
            val result = textToSpeech?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                utteranceId
            )
            
            if (result == TextToSpeech.ERROR) {
                _ttsState.value = TtsState.Error("Failed to start speech")
                Log.e("TTS", "Failed to speak text: $text")
            }
        } catch (e: Exception) {
            _ttsState.value = TtsState.Error("Exception during speech: ${e.message}")
            Log.e("TTS", "Exception during TTS", e)
        }
    }

    private fun processQueue() {
        if (utteranceQueue.isNotEmpty()) {
            val nextText = utteranceQueue.removeFirst()
            val utteranceId = "utterance_${++utteranceCounter}"
            speakImmediately(nextText, utteranceId)
        }
    }

    fun stop() {
        try {
            utteranceQueue.clear()
            textToSpeech?.stop()
            _ttsState.value = TtsState.Ready
        } catch (e: Exception) {
            Log.e("TTS", "Error stopping TTS", e)
        }
    }

    fun pause() {
        try {
            textToSpeech?.stop()
            _ttsState.value = TtsState.Paused
        } catch (e: Exception) {
            Log.e("TTS", "Error pausing TTS", e)
        }
    }

    fun setSpeechRate(rate: Float) {
        textToSpeech?.setSpeechRate(rate.coerceIn(0.1f, 3.0f))
    }

    fun setPitch(pitch: Float) {
        textToSpeech?.setPitch(pitch.coerceIn(0.1f, 2.0f))
    }

    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking == true
    }

    fun shutdown() {
        stop()
        textToSpeech?.shutdown()
    }

    sealed class TtsState {
        object Idle : TtsState()
        object Ready : TtsState()
        object Speaking : TtsState()
        object Paused : TtsState()
        data class Error(val message: String) : TtsState()
    }
}
