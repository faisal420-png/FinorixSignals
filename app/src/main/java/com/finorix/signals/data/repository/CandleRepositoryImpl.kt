package com.finorix.signals.data.repository

import com.finorix.signals.data.remote.api.BinanceApi
import com.finorix.signals.data.remote.api.YahooFinanceApi
import com.finorix.signals.domain.model.Candle
import com.finorix.signals.domain.model.Result
import com.finorix.signals.domain.repository.CandleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CandleRepositoryImpl @Inject constructor(
    private val binanceApi: BinanceApi,
    private val yahooApi: YahooFinanceApi
) : CandleRepository {

    override fun getCandles(symbol: String, interval: String): Flow<Result<List<Candle>>> = flow {
        while (true) {
            emit(Result.Loading)
            try {
                val isCrypto = symbol.endsWith("USDT", ignoreCase = true) || symbol.endsWith("BTC", ignoreCase = true)
                val candles = if (isCrypto) {
                    fetchBinanceCandles(symbol, interval)
                } else {
                    fetchYahooCandles(symbol, interval)
                }
                emit(Result.Success(candles))
            } catch (e: Exception) {
                emit(Result.Error(e.localizedMessage ?: "An unexpected error occurred"))
            }
            delay(10000L) // Poll every 10 seconds
        }
    }

    private suspend fun fetchBinanceCandles(symbol: String, interval: String): List<Candle> {
        val cleanSymbol = symbol.replace("/", "").uppercase()
        val response = binanceApi.getKlines(symbol = cleanSymbol, interval = interval)
        return response.mapNotNull { item ->
            try {
                Candle(
                    openTime = (item[0] as Number).toLong(),
                    open = item[1].toString().toDouble(),
                    high = item[2].toString().toDouble(),
                    low = item[3].toString().toDouble(),
                    close = item[4].toString().toDouble(),
                    volume = item[5].toString().toDouble()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    private suspend fun fetchYahooCandles(symbol: String, interval: String): List<Candle> {
        val cleanSymbol = when (symbol.uppercase()) {
            "USD/JPY" -> "JPY=X"
            else -> symbol.replace("/", "").uppercase() + "=X"
        }

        val response = yahooApi.getChart(symbol = cleanSymbol, interval = interval)
        val result = response.chart?.result?.firstOrNull() ?: return emptyList()
        
        val timestamps = result.timestamp ?: return emptyList()
        val quote = result.indicators?.quote?.firstOrNull() ?: return emptyList()
        
        val candles = mutableListOf<Candle>()
        for (i in timestamps.indices) {
            val open = quote.open?.getOrNull(i)
            val high = quote.high?.getOrNull(i)
            val low = quote.low?.getOrNull(i)
            val close = quote.close?.getOrNull(i)
            val volume = quote.volume?.getOrNull(i)
            
            if (open != null && high != null && low != null && close != null && volume != null) {
                candles.add(
                    Candle(
                        openTime = timestamps[i] * 1000L, // Yahoo returns seconds, convert to ms
                        open = open,
                        high = high,
                        low = low,
                        close = close,
                        volume = volume
                    )
                )
            }
        }
        return candles
    }
}
