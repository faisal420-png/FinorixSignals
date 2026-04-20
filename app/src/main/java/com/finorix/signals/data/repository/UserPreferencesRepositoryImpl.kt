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

    override val soundEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.SOUND_ENABLED] ?: true }
    override val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.VIBRATION_ENABLED] ?: true }
    override val minConfidence: Flow<Int> = context.dataStore.data.map { it[Keys.MIN_CONFIDENCE] ?: 65 }
    override val defaultTimeframe: Flow<String> = context.dataStore.data.map { it[Keys.DEFAULT_TIMEFRAME] ?: "1m" }
    override val openRouterApiKey: Flow<String> = context.dataStore.data.map { it[Keys.OPENROUTER_API_KEY] ?: "" }
    override val twelveDataApiKey: Flow<String> = context.dataStore.data.map { it[Keys.TWELVEDATA_API_KEY] ?: "" }
    override val selectedModel: Flow<String> = context.dataStore.data.map { it[Keys.SELECTED_MODEL] ?: "deepseek/deepseek-chat:free" }
    override val notificationConfidence: Flow<Int> = context.dataStore.data.map { it[Keys.NOTIFICATION_CONFIDENCE] ?: 80 }
    override val enabledPairs: Flow<Set<String>> = context.dataStore.data.map { it[Keys.ENABLED_PAIRS] ?: setOf("EUR/USD", "GBP/USD", "BTC/USDT", "ETH/USDT") }
    override val analyticsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.ANALYTICS_ENABLED] ?: true }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }
    }

    override suspend fun setMinConfidence(confidence: Int) {
        context.dataStore.edit { it[Keys.MIN_CONFIDENCE] = confidence }
    }

    override suspend fun setDefaultTimeframe(timeframe: String) {
        context.dataStore.edit { it[Keys.DEFAULT_TIMEFRAME] = timeframe }
    }

    override suspend fun setOpenRouterApiKey(key: String) {
        context.dataStore.edit { it[Keys.OPENROUTER_API_KEY] = key }
    }

    override suspend fun setTwelveDataApiKey(key: String) {
        context.dataStore.edit { it[Keys.TWELVEDATA_API_KEY] = key }
    }

    override suspend fun setSelectedModel(model: String) {
        context.dataStore.edit { it[Keys.SELECTED_MODEL] = model }
    }

    override suspend fun setNotificationConfidence(confidence: Int) {
        context.dataStore.edit { it[Keys.NOTIFICATION_CONFIDENCE] = confidence }
    }

    override suspend fun setEnabledPairs(pairs: Set<String>) {
        context.dataStore.edit { it[Keys.ENABLED_PAIRS] = pairs }
    }

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ANALYTICS_ENABLED] = enabled }
    }

    override fun getPreferences(): Flow<com.finorix.signals.domain.repository.UserPreferences> = kotlinx.coroutines.flow.combine(
        minConfidence, defaultTimeframe, soundEnabled, vibrationEnabled, enabledPairs, selectedModel, analyticsEnabled
    ) { args: Array<Any?> ->
        val minC = args[0] as Int
        val defT = args[1] as String
        val sound = args[2] as Boolean
        val vib = args[3] as Boolean
        val pairs = args[4] as Set<String>
        val model = args[5] as String
        val analytics = args[6] as Boolean
        com.finorix.signals.domain.repository.UserPreferences(minC, defT, sound, vib, pairs.toList(), model, analytics)
    }

    override suspend fun updateUserPreferences(prefs: com.finorix.signals.domain.repository.UserPreferences) {
        context.dataStore.edit {
            it[Keys.MIN_CONFIDENCE] = prefs.minConfidence
            it[Keys.DEFAULT_TIMEFRAME] = prefs.defaultTimeframe
            it[Keys.SOUND_ENABLED] = prefs.soundEnabled
            it[Keys.VIBRATION_ENABLED] = prefs.vibrationEnabled
            it[Keys.ENABLED_PAIRS] = prefs.enabledPairs.toSet()
            it[Keys.SELECTED_MODEL] = prefs.aiModel
            it[Keys.ANALYTICS_ENABLED] = prefs.analyticsEnabled
        }
        syncToFirestore(prefs)
    }

    private suspend fun syncToFirestore(prefs: com.finorix.signals.domain.repository.UserPreferences) {
        val uid = auth.currentUser?.uid ?: return
        try {
            db.collection("users").document(uid)
                .collection("settings").document("preferences")
                .set(prefs)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
