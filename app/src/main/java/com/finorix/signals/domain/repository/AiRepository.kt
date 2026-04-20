package com.finorix.signals.domain.repository

import com.finorix.signals.domain.model.Candle
import com.finorix.signals.domain.model.Signal
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    fun explainSignal(signal: Signal, candles: List<Candle>): Flow<String>
    fun chatWithAssistant(userMessage: String, marketContext: String): Flow<String>
}
