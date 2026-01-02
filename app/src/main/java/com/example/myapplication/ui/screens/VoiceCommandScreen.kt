package com.example.myapplication.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.accessibility.AccessibilityManager
import com.example.myapplication.viewmodel.VoiceViewModelFactory
import com.example.myapplication.model.SOSStatus
import com.example.myapplication.viewmodel.VoiceViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Voice Command Screen for user speech input.
 * 
 * Features:
 * - Microphone button with pulse animation
 * - Real-time speech recognition feedback
 * - Command recognition and execution
 * - Support for: "open scan", "send sos", "show alerts", "back home", "send incident"
 * 
 * File Location: app/src/main/java/com/example/myapplication/ui/screens/VoiceCommandScreen.kt
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VoiceCommandScreen(
    accessibilityManager: AccessibilityManager? = null,
    onNavigateBack: () -> Unit,
    onNavigateToScan: () -> Unit,
    onTriggerSOS: (SOSStatus) -> Unit,
    onShowAlerts: () -> Unit,
    onNavigateToHome: () -> Unit,
    onReportIncident: (String) -> Unit
) {
    val context = LocalContext.current
    
    // Create ViewModel with factory
    val viewModel: VoiceViewModel = viewModel(
        factory = VoiceViewModelFactory(context, accessibilityManager)
    )

    // Permission handling for RECORD_AUDIO
    val micPermissionState = rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO
    )

    // Collect state from ViewModel
    val listeningState by viewModel.listeningState.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val commandResult by viewModel.commandResult.collectAsState()
    val lastError by viewModel.lastError.collectAsState()

    // Set up navigation callbacks
    LaunchedEffect(Unit) {
        viewModel.onNavigateToScan = onNavigateToScan
        viewModel.onTriggerSOS = onTriggerSOS
        viewModel.onShowAlerts = onShowAlerts
        viewModel.onNavigateToHome = onNavigateToHome
        viewModel.onReportIncident = onReportIncident
    }

    // Handle command results
    LaunchedEffect(commandResult) {
        when (val result = commandResult) {
            is VoiceViewModel.CommandResult.Success -> {
                // Command was recognized and action triggered
                // Navigation will be handled by callbacks
            }
            is VoiceViewModel.CommandResult.Error -> {
                accessibilityManager?.speak(result.message)
            }
            is VoiceViewModel.CommandResult.NoMatch -> {
                accessibilityManager?.speak("Command not recognized. Try again.")
            }
            null -> { /* Initial state */ }
        }
    }

    // Animation for microphone pulse
    val isListening = listeningState is VoiceViewModel.ListeningState.Listening
    val pulseScale by animateFloatAsState(
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = tween(500),
        label = "pulse"
    )
    
    val micColor by animateColorAsState(
        targetValue = if (isListening) Color.Red else MaterialTheme.colorScheme.primary,
        animationSpec = tween(300),
        label = "micColor"
    )

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopListening()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Command") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopListening()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ============================================================
            // STATUS INDICATOR
            // ============================================================
            
            StatusIndicator(
                state = listeningState,
                error = lastError
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ============================================================
            // MICROPHONE BUTTON
            // ============================================================

            // Pulse effect
            if (isListening) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.3f))
                )
            }
            
            // Main mic button
            IconButton(
                onClick = {
                    when {
                        // Permission not granted - request it
                        !micPermissionState.status.isGranted -> {
                            micPermissionState.launchPermissionRequest()
                        }
                        // Already listening - stop
                        isListening -> {
                            viewModel.stopListening()
                        }
                        // Start listening
                        else -> {
                            viewModel.resetState()
                            viewModel.startListening()
                        }
                    }
                },
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(micColor)
            ) {
                Icon(
                    imageVector = when {
                        !micPermissionState.status.isGranted -> Icons.Default.MicOff
                        isListening -> Icons.Default.MicOff
                        else -> Icons.Default.Mic
                    },
                    contentDescription = when {
                        !micPermissionState.status.isGranted -> "Grant microphone permission"
                        isListening -> "Stop listening"
                        else -> "Start listening"
                    },
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status text
            Text(
                text = when {
                    !micPermissionState.status.isGranted -> "Tap to grant microphone permission"
                    isListening -> "Listening... Say a command"
                    listeningState is VoiceViewModel.ListeningState.Processing -> "Processing..."
                    else -> "Tap to speak"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = when (listeningState) {
                    is VoiceViewModel.ListeningState.Listening -> Color.Red
                    is VoiceViewModel.ListeningState.Error -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ============================================================
            // RECOGNIZED TEXT DISPLAY
            // ============================================================

            if (recognizedText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Heard:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"$recognizedText\"",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ============================================================
            // PERMISSION RATIONALE
            // ============================================================

            if (!micPermissionState.status.isGranted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Microphone Permission Required",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (micPermissionState.status.shouldShowRationale) {
                                "Microphone permission is needed to listen to your voice commands. Please grant the permission to use voice commands."
                            } else {
                                "Please grant microphone permission to enable voice commands."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ============================================================
            // SUPPORTED COMMANDS
            // ============================================================

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Supported Commands",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    CommandItem("open scan", "Navigate to scan screen")
                    CommandItem("send sos", "Trigger emergency SOS")
                    CommandItem("show alerts", "View active alerts")
                    CommandItem("back home", "Return to home screen")
                    CommandItem("send incident [description]", "Report an incident with voice")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ============================================================
            // SPEECH NOT AVAILABLE WARNING
            // ============================================================

            if (!viewModel.isSpeechRecognizerAvailable()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Speech recognition is not available on this device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

/**
 * Displays the current listening status and any errors
 */
@Composable
private fun StatusIndicator(
    state: VoiceViewModel.ListeningState,
    error: String?
) {
    val (icon, color, text) = when (state) {
        is VoiceViewModel.ListeningState.Idle -> Triple(
            Icons.Default.Mic,
            MaterialTheme.colorScheme.primary,
            "Ready for voice command"
        )
        is VoiceViewModel.ListeningState.Listening -> Triple(
            Icons.Default.RecordVoiceOver,
            Color.Red,
            "Listening..."
        )
        is VoiceViewModel.ListeningState.Processing -> Triple(
            Icons.Default.HourglassEmpty,
            MaterialTheme.colorScheme.secondary,
            "Processing speech..."
        )
        is VoiceViewModel.ListeningState.Error -> Triple(
            Icons.Default.Error,
            MaterialTheme.colorScheme.error,
            error ?: "An error occurred"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

/**
 * Displays a single command with its description
 */
@Composable
private fun CommandItem(command: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "\"$command\"",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

