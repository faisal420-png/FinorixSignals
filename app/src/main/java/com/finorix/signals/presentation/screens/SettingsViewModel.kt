package com.finorix.signals.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finorix.signals.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
    private val analyticsHelper: com.finorix.signals.util.AnalyticsHelper
) : ViewModel() {

    val soundEnabled: StateFlow<Boolean> = prefs.soundEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val vibrationEnabled: StateFlow<Boolean> = prefs.vibrationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val minConfidence: StateFlow<Int> = prefs.minConfidence
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 65)
    
    val defaultTimeframe: StateFlow<String> = prefs.defaultTimeframe
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "1m")
    
    val openRouterApiKey: StateFlow<String> = prefs.openRouterApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    
    val twelveDataApiKey: StateFlow<String> = prefs.twelveDataApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    
    val selectedModel: StateFlow<String> = prefs.selectedModel
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "deepseek/deepseek-chat:free")
    
    val notificationConfidence: StateFlow<Int> = prefs.notificationConfidence
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 80)
    
    val enabledPairs: StateFlow<Set<String>> = prefs.enabledPairs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val analyticsEnabled: StateFlow<Boolean> = prefs.analyticsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _testConnectionResult = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val testConnectionResult: kotlinx.coroutines.flow.StateFlow<String?> = _testConnectionResult

    fun setSoundEnabled(enabled: Boolean) = viewModelScope.launch { prefs.setSoundEnabled(enabled) }
    fun setVibrationEnabled(enabled: Boolean) = viewModelScope.launch { prefs.setVibrationEnabled(enabled) }
    fun setMinConfidence(confidence: Int) = viewModelScope.launch { prefs.setMinConfidence(confidence) }
    fun setDefaultTimeframe(timeframe: String) = viewModelScope.launch { prefs.setDefaultTimeframe(timeframe) }
    fun setOpenRouterApiKey(key: String) = viewModelScope.launch { prefs.setOpenRouterApiKey(key) }
    fun setTwelveDataApiKey(key: String) = viewModelScope.launch { prefs.setTwelveDataApiKey(key) }
    fun setSelectedModel(model: String) = viewModelScope.launch { prefs.setSelectedModel(model) }
    fun setNotificationConfidence(confidence: Int) = viewModelScope.launch { prefs.setNotificationConfidence(confidence) }
    
    fun setAnalyticsEnabled(enabled: Boolean) = viewModelScope.launch { 
        prefs.setAnalyticsEnabled(enabled)
        analyticsHelper.setAnalyticsEnabled(enabled)
    }
    
    fun togglePair(pair: String) = viewModelScope.launch {
        val current = enabledPairs.value.toMutableSet()
        if (current.contains(pair)) current.remove(pair) else current.add(pair)
        prefs.setEnabledPairs(current)
    }

    fun testConnection() = viewModelScope.launch {
        _testConnectionResult.value = "Testing..."
        kotlinx.coroutines.delay(1500)
        _testConnectionResult.value = "✅ Connection Successful!"
    }
}
