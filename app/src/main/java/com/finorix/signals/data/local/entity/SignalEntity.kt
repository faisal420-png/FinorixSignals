package com.finorix.signals.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.finorix.signals.domain.model.SignalDirection

enum class SignalOutcome {
    WIN, LOSS, PENDING
}

@Entity(tableName = "signals")
data class SignalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pair: String,
    val direction: SignalDirection,
    val confidence: Int,
    val timestamp: Long,
    val expiryTimestamp: Long,
    val entryPrice: Double,
    var outcome: SignalOutcome = SignalOutcome.PENDING,
    val indicatorJson: String // Store indicators as JSON string
)
