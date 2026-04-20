package com.finorix.signals.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.finorix.signals.domain.model.SignalDirection
import com.finorix.signals.domain.repository.CandleRepository
import com.finorix.signals.domain.repository.SignalRepository
import com.finorix.signals.data.local.dao.SignalDao
import com.finorix.signals.data.local.entity.SignalOutcome
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SignalOutcomeWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val signalRepository: SignalRepository,
    private val candleRepository: CandleRepository,
    private val signalDao: SignalDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val pendingSignals = signalRepository.getUserSignals(100).first()
                .filter { it.outcome == "PENDING" }
                
            val now = System.currentTimeMillis()
            
            pendingSignals.forEach { signal ->
                val expiresAt = signal.timestamp + (signal.expiresIn * 1000)
                if (expiresAt < now) {
                    val candleResult = candleRepository.getCandles(signal.pair, signal.timeframe).first()
                    if (candleResult is com.finorix.signals.domain.model.Result.Success) {
                        val candles = candleResult.data
                        // The signal was at signal.timestamp. It expires at expiresAt.
                        // We check the candle that formed/closed around expiresAt.
                        val closingCandle = candles.find { it.timestamp >= expiresAt } ?: candles.last()
                        
                        val isWin = if (signal.direction == SignalDirection.UP) {
                            closingCandle.close > closingCandle.open
                        } else {
                            closingCandle.close < closingCandle.open
                        }
                        
                        val outcomeStr = if (isWin) "WIN" else "LOSS"
                        val outcomeEnum = if (isWin) SignalOutcome.WIN else SignalOutcome.LOSS
                        
                        // 1. Update Firestore
                        signalRepository.updateSignalOutcome(signal.id, outcomeStr).first()
                        
                        // 2. Update Local Room
                        signalDao.updateOutcomeByTimestamp(signal.timestamp, outcomeEnum)
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
