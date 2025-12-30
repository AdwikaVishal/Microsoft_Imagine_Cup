package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Incident
import com.example.myapplication.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class IncidentViewModel(private val apiService: ApiService) : ViewModel() {

    private val _incidents = MutableStateFlow<List<Incident>>(emptyList())
    val incidents: StateFlow<List<Incident>> = _incidents

    fun fetchIncidents() {
        viewModelScope.launch {
            try {
                _incidents.value = apiService.getMyIncidents()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
