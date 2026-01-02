package com.example.myapplication.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.accessibility.AccessibilityManager
import com.example.myapplication.model.SOSStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * VoiceViewModel - Manages voice command state and processing
 * 
 * This ViewModel handles:
 * - Listening state management (IDLE, LISTENING, PROCESSING, ERROR)
 * - Speech recognition using Android's native SpeechRecognizer
 * - Command matching and execution
 * - Navigation and action callbacks
 * 
 * Supported Commands:
 * - "open scan" → Navigate to Scan screen
 * - "send sos" → Trigger SOS flow
 * - "show alerts" → Open Alerts screen
 * - "back home" → Navigate to Home
 * - "send incident [description]" → Report incident with voice description
 */
class VoiceViewModel(
    private val context: Context,
    private val accessibilityManager: AccessibilityManager? = null
) : ViewModel() {

    // ============================================================
    // LISTENING STATES
    // ============================================================
    
    /**
     * Represents the current state of voice listening
     */
    sealed class ListeningState {
        data object Idle : ListeningState()
        data object Listening : ListeningState()
        data object Processing : ListeningState()
        data class Error(val message: String) : ListeningState()
    }

    /**
     * Represents the result of command processing
     */
    sealed class CommandResult {
        data class Success(val command: String, val action: VoiceAction) : CommandResult()
        data class Error(val message: String) : CommandResult()
        data object NoMatch : CommandResult()
    }

    /**
     * Actions that can be triggered by voice commands
     */
    sealed class VoiceAction {
        data object NavigateToScan : VoiceAction()
        data object TriggerSOS : VoiceAction()
        data object ShowAlerts : VoiceAction()
        data object NavigateToHome : VoiceAction()
        data class ReportIncident(val description: String) : VoiceAction()
        data class Unknown(val text: String) : VoiceAction()
    }

    // ============================================================
    // STATE FLOWS
    // ============================================================

    private val _listeningState = MutableStateFlow<ListeningState>(ListeningState.Idle)
    val listeningState: StateFlow<ListeningState> = _listeningState.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _commandResult = MutableStateFlow<CommandResult?>(null)
    val commandResult: StateFlow<CommandResult?> = _commandResult.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    // Retry counter for auto-restart (prevents infinite loops)
    private var retryCount = 0
    private val maxRetries = 3
    private var isAutoRestartEnabled = true

    // ============================================================
    // SPEECH RECOGNIZER
    // ============================================================

    private val speechRecognizer: SpeechRecognizer? = try {
        SpeechRecognizer.createSpeechRecognizer(context)
    } catch (e: Exception) {
        Log.e("VoiceViewModel", "SpeechRecognizer not available: ${e.message}")
        null
    }

    // Navigation callbacks (set by the UI)
    var onNavigateToScan: (() -> Unit)? = null
    var onTriggerSOS: ((SOSStatus) -> Unit)? = null
    var onShowAlerts: (() -> Unit)? = null
    var onNavigateToHome: (() -> Unit)? = null
    var onReportIncident: ((String) -> Unit)? = null

    // ============================================================
    // COMMAND PATTERNS
    // ============================================================

    /**
     * Command patterns mapped to their normalized form
     * Uses flexible matching to handle various phrasings
     */
    private val commandPatterns = mapOf(
        // Scan command variations
        "open scan" to listOf(
            "open scan", "start scan", "begin scan", "scan area", 
            "scan surroundings", "look around", "check area"
        ),
        // SOS command variations
        "send sos" to listOf(
            "send sos", "sos", "emergency", "help me", "i need help",
            "call emergency", "send emergency", "trigger sos"
        ),
        // Alerts command variations
        "show alerts" to listOf(
            "show alerts", "open alerts", "view alerts", "alerts",
            "check alerts", "see alerts"
        ),
        // Home command variations
        "back home" to listOf(
            "back home", "go home", "return home", "home", 
            "main screen", "main menu"
        ),
        // Incident command variations
        "send incident" to listOf(
            "send incident", "report incident", "report emergency",
            "file incident", "create incident", "log incident"
        )
    )

    // ============================================================
    // INITIALIZATION
    // ============================================================

    init {
        setupSpeechRecognizer()
    }

    /**
     * Sets up the speech recognizer with aRecognitionListener
     */
    private fun setupSpeechRecognizer() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("VoiceViewModel", "Ready for speech")
                _listeningState.value = ListeningState.Listening
                accessibilityManager?.speak("Listening. Say a command.")
            }

            override fun onBeginningOfSpeech() {
                // User started speaking
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Audio level changed - could be used for visualization
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Audio buffer received
            }

            override fun onEndOfSpeech() {
                Log.d("VoiceViewModel", "End of speech")
                _listeningState.value = ListeningState.Processing
            }

            override fun onError(error: Int) {
                Log.e("VoiceViewModel", "Speech error: $error")
                _listeningState.value = ListeningState.Idle
                
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_NETWORK, 
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                        "No internet connection. Please check your network."
                    }
                    SpeechRecognizer.ERROR_NO_MATCH -> {
                        "Could not understand. Please try again."
                    }
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        "No speech detected. Listening again..."
                    }
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                        "Recognition service busy. Please wait."
                    }
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        "Microphone permission not granted."
                    }
                    else -> {
                        "Speech recognition error. Please try again."
                    }
                }
                
                _lastError.value = errorMessage
                
                // Auto-restart for recoverable errors (timeout, no match)
                val shouldAutoRestart = error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || 
                                       error == SpeechRecognizer.ERROR_NO_MATCH
                
                if (shouldAutoRestart && isAutoRestartEnabled && retryCount < maxRetries) {
                    retryCount++
                    Log.d("VoiceViewModel", "Auto-restarting listening (attempt $retryCount of $maxRetries)")
                    // Announce and restart after short delay
                    accessibilityManager?.speak(errorMessage)
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(1500) // Wait 1.5 seconds before restart
                        if (_listeningState.value == ListeningState.Idle) {
                            restartListening()
                        }
                    }
                } else if (retryCount >= maxRetries) {
                    // Max retries reached, require manual restart
                    Log.w("VoiceViewModel", "Max retries reached, stopping auto-restart")
                    accessibilityManager?.speak("Too many attempts. Tap the microphone to try again.")
                    _lastError.value = "Tap microphone to try again"
                } else {
                    // Non-recoverable error or auto-restart disabled
                    accessibilityManager?.speak(errorMessage)
                }
            }

            override fun onResults(results: Bundle?) {
                Log.d("VoiceViewModel", "Speech results received")
                _listeningState.value = ListeningState.Idle
                
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val rawText = matches[0]
                    _recognizedText.value = rawText
                    retryCount = 0 // Reset retry count on successful recognition
                    processCommand(rawText)
                } else {
                    _commandResult.value = CommandResult.NoMatch
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Partial results - can be used for real-time feedback
                val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!partial.isNullOrEmpty()) {
                    _recognizedText.value = partial[0]
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Reserved for future events
            }
        })
    }

    // ============================================================
    // COMMAND PROCESSING
    // ============================================================

    /**
     * Process the recognized speech and execute appropriate action
     */
    private fun processCommand(rawText: String) {
        val normalizedText = rawText.lowercase().trim()
        Log.d("VoiceViewModel", "Processing command: '$normalizedText'")

        // Check for incident command first (needs special handling for description)
        if (isIncidentCommand(normalizedText)) {
            val description = extractIncidentDescription(normalizedText)
            if (description != null) {
                executeAction(VoiceAction.ReportIncident(description))
            } else {
                accessibilityManager?.speak("Please describe the incident after saying 'send incident'")
                _commandResult.value = CommandResult.Error("Missing incident description")
            }
            return
        }

        // Check other commands
        for ((command, patterns) in commandPatterns) {
            if (patterns.any { normalizedText.contains(it) }) {
                val action = when (command) {
                    "open scan" -> VoiceAction.NavigateToScan
                    "send sos" -> VoiceAction.TriggerSOS
                    "show alerts" -> VoiceAction.ShowAlerts
                    "back home" -> VoiceAction.NavigateToHome
                    else -> VoiceAction.Unknown(normalizedText)
                }
                executeAction(action)
                return
            }
        }

        // No match found
        accessibilityManager?.speak("Unknown command: $rawText")
        _commandResult.value = CommandResult.NoMatch
        executeAction(VoiceAction.Unknown(normalizedText))
    }

    /**
     * Check if the text contains an incident command
     */
    private fun isIncidentCommand(text: String): Boolean {
        val incidentPatterns = commandPatterns["send incident"] ?: return false
        return incidentPatterns.any { text.contains(it) }
    }

    /**
     * Extract the incident description from the voice command
     * Assumes format: "send incident [description]"
     */
    private fun extractIncidentDescription(text: String): String? {
        val incidentPatterns = commandPatterns["send incident"] ?: return null
        
        for (pattern in incidentPatterns) {
            if (text.contains(pattern)) {
                // Extract everything after the command phrase
                val description = text.substringAfter(pattern, "")
                    .replace("about", "")
                    .replace("with", "")
                    .replace("saying", "")
                    .trim()
                
                return description.ifEmpty { null }
            }
        }
        
        // Try generic extraction - everything after "incident"
        val afterIncident = text.substringAfter("incident", "")
            .replace("about", "")
            .replace("with", "")
            .replace("saying", "")
            .trim()
        
        return afterIncident.ifEmpty { null }
    }

    /**
     * Execute the voice action
     */
    private fun executeAction(action: VoiceAction) {
        when (action) {
            is VoiceAction.NavigateToScan -> {
                accessibilityManager?.speak("Opening scan screen")
                _commandResult.value = CommandResult.Success("open scan", action)
                onNavigateToScan?.invoke()
            }
            is VoiceAction.TriggerSOS -> {
                accessibilityManager?.speak("Sending SOS. Please select your status.")
                _commandResult.value = CommandResult.Success("send sos", action)
                // Trigger SOS with default status - UI will prompt for confirmation
                onTriggerSOS?.invoke(SOSStatus.NEED_HELP)
            }
            is VoiceAction.ShowAlerts -> {
                accessibilityManager?.speak("Showing alerts")
                _commandResult.value = CommandResult.Success("show alerts", action)
                onShowAlerts?.invoke()
            }
            is VoiceAction.NavigateToHome -> {
                accessibilityManager?.speak("Going back to home")
                _commandResult.value = CommandResult.Success("back home", action)
                onNavigateToHome?.invoke()
            }
            is VoiceAction.ReportIncident -> {
                accessibilityManager?.speak("Reporting incident: ${action.description}")
                _commandResult.value = CommandResult.Success("send incident", action)
                onReportIncident?.invoke(action.description)
            }
            is VoiceAction.Unknown -> {
                _commandResult.value = CommandResult.Error("Unknown command: ${action.text}")
            }
        }
    }

    // ============================================================
    // PUBLIC METHODS
    // ============================================================

    /**
     * Start listening for voice commands
     * 
     * @param languageCode Language code (e.g., "en-US", "es-ES")
     */
    fun startListening(languageCode: String = "en-US") {
        if (_listeningState.value == ListeningState.Listening) {
            Log.d("VoiceViewModel", "Already listening")
            return
        }

        if (speechRecognizer == null) {
            _lastError.value = "Speech recognition not available on this device"
            _listeningState.value = ListeningState.Error("Speech recognition not available")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true) // Prefer offline for faster response
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            
            // ============ FIX FOR ERROR 13: Speech Timeout ============
            // Longer speech input time to prevent timeout errors
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L) // 5 seconds of silence before ending
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L) // 3 seconds for possibly complete
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000L) // Minimum 3 seconds of speech
        }

        try {
            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            Log.e("VoiceViewModel", "Error starting listening: ${e.message}")
            _lastError.value = "Failed to start voice recognition: ${e.message}"
            _listeningState.value = ListeningState.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Restart listening after an error (used for auto-recovery)
     * Resets state and starts a new listening session
     */
    private fun restartListening() {
        Log.d("VoiceViewModel", "Restarting listening session")
        resetState()
        startListening()
    }

    /**
     * Enable or disable auto-restart feature
     */
    fun setAutoRestartEnabled(enabled: Boolean) {
        isAutoRestartEnabled = enabled
    }

    /**
     * Get current retry count (for debugging/testing)
     */
    fun getRetryCount(): Int = retryCount

    /**
     * Stop listening for voice commands
     */
    fun stopListening() {
        if (_listeningState.value == ListeningState.Listening) {
            speechRecognizer?.stopListening()
            _listeningState.value = ListeningState.Idle
        }
    }

    /**
     * Reset the voice command state
     */
    fun resetState() {
        _recognizedText.value = ""
        _commandResult.value = null
        _lastError.value = null
        _listeningState.value = ListeningState.Idle
        retryCount = 0 // Reset retry count on manual reset
    }

    /**
     * Check if speech recognizer is available on this device
     */
    fun isSpeechRecognizerAvailable(): Boolean {
        return speechRecognizer != null
    }

    /**
     * Get list of supported commands
     */
    fun getSupportedCommands(): List<String> {
        return commandPatterns.keys.toList()
    }

    // ============================================================
    // CLEANUP
    // ============================================================

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        Log.d("VoiceViewModel", "ViewModel cleared - SpeechRecognizer destroyed")
    }
}

/**
 * Factory for creating VoiceViewModel with dependencies
 */
class VoiceViewModelFactory(
    private val context: Context,
    private val accessibilityManager: AccessibilityManager? = null
) : androidx.lifecycle.ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VoiceViewModel::class.java)) {
            return VoiceViewModel(context, accessibilityManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

