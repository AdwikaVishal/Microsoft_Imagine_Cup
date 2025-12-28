package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.myapplication.accessibility.AccessibilityManager
import com.example.myapplication.alerts.AlertManager
import com.example.myapplication.data.AbilityProfile
import com.example.myapplication.data.UserStatus
import com.example.myapplication.services.SafetyMonitoringService
import com.example.myapplication.ui.screens.CameraScreen
import com.example.myapplication.ui.screens.DisasterScreen
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.OnboardingScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.ShakeDetector
import com.example.myapplication.utils.VoiceCommandManager
import com.example.myapplication.viewmodel.SafetyViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: SafetyViewModel by viewModels()
    private lateinit var accessibilityManager: AccessibilityManager
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var voiceCommandManager: VoiceCommandManager
    private var isCameraOpen by mutableStateOf(false)

    // 7️⃣ Permissions (Ask clearly)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permissions granted/rejected
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startSafetyService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize helpers
        accessibilityManager = AccessibilityManager(this)
        AlertManager.createChannel(this)
        
        // 3️⃣ Shake Detector
        shakeDetector = ShakeDetector(this) {
            // Trigger SOS or Alert on shake
            viewModel.sendRescueSignal(UserStatus.NEED_HELP)
            // 3️⃣ While shaking phone nothing happening I should get a popup that location has been sent
            Toast.makeText(this, "SOS Sent! Location shared.", Toast.LENGTH_LONG).show()
            accessibilityManager.vibrate(AccessibilityManager.PATTERN_SOS)
        }
        
        // 1️⃣ Voice Command Manager
        voiceCommandManager = VoiceCommandManager(this)

        // Check permissions
        checkAndRequestPermissions()

        setContent {
            MyApplicationTheme {
                val abilityProfile by viewModel.abilityProfile.collectAsState()
                
                Scaffold { innerPadding ->
                    if (isCameraOpen) {
                        // Real-time camera scan
                        CameraScreen(
                            profile = abilityProfile,
                            onExitDetected = { exitText ->
                                // 1️⃣ If Im blind add that it should take voice input for user and for eveyrhting
                                // If blind, announce result
                                if (abilityProfile == AbilityProfile.BLIND) {
                                    accessibilityManager.speak("Found $exitText")
                                }
                            }
                        )
                        // Back button handling logic would go here in a full nav graph
                    } else {
                        SenseSafeApp(
                            viewModel = viewModel,
                            accessibilityManager = accessibilityManager,
                            onOpenCamera = { isCameraOpen = true },
                            voiceCommandManager = voiceCommandManager,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        shakeDetector.start()
    }

    override fun onPause() {
        super.onPause()
        shakeDetector.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        accessibilityManager.shutdown()
        voiceCommandManager.destroy()
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (toRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(toRequest.toTypedArray())
        } else {
            startSafetyService()
        }
    }

    private fun startSafetyService() {
        val intent = Intent(this, SafetyMonitoringService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent)
        } else {
            startService(intent)
        }
    }
}

@Composable
fun SenseSafeApp(
    viewModel: SafetyViewModel,
    accessibilityManager: AccessibilityManager,
    onOpenCamera: () -> Unit,
    voiceCommandManager: VoiceCommandManager,
    modifier: Modifier = Modifier
) {
    val abilityProfile by viewModel.abilityProfile.collectAsState()
    val isDisasterActive by viewModel.isDisasterActive.collectAsState()
    val context = LocalContext.current

    // React to disaster state for Accessibility
    LaunchedEffect(isDisasterActive) {
        if (isDisasterActive) {
            if (abilityProfile == AbilityProfile.BLIND) {
                accessibilityManager.speak("Emergency Alert! Disaster detected. Follow voice instructions.")
            }
            // Trigger vibration for everyone
            accessibilityManager.vibrate(AccessibilityManager.PATTERN_DANGER)
        } else {
            accessibilityManager.stopVibration()
        }
    }
    
    // 1️⃣ Blind user: Auto-listen for commands if profile is blind
    LaunchedEffect(abilityProfile) {
        if (abilityProfile == AbilityProfile.BLIND) {
             // Example: Start listening automatically or on a gesture
             // For now, let's just make sure TTS is active
             accessibilityManager.speak("Blind mode active. Shake for SOS. Tap screen to scan surroundings.")
        }
    }

    if (abilityProfile == AbilityProfile.NONE) {
        // 2️⃣ Onboarding
        OnboardingScreen(
            currentProfile = abilityProfile,
            onProfileSelected = { viewModel.setAbilityProfile(it) },
            onContinue = { /* Logic handled by state change */ }
        )
    } else {
        if (isDisasterActive) {
            // 3️⃣ Disaster Warning System
            DisasterScreen(
                profile = abilityProfile,
                onStatusUpdate = { status ->
                    // 5️⃣ Rescue Mode: Send data
                    viewModel.sendRescueSignal(status)
                    // Haptic confirmation
                    accessibilityManager.vibrate(AccessibilityManager.PATTERN_CONFIRM)
                    if (abilityProfile == AbilityProfile.BLIND) {
                        accessibilityManager.speak("Status sent: ${status.description}")
                    }
                }
            )
        } else {
            // Home Screen (Placeholder) -> 8️⃣ Testing helpers
            HomeScreen(
                onSimulateAlert = { viewModel.triggerFakeAlert() },
                onResetApp = { viewModel.resetApp() },
                onOpenCamera = onOpenCamera,
                isBlind = abilityProfile == AbilityProfile.BLIND,
                onVoiceCommand = { 
                    // 2️⃣ It should also ask for user langugae add a multilingual option 
                    // Simplified: Start listening in default locale, could show picker
                    voiceCommandManager.startListening(Locale.getDefault().toLanguageTag()) 
                }
            )
        }
    }
}
