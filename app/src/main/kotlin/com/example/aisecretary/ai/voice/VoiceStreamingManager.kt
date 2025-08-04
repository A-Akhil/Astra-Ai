package com.example.aisecretary.ai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.aisecretary.data.model.VoiceStreamingState
import com.example.aisecretary.data.local.preferences.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Manages voice synthesis for streaming responses with sentence-by-sentence playback
 */
class VoiceStreamingManager(
    private val context: Context,
    private val settingsManager: SettingsManager,
    private val coroutineScope: CoroutineScope
) {
    
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private val utteranceIdGenerator = AtomicInteger(0)
    
    private val _voiceState = MutableStateFlow(VoiceStreamingState())
    val voiceState: StateFlow<VoiceStreamingState> = _voiceState.asStateFlow()
    
    private val sentenceQueue = mutableListOf<String>()
    private var currentUtteranceId: String? = null
    private var isSpeaking = false
    private var streamingComplete = false
    
    // Callbacks
    var onSpeechComplete: (() -> Unit)? = null
    var onSpeechError: ((String) -> Unit)? = null
    var onAllSpeechComplete: (() -> Unit)? = null

    init {
        initializeTextToSpeech()
    }

    private fun initializeTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.getDefault()
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        updateVoiceState { it.copy(isSpeaking = true) }
                    }

                    override fun onDone(utteranceId: String?) {
                        coroutineScope.launch {
                            handleUtteranceComplete(utteranceId)
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        updateVoiceState { it.copy(isSpeaking = false) }
                        onSpeechError?.invoke("TTS error for utterance: $utteranceId")
                    }

                    override fun onStop(utteranceId: String?, interrupted: Boolean) {
                        updateVoiceState { it.copy(isSpeaking = false) }
                        if (interrupted) {
                            clearQueue()
                        }
                    }
                })
                isInitialized = true
            } else {
                onSpeechError?.invoke("Failed to initialize TextToSpeech")
            }
        }
    }

    /**
     * Add a sentence to the speech queue
     */
    fun addSentenceToQueue(sentence: String) {
        if (!settingsManager.isVoiceOutputEnabled()) return
        
        synchronized(sentenceQueue) {
            sentenceQueue.add(sentence.trim())
            updateVoiceState { state ->
                state.copy(speechQueue = sentenceQueue.toList())
            }
        }
        
        // Start speaking if not already speaking
        if (!isSpeaking && isInitialized) {
            processNextSentence()
        }
    }

    /**
     * Mark streaming as complete
     */
    fun markStreamingComplete() {
        streamingComplete = true
        // Process any remaining sentences
        if (!isSpeaking && sentenceQueue.isNotEmpty()) {
            processNextSentence()
        }
    }

    /**
     * Process the next sentence in the queue
     */
    private fun processNextSentence() {
        synchronized(sentenceQueue) {
            if (sentenceQueue.isEmpty()) {
                if (streamingComplete) {
                    handleAllSpeechComplete()
                }
                return
            }
            
            val sentence = sentenceQueue.removeAt(0)
            currentUtteranceId = "utterance_${utteranceIdGenerator.incrementAndGet()}"
            
            updateVoiceState { state ->
                state.copy(
                    speechQueue = sentenceQueue.toList(),
                    currentSentenceIndex = state.currentSentenceIndex + 1
                )
            }
            
            textToSpeech?.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, currentUtteranceId)
            isSpeaking = true
        }
    }

    /**
     * Handle completion of current utterance
     */
    private fun handleUtteranceComplete(utteranceId: String?) {
        if (utteranceId == currentUtteranceId) {
            isSpeaking = false
            onSpeechComplete?.invoke()
            
            // Check if there are more sentences to speak
            if (sentenceQueue.isNotEmpty()) {
                processNextSentence()
            } else if (streamingComplete) {
                handleAllSpeechComplete()
            }
        }
    }

    /**
     * Handle completion of all speech
     */
    private fun handleAllSpeechComplete() {
        updateVoiceState { state ->
            state.copy(
                isSpeaking = false,
                speechQueue = emptyList(),
                shouldAutoActivateMic = settingsManager.isAutoActivateMicEnabled(),
                micActivationPending = settingsManager.isAutoActivateMicEnabled()
            )
        }
        
        onAllSpeechComplete?.invoke()
        
        // Reset state for next conversation turn
        streamingComplete = false
    }

    /**
     * Stop speaking and clear queue
     */
    fun stopSpeaking() {
        textToSpeech?.stop()
        clearQueue()
    }

    /**
     * Clear the speech queue
     */
    fun clearQueue() {
        synchronized(sentenceQueue) {
            sentenceQueue.clear()
            updateVoiceState { state ->
                state.copy(
                    isSpeaking = false,
                    speechQueue = emptyList(),
                    micActivationPending = false
                )
            }
        }
        isSpeaking = false
        streamingComplete = false
    }

    /**
     * Check if currently speaking
     */
    fun isSpeaking(): Boolean = isSpeaking || (textToSpeech?.isSpeaking == true)

    /**
     * Check if there are sentences queued for speech
     */
    fun hasQueuedSentences(): Boolean = sentenceQueue.isNotEmpty()

    /**
     * Get the current speech queue size
     */
    fun getQueueSize(): Int = sentenceQueue.size

    /**
     * Interrupt current speech and streaming
     */
    fun interruptAll() {
        stopSpeaking()
        clearQueue()
        streamingComplete = false
    }

    /**
     * Mark microphone as activated (called when user starts speaking)
     */
    fun markMicrophoneActivated() {
        updateVoiceState { state ->
            state.copy(
                micActivationPending = false,
                shouldAutoActivateMic = false
            )
        }
    }

    /**
     * Update voice state
     */
    private fun updateVoiceState(update: (VoiceStreamingState) -> VoiceStreamingState) {
        _voiceState.value = update(_voiceState.value)
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        clearQueue()
    }
}
