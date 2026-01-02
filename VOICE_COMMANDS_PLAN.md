# Voice Commands Implementation Plan

## Summary
Implement voice commands using Android's native SpeechRecognizer (not Azure) for the SenseSafe Android app.

## Files to Create

### 1. `/app/src/main/java/com/example/myapplication/viewmodel/VoiceViewModel.kt`
**Purpose:** MVVM ViewModel for managing voice command state and processing

**Key Responsibilities:**
- Manage listening state (IDLE, LISTENING, PROCESSING, ERROR)
- Store recognized text
- Match voice commands to actions
- Handle navigation callbacks
- Trigger SOS, incident reporting, and screen navigation

---

### 2. `/app/src/main/java/com/example/myapplication/utils/VoiceCommandManager.kt` (UPDATE)
**Purpose:** Enhance existing manager with command processing

**New Features:**
- Command matching for all supported commands
- Error handling for network, no match, cancelled
- Command callbacks for navigation

---

### 3. `/app/src/main/java/com/example/myapplication/ui/screens/VoiceCommandScreen.kt` (UPDATE)
**Purpose:** Update existing screen with permission handling and command UI

**Changes:**
- Add permission handling with Accompanist
- Show listening status with animations
- Display recognized text
- Handle command feedback

---

## Files to Update

### 4. `/app/src/main/java/com/example/myapplication/ui/MainAppNavGraph.kt`
**Purpose:** Pass navigation callbacks to VoiceCommandScreen

**Changes:**
- Pass navigation lambdas to VoiceCommandScreen
- Add navigation to Alerts screen (create if needed)

---

### 5. `/app/src/main/java/com/example/myapplication/MainActivity.kt`
**Purpose:** Initialize ViewModels for navigation

**Changes:**
- Add VoiceViewModel initialization

---

## AndroidManifest.xml Changes

**Already present ✅:**
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

No additional changes needed.

---

## Voice Commands Supported

| Command | Action |
|---------|--------|
| "open scan" | Navigate to Camera/Scan screen |
| "send sos" | Trigger SOS flow |
| "show alerts" | Open Alerts screen |
| "back home" | Navigate to Home |
| "send incident [description]" | Report incident with voice description |

---

## Implementation Steps

### Step 1: Create VoiceViewModel.kt
- StateFlow for listening status, recognized text, errors
- Command matching logic
- Navigation callbacks

### Step 2: Update VoiceCommandManager.kt
- Add command recognition patterns
- Improve error handling
- Add callbacks for command execution

### Step 3: Update VoiceCommandScreen.kt
- Add permission handling
- Integrate with VoiceViewModel
- Update UI for command feedback

### Step 4: Update MainAppNavGraph.kt
- Pass navigation lambdas
- Handle alerts navigation

### Step 5: Update AndroidManifest.xml
- ✅ Already configured with RECORD_AUDIO

---

## Dependencies

**Already in build.gradle.kts ✅:**
- `kotlinx.coroutines`
- `accompanist-permissions`
- `navigation-compose`
- `androidx.lifecycle.viewmodel.compose`

No new dependencies needed.

---

## Testing

1. Test microphone permission flow
2. Test each voice command
3. Verify error handling (no network, no match)
4. Verify navigation works correctly

