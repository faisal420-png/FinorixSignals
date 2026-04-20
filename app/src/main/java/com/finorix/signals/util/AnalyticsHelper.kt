package com.finorix.signals.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun logSignalGenerated(pair: String, direction: String, confidence: Int, timeframe: String) {
        val bundle = Bundle().apply {
            putString("pair", pair)
            putString("direction", direction)
            putInt("confidence", confidence)
            putString("timeframe", timeframe)
        }
        firebaseAnalytics.logEvent("signal_generated", bundle)
    }

    fun logSignalOutcome(pair: String, outcome: String) {
        val bundle = Bundle().apply {
            putString("pair", pair)
            putString("outcome", outcome)
        }
        firebaseAnalytics.logEvent("signal_outcome", bundle)
    }

    fun logAiExplanationRequested(model: String) {
        val bundle = Bundle().apply {
            putString("model", model)
        }
        firebaseAnalytics.logEvent("ai_explanation_requested", bundle)
    }

    fun logPairSelected(pair: String) {
        val bundle = Bundle().apply {
            putString("pair", pair)
        }
        firebaseAnalytics.logEvent("pair_selected", bundle)
    }

    fun logTimeframeChanged(timeframe: String) {
        val bundle = Bundle().apply {
            putString("timeframe", timeframe)
        }
        firebaseAnalytics.logEvent("timeframe_changed", bundle)
    }

    fun logSettingsUpdated(field: String) {
        val bundle = Bundle().apply {
            putString("field", field)
        }
        firebaseAnalytics.logEvent("settings_updated", bundle)
    }

    fun logLogin(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    fun logSignup(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
    }

    fun setAnalyticsEnabled(enabled: Boolean) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }
}
