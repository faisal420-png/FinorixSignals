package com.finorix.signals.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val soundEnabled: Flow<Boolean>
    val vibrationEnabled: Flow<Boolean>
    val minConfidence: Flow<Int>
    val defaultTimeframe: Flow<String>
    val openRouterApiKey: Flow<String>
    val twelveDataApiKey: Flow<String>
    val selectedModel: Flow<String>
    val notificationConfidence: Flow<Int>
    val enabledPairs: Flow<Set<String>>
    val analyticsEnabled: Flow<Boolean>

    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setVibrationEnabled(enabled: Boolean)
    suspend fun setMinConfidence(confidence: Int)
    suspend fun setDefaultTimeframe(timeframe: String)
    suspend fun setOpenRouterApiKey(key: String)
    suspend fun setTwelveDataApiKey(key: String)
    suspend fun setSelectedModel(model: String)
    suspend fun setNotificationConfidence(confidence: Int)
    suspend fun setEnabledPairs(pairs: Set<String>)
    suspend fun setAnalyticsEnabled(enabled: Boolean)

    fun getPreferences(): Flow<UserPreferences>
    suspend fun updateUserPreferences(prefs: UserPreferences)
}

data class UserPreferences(
    val minConfidence: Int = 80,
    val defaultTimeframe: String = "1m",
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val enabledPairs: List<String> = emptyList(),
    val aiModel: String = "deepseek/deepseek-chat:free",
    val analyticsEnabled: Boolean = true
)
