package com.finorix.signals.data.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.finorix.signals.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: com.google.firebase.auth.FirebaseAuth,
    private val db: com.google.firebase.firestore.FirebaseFirestore
) : UserPreferencesRepository {

    private object Keys {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val MIN_CONFIDENCE = intPreferencesKey("min_confidence")
        val DEFAULT_TIMEFRAME = stringPreferencesKey("default_timeframe")
        val OPENROUTER_API_KEY = stringPreferencesKey("openrouter_api_key")
        val TWELVEDATA_API_KEY = stringPreferencesKey("twelvedata_api_key")
        val SELECTED_MODEL = stringPreferencesKey("selected_model")
        val NOTIFICATION_CONFIDENCE = intPreferencesKey("notification_confidence")
        val ENABLED_PAIRS = stringSetPreferencesKey("enabled_pairs")
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
    }

    override val soundEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.SOUND_ENABLED] ?: true
    }

    override val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.VIBRATION_ENABLED] ?: true
    }

    override val minConfidence: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[Keys.MIN_CONFIDENCE] ?: 70
    }

    override val defaultTimeframe: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.DEFAULT_TIMEFRAME] ?: "1h"
    }

    override val openRouterApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.OPENROUTER_API_KEY] ?: ""
    }

    override val twelveDataApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.TWELVEDATA_API_KEY] ?: ""
    }

    override val selectedModel: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[Keys.SELECTED_MODEL] ?: "google/gemini-pro-1.5-exp"
    }

    override val notificationConfidence: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[Keys.NOTIFICATION_CONFIDENCE] ?: 80
    }

    override val enabledPairs: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[Keys.ENABLED_PAIRS] ?: setOf("EUR/USD", "BTC/USD", "XAU/USD")
    }

    override val analyticsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[Keys.ANALYTICS_ENABLED] ?: true
    }

    override suspend fun updateSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SOUND_ENABLED] = enabled
        }
    }

    override suspend fun updateVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.VIBRATION_ENABLED] = enabled
        }
    }

    override suspend fun updateMinConfidence(confidence: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.MIN_CONFIDENCE] = confidence
        }
    }

    override suspend fun updateDefaultTimeframe(timeframe: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.DEFAULT_TIMEFRAME] = timeframe
        }
    }

    override suspend fun updateOpenRouterApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.OPENROUTER_API_KEY] = apiKey
        }
    }

    override suspend fun updateTwelveDataApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.TWELVEDATA_API_KEY] = apiKey
        }
    }

    override suspend fun updateSelectedModel(model: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SELECTED_MODEL] = model
        }
    }

    override suspend fun updateNotificationConfidence(confidence: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.NOTIFICATION_CONFIDENCE] = confidence
        }
    }

    override suspend fun updateEnabledPairs(pairs: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ENABLED_PAIRS] = pairs
        }
    }

    override suspend fun updateAnalyticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ANALYTICS_ENABLED] = enabled
        }
    }

    override suspend fun syncWithCloud() {
        val user = auth.currentUser ?: return
        try {
            val doc = db.collection("users").document(user.uid).get().await()
            if (doc.exists()) {
                context.dataStore.edit { prefs ->
                    doc.getBoolean("sound_enabled")?.let { prefs[Keys.SOUND_ENABLED] = it }
                    doc.getBoolean("vibration_enabled")?.let { prefs[Keys.VIBRATION_ENABLED] = it }
                    doc.getLong("min_confidence")?.let { prefs[Keys.MIN_CONFIDENCE] = it.toInt() }
                    doc.getString("default_timeframe")?.let { prefs[Keys.DEFAULT_TIMEFRAME] = it }
                    doc.getString("openrouter_api_key")?.let { prefs[Keys.OPENROUTER_API_KEY] = it }
                    doc.getString("twelvedata_api_key")?.let { prefs[Keys.TWELVEDATA_API_KEY] = it }
                    doc.getString("selected_model")?.let { prefs[Keys.SELECTED_MODEL] = it }
                    doc.getLong("notification_confidence")?.let { prefs[Keys.NOTIFICATION_CONFIDENCE] = it.toInt() }
                    (doc.get("enabled_pairs") as? List<*>)?.let { list ->
                        prefs[Keys.ENABLED_PAIRS] = list.filterIsInstance<String>().toSet()
                    }
                    doc.getBoolean("analytics_enabled")?.let { prefs[Keys.ANALYTICS_ENABLED] = it }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
