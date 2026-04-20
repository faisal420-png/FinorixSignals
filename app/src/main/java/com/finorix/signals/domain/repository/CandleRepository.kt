package com.finorix.signals.domain.repository

import com.finorix.signals.domain.model.Candle
import com.finorix.signals.domain.model.Result
import kotlinx.coroutines.flow.Flow

interface CandleRepository {
    fun getCandles(symbol: String, interval: String): Flow<Result<List<Candle>>>
}
