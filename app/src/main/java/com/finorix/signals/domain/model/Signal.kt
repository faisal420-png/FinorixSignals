package com.finorix.signals.domain.model

enum class SignalDirection {
    UP, DOWN, NEUTRAL
}

data class Signal(
    val id: String = "",
    val pair: String,
    val direction: SignalDirection,
    val confidence: Int,
    val timeframe: String = "1m",
    val expiresIn: Int,
    val indicators: Map<String, String>,
    val timestamp: Long,
    val outcome: String = "PENDING"
)
