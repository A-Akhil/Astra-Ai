package com.example.aisecretary.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aisecretary.SecretaryApplication
import com.example.aisecretary.ai.llm.LlamaClient
import com.example.aisecretary.ai.memory.MemoryManager
import com.example.aisecretary.ai.memory.DeviceControlType
import com.example.aisecretary.ai.device.DeviceVoiceProcessor
import com.example.aisecretary.ai.device.DevicePermissionManager
import com.example.aisecretary.ai.device.DeviceStatusMonitor
import com.example.aisecretary.ai.device.DeviceCommandResult
import com.example.aisecretary.data.local.database.AppDatabase
import com.example.aisecretary.data.model.Message
import com.example.aisecretary.data.repository.ChatRepository
import com.example.aisecretary.data.repository.VoiceRepository
import com.example.aisecretary.ai.voice.SpeechState
import com.example.aisecretary.ai.voice.TtsState
import com.example.aisecretary.di.AppModule
import com.example.aisecretary.settings.SettingsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Queue
import java.util.LinkedList

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase = (application as SecretaryApplication).database
    private val voiceRepository = VoiceRepository(application)

    // Initialize LLM components
    private val retrofit = AppModule.provideRetrofit()
    private val llamaClient = LlamaClient(retrofit)
    private val memoryManager = MemoryManager(database.memoryFactDao())
    private val chatRepository = ChatRepository(
        database.messageDao(),
        llamaClient,
        memoryManager
    )

    // Settings manager
    private val settingsManager = SettingsManager(getApplication())
    
    // Device control components
    private val deviceVoiceProcessor = AppModule.provideDeviceVoiceProcessor(getApplication())
    private val devicePermissionManager = AppModule.provideDevicePermissionManager(getApplication())
    private val deviceStatusMonitor = DeviceStatusMonitor(getApplication())

    // Messages in the chat
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // Current user input
    private val _currentInput = MutableStateFlow("")
    val currentInput: StateFlow<String> = _currentInput.asStateFlow()

    // UI state
    private val _uiState = MutableStateFlow<UiState>(UiState.Ready)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Speech events
    private val _speechEvents = MutableSharedFlow<SpeechEvent>()
    val speechEvents: SharedFlow<SpeechEvent> = _speechEvents.asSharedFlow()
    
    // Device control events
    private val _deviceControlEvents = MutableSharedFlow<DeviceControlEvent>()
    val deviceControlEvents: SharedFlow<DeviceControlEvent> = _deviceControlEvents.asSharedFlow()

    // Message queue for TTS
    private val messageQueue: Queue<Message> = LinkedList()
    private var isSpeaking = false
    private var isFirstRequest = true
    private var isBackgroundListening = false
    private var wakeWordDetectionJob: Job? = null

    // Wake word
    private val WAKE_WORD = "hey astra"

    init {
        observeSpeechRecognition()
        loadMessages()
        observeSettings()
        observeTTS()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            chatRepository.getAllMessages().collectLatest { messagesList ->
                _messages.value = messagesList
            }
        }
    }

    private fun observeSpeechRecognition() {
        viewModelScope.launch {
            voiceRepository.speechState.collectLatest { state ->
                when (state) {
                    is SpeechState.Result -> {
                        val text = state.text
                        _currentInput.value = text

                        if (isBackgroundListening) {
                            // Check for wake word
                            if (text.lowercase().contains(WAKE_WORD)) {
                                isBackgroundListening = false
                                _speechEvents.emit(SpeechEvent.WakeWordDetected)
                                _currentInput.value = ""
                            } else {
                                // Continue background listening
                                startBackgroundListening()
                            }
                        } else {
                            _uiState.value = UiState.Ready
                            // Auto-send when speech recognition ends with a result
                            if (text.isNotEmpty()) {
                                _speechEvents.emit(SpeechEvent.SpeechEnded)
                            }
                        }
                    }
                    is SpeechState.PartialResult -> {
                        if (!isBackgroundListening) {
                            _currentInput.value = state.text
                        }
                    }
                    is SpeechState.Error -> {
                        if (isBackgroundListening) {
                            // Restart background listening on error
                            startBackgroundListening()
                        } else {
                            _uiState.value = UiState.Error(state.message)
                        }
                    }
                    is SpeechState.Listening -> {
                        if (isBackgroundListening) {
                            _uiState.value = UiState.BackgroundListening
                        } else {
                            _uiState.value = UiState.Listening
                        }
                    }
                    is SpeechState.Processing -> {
                        if (!isBackgroundListening) {
                            _uiState.value = UiState.Processing(isInitialRequest = false)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun observeTTS() {
        viewModelScope.launch {
            voiceRepository.ttsState.collectLatest { ttsState ->
                when(ttsState) {
                    is TtsState.Speaking -> {
                        isSpeaking = true
                        _uiState.value = UiState.Speaking
                    }
                    is TtsState.Ready -> {
                        if (isSpeaking) {
                            // Only emit speaking completed if we were speaking before
                            isSpeaking = false
                            _uiState.value = UiState.Ready
                            _speechEvents.emit(SpeechEvent.SpeakingCompleted)
                            processNextMessageInQueue()
                        }
                    }
                    is TtsState.Error -> {
                        isSpeaking = false
                        _uiState.value = UiState.Error(ttsState.message)
                        _speechEvents.emit(SpeechEvent.SpeakingCompleted)
                        processNextMessageInQueue()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun processNextMessageInQueue() {
        if (messageQueue.isNotEmpty() && !isSpeaking) {
            val nextMessage = messageQueue.poll()
            if (nextMessage != null) {
                voiceRepository.speak(nextMessage.content)
            }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsManager.memoryEnabled.collectLatest { enabled: Boolean ->
                // Update memory usage if needed
            }
        }

        viewModelScope.launch {
            settingsManager.voiceOutputEnabled.collectLatest { enabled: Boolean ->
                // Update voice output behavior if needed
            }
        }
    }

    fun onInputChanged(input: String) {
        _currentInput.value = input
    }

    fun startVoiceInput() {
        stopBackgroundListening()
        isBackgroundListening = false
        voiceRepository.startListening()
    }

    fun startBackgroundListening() {
        // Only start background listening if wake word is enabled in settings
        if (settingsManager.isWakeWordEnabled()) {
            isBackgroundListening = true
            wakeWordDetectionJob?.cancel()
            wakeWordDetectionJob = viewModelScope.launch {
                // A short delay to avoid rapid re-triggering
                delay(500)
                voiceRepository.startListening()
            }
            _uiState.value = UiState.BackgroundListening
        } else {
            // If wake word is disabled, just go to ready state
            isBackgroundListening = false
            _uiState.value = UiState.Ready
        }
    }

    fun stopBackgroundListening() {
        isBackgroundListening = false
        wakeWordDetectionJob?.cancel()
        wakeWordDetectionJob = null
    }

    fun stopVoiceInput() {
        voiceRepository.stopListening()
    }

    fun stopSpeaking() {
        voiceRepository.stopSpeaking()
        messageQueue.clear()
        isSpeaking = false
        _uiState.value = UiState.Ready
    }

    fun readMessage(message: Message) {
        if (!message.isFromUser && settingsManager.isVoiceOutputEnabled()) {
            if (isSpeaking) {
                // Add to queue if already speaking
                messageQueue.add(message)
            } else {
                // Start speaking immediately if not already speaking
                voiceRepository.speak(message.content)
            }
        }
    }

    fun sendMessage() {
        val inputText = currentInput.value.trim()
        if (inputText.isNotEmpty()) {
            // Create a user message
            val userMessage = Message(
                content = inputText,
                isFromUser = true
            )

            viewModelScope.launch {
                chatRepository.saveMessage(userMessage)
                _currentInput.value = ""
                
                // Check if this is a device control command first
                if (memoryManager.isDeviceControlCommand(inputText)) {
                    processDeviceControlCommand(inputText)
                } else {
                    // Process as regular chat message
                    processChatMessage(inputText)
                }
            }
        }
    }
    
    private suspend fun processDeviceControlCommand(inputText: String) {
        // Check permissions first
        if (!devicePermissionManager.hasAllRequiredPermissions()) {
            val missingPermissions = devicePermissionManager.getMissingPermissions()
            val permissionMessage = Message(
                content = "Device control requires additional permissions. Please grant the necessary permissions in settings.",
                isFromUser = false
            )
            chatRepository.saveMessage(permissionMessage)
            _deviceControlEvents.emit(DeviceControlEvent.PermissionRequired(missingPermissions))
            _uiState.value = UiState.Ready
            return
        }
        
        // Process device control command
        val result = deviceVoiceProcessor.processVoiceCommand(inputText)
        
        when (result) {
            is DeviceCommandResult.Success -> {
                val successMessage = Message(
                    content = result.message,
                    isFromUser = false
                )
                chatRepository.saveMessage(successMessage)
                _deviceControlEvents.emit(DeviceControlEvent.CommandExecuted(result.message))
            }
            is DeviceCommandResult.Error -> {
                val errorMessage = Message(
                    content = "Device control error: ${result.message}",
                    isFromUser = false
                )
                chatRepository.saveMessage(errorMessage)
                _deviceControlEvents.emit(DeviceControlEvent.CommandError(result.message))
            }
            is DeviceCommandResult.RequiresUserAction -> {
                val actionMessage = Message(
                    content = result.message,
                    isFromUser = false
                )
                chatRepository.saveMessage(actionMessage)
                _deviceControlEvents.emit(DeviceControlEvent.UserActionRequired(result.message, result.intent))
            }
            is DeviceCommandResult.NoMatch -> {
                // Fall back to regular chat processing
                processChatMessage(inputText)
            }
        }
        
        _uiState.value = UiState.Ready
    }
    
    private suspend fun processChatMessage(inputText: String) {
        // Check if this is the first request
        if (isFirstRequest) {
            _uiState.value = UiState.Processing(isInitialRequest = true)
            _speechEvents.emit(SpeechEvent.InitialRequestStarted)
        } else {
            _uiState.value = UiState.Processing(isInitialRequest = false)
        }

        chatRepository.processUserMessage(inputText).fold(
            onSuccess = { response ->
                val assistantMessage = Message(
                    content = response,
                    isFromUser = false
                )

                chatRepository.saveMessage(assistantMessage)

                if (isFirstRequest) {
                    isFirstRequest = false
                    _speechEvents.emit(SpeechEvent.InitialRequestCompleted)
                }

                // Emit event for new message received
                _speechEvents.emit(SpeechEvent.NewMessageReceived(assistantMessage))
                
                _uiState.value = UiState.Ready
            },
            onFailure = { error ->
                if (isFirstRequest) {
                    isFirstRequest = false
                    _speechEvents.emit(SpeechEvent.InitialRequestCompleted)
                }
                
                _uiState.value = UiState.Error("Error: ${error.message}")

                val errorMessage = Message(
                    content = "Sorry, I encountered a problem. Please try again.",
                    isFromUser = false
                )
                chatRepository.saveMessage(errorMessage)
                
                // Still try to read the error message
                _speechEvents.emit(SpeechEvent.NewMessageReceived(errorMessage))
            }
        )
    }
    
    fun clearConversation() {
        viewModelScope.launch {
            chatRepository.clearAllMessages()
            stopSpeaking()
            // Messages will be cleared automatically through the Flow
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceRepository.cleanup()
        stopSpeaking()
        stopBackgroundListening()
        
        // Unload the model when the app is exiting
        viewModelScope.launch {
            llamaClient.unloadModel().onFailure { error ->
                // Just log the error, don't need to show to user at app exit
                android.util.Log.e("ChatViewModel", "Failed to unload model: ${error.message}")
            }
        }
    }

    // Methods to check current status
    fun isBackgroundListeningActive(): Boolean {
        return isBackgroundListening
    }
    
    fun isSpeakingOrProcessing(): Boolean {
        return isSpeaking || _uiState.value is UiState.Processing || _uiState.value is UiState.Speaking
    }
    
    /**
     * Unloads the model from memory
     */
    fun unloadModel() {
        viewModelScope.launch {
            llamaClient.unloadModel().onFailure { error ->
                android.util.Log.e("ChatViewModel", "Failed to unload model: ${error.message}")
            }
        }
    }
    
    /**
     * Get device status report
     */
    fun getDeviceStatusReport(): String {
        return deviceStatusMonitor.getDeviceStatusReport()
    }
    
    /**
     * Get device status for a specific type
     */
    fun getDeviceStatus(deviceType: DeviceControlType): String {
        return deviceStatusMonitor.getStatusDescription(deviceType)
    }
    
    /**
     * Get optimal settings recommendations
     */
    fun getOptimalSettingsRecommendations(): List<String> {
        return deviceStatusMonitor.getOptimalSettingsRecommendations()
    }
    
    /**
     * Refresh device status
     */
    fun refreshDeviceStatus() {
        deviceStatusMonitor.refreshAllStatus()
    }
    
    /**
     * Check if device control is available
     */
    fun isDeviceControlAvailable(): Boolean {
        return devicePermissionManager.hasAllRequiredPermissions()
    }
    
    /**
     * Get missing permissions
     */
    fun getMissingPermissions(): List<com.example.aisecretary.ai.device.PermissionInfo> {
        return devicePermissionManager.getMissingPermissions()
    }
}

sealed class UiState {
    object Ready : UiState()
    object Listening : UiState()
    data class Processing(val isInitialRequest: Boolean = false) : UiState()
    data class Error(val message: String) : UiState()
    object Speaking : UiState()
    object BackgroundListening : UiState()
}

sealed class SpeechEvent {
    object SpeechEnded : SpeechEvent()
    data class NewMessageReceived(val message: Message) : SpeechEvent()
    object InitialRequestStarted : SpeechEvent()
    object InitialRequestCompleted : SpeechEvent()
    object SpeakingCompleted : SpeechEvent()
    object WakeWordDetected : SpeechEvent()
}

sealed class DeviceControlEvent {
    data class PermissionRequired(val missingPermissions: List<com.example.aisecretary.ai.device.PermissionInfo>) : DeviceControlEvent()
    data class CommandExecuted(val message: String) : DeviceControlEvent()
    data class CommandError(val error: String) : DeviceControlEvent()
    data class UserActionRequired(val message: String, val intent: android.content.Intent) : DeviceControlEvent()
}
