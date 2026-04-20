package com.finorix.signals.domain.repository

import com.finorix.signals.domain.model.Result
import com.finorix.signals.domain.model.Signal
import kotlinx.coroutines.flow.Flow

interface SignalRepository {
    fun saveSignal(signal: Signal): Flow<Result<String>>
    fun getUserSignals(limit: Int = 50): Flow<List<Signal>>
    fun updateSignalOutcome(signalId: String, outcome: String): Flow<Result<Unit>>
    fun getWinRate(daysBack: Int = 7): Flow<Float>
    fun observeLatestSignals(): Flow<List<Signal>>
}
