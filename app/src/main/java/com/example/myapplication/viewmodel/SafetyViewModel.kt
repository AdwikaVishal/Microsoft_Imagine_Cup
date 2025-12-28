package com.example.myapplication.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.myapplication.accessibility.AccessibilityManager
import com.example.myapplication.alerts.AlertManager
import com.example.myapplication.data.AbilityProfile
import com.example.myapplication.data.DisasterPayload
import com.example.myapplication.data.RetrofitClient
import com.example.myapplication.data.UserRepository
import com.example.myapplication.data.UserStatus
import com.example.myapplication.utils.DeviceUtils
import com.example.myapplication.utils.LocationHelper
import com.example.myapplication.utils.OfflineManager
import com.example.myapplication.workers.RescueWorker
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Shared ViewModel for Onboarding, Alerts, and Rescue.
 * Manages ability profiles, disaster status, and rescue signals.
 */
class SafetyViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository = UserRepository(application)
    private val locationHelper = LocationHelper(application)
    
    // UI State for Ability Profile
    private val _abilityProfile = MutableStateFlow(userRepository.getAbilityProfile())
    val abilityProfile: StateFlow<AbilityProfile> = _abilityProfile

    // UI State for Disaster Alert
    private val _isDisasterActive = MutableStateFlow(false)
    val isDisasterActive: StateFlow<Boolean> = _isDisasterActive
    
    // Last Known Location
    val lastKnownLocation = MutableStateFlow<Location?>(null)

    // Offline Status
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline

    // Save profile
    fun setAbilityProfile(profile: AbilityProfile) {
        userRepository.saveAbilityProfile(profile)
        _abilityProfile.value = profile
    }

    // Trigger fake alert for testing
    fun triggerFakeAlert() {
        _isDisasterActive.value = true
        // In a real scenario, this would be triggered by a push notification
        AlertManager.showDisasterAlert(getApplication())
    }

    // Send rescue signal
    fun sendRescueSignal(status: UserStatus) {
        viewModelScope.launch {
            val location = locationHelper.getCurrentLocation()
            lastKnownLocation.value = location
            val battery = DeviceUtils.getBatteryLevel(getApplication())
            
            val payload = DisasterPayload(
                userId = userRepository.getUserId(),
                ability = _abilityProfile.value.name.lowercase(),
                status = status.name.lowercase(),
                lat = location?.latitude ?: 0.0,
                lng = location?.longitude ?: 0.0,
                battery = battery
            )
            
            val isOnline = OfflineManager.isOnline(getApplication())
            _isOffline.value = !isOnline

            if (isOnline) {
                // 1. Try sending immediately via Retrofit
                launch {
                    try {
                        val response = RetrofitClient.instance.sendAlert(payload)
                        if (!response.isSuccessful) {
                            enqueueRescueWorker(payload)
                        }
                    } catch (e: Exception) {
                        enqueueRescueWorker(payload)
                    }
                }
            } else {
                // OFFLINE-FIRST: Cache immediately
                enqueueRescueWorker(payload)
            }
        }
    }
    
    // Reset app state for a fresh start (Testing helper)
    fun resetApp() {
        userRepository.resetUser()
        _abilityProfile.value = AbilityProfile.NONE
        _isDisasterActive.value = false
        AlertManager.cancelAlert(getApplication())
    }

    // 1️⃣ & 5️⃣ WorkManager for retries: Enqueue worker if direct network fails or Offline
    private fun enqueueRescueWorker(payload: DisasterPayload) {
        val payloadJson = Gson().toJson(payload)
        val data = Data.Builder()
            .putString(RescueWorker.KEY_PAYLOAD, payloadJson)
            .build()

        val request = OneTimeWorkRequestBuilder<RescueWorker>()
            .setInputData(data)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(getApplication()).enqueue(request)
    }
}
