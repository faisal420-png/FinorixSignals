package com.finorix.signals.domain.engine

import com.finorix.signals.domain.model.Candle
import com.finorix.signals.domain.model.Signal
import com.finorix.signals.domain.model.SignalDirection
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class SignalEngine {

    fun generateSignal(pair: String, candles: List<Candle>): Signal? {
        if (candles.size < 30) return null

        val votes = mutableMapOf<String, Int>()
        val details = mutableMapOf<String, String>()

        // 1. RSI (14)
        val rsiValue = calculateRSI(candles, 14)
        val rsiVote = when {
            rsiValue < 30 -> 1 // Oversold
            rsiValue > 70 -> -1 // Overbought
            else -> 0
        }
        votes["RSI"] = rsiVote
        details["RSI"] = String.format(Locale.US, "%.2f", rsiValue)

        // 2. EMA Cross (9, 21)
        val ema9 = calculateEMA(candles, 9)
        val ema21 = calculateEMA(candles, 21)
        val emaVote = if (ema9 > ema21) 1 else -1
        votes["EMA"] = emaVote
        details["EMA"] = if (emaVote > 0) "Bullish Trend" else "Bearish Trend"

        // 3. MACD (12, 26, 9)
        val macd = calculateMACD(candles)
        val macdVote = if (macd.first > macd.second) 1 else -1
        votes["MACD"] = macdVote
        details["MACD"] = if (macdVote > 0) "Momentum UP" else "Momentum DOWN"

        // 4. Bollinger Bands (20, 2)
        val bb = calculateBollingerBands(candles, 20, 2.0)
        val currentPrice = candles.last().close
        val bbVote = when {
            currentPrice <= bb.third -> 1 // Lower band
            currentPrice >= bb.first -> -1 // Upper band
            else -> 0
        }
        votes["BB"] = bbVote
        details["BB"] = when (bbVote) {
            1 -> "Bounce Lower"
            -1 -> "Reject Upper"
            else -> "Neutral"
        }

        // 5. Candlestick Patterns
        val pattern = detectPattern(candles)
        votes["Pattern"] = pattern.first
        details["Pattern"] = pattern.second

        // 6. Support / Resistance
        val sr = analyzeSR(candles)
        votes["SR"] = sr.first
        details["SR"] = sr.second

        val totalVotes = votes.values.sum()
        val totalIndicators = votes.size
        val confidence = (abs(totalVotes).toFloat() / totalIndicators * 100).toInt()

        // Minimum confidence 65% as requested
        if (confidence < 65) return null

        val direction = if (totalVotes > 0) SignalDirection.UP else SignalDirection.DOWN

        return Signal(
            pair = pair,
            direction = direction,
            confidence = confidence,
            expiresIn = 60, // Standard 1m signal
            indicators = details,
            timestamp = System.currentTimeMillis()
        )
    }

    private fun calculateEMA(candles: List<Candle>, period: Int): Double {
        if (candles.size < period) return candles.last().close
        val multiplier = 2.0 / (period + 1)
        var ema = candles.take(period).map { it.close }.average()
        for (i in period until candles.size) {
            ema = (candles[i].close - ema) * multiplier + ema
        }
        return ema
    }

    private fun calculateRSI(candles: List<Candle>, period: Int): Double {
        if (candles.size < period + 1) return 50.0
        var avgGain = 0.0
        var avgLoss = 0.0

        for (i in 1..period) {
            val diff = candles[i].close - candles[i - 1].close
            if (diff >= 0) avgGain += diff else avgLoss += abs(diff)
        }

        avgGain /= period
        avgLoss /= period

        for (i in period + 1 until candles.size) {
            val diff = candles[i].close - candles[i - 1].close
            val gain = if (diff >= 0) diff else 0.0
            val loss = if (diff < 0) abs(diff) else 0.0

            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period
        }

        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100.0 - (100.0 / (1.0 + rs))
    }

    private fun calculateMACD(candles: List<Candle>): Pair<Double, Double> {
        val ema12 = calculateEMA(candles, 12)
        val ema26 = calculateEMA(candles, 26)
        val macdLine = ema12 - ema26
        
        // Approximate signal line momentum
        val prevEma12 = calculateEMA(candles.dropLast(1), 12)
        val prevEma26 = calculateEMA(candles.dropLast(1), 26)
        val prevMacdLine = prevEma12 - prevEma26
        val signalLine = (macdLine + prevMacdLine) / 2.0 
        
        return Pair(macdLine, signalLine)
    }

    private fun calculateBollingerBands(candles: List<Candle>, period: Int, stdDevMult: Double): Triple<Double, Double, Double> {
        val lastN = candles.takeLast(period).map { it.close }
        val sma = lastN.average()
        val variance = lastN.map { (it - sma).pow(2.0) }.average()
        val stdDev = sqrt(variance)
        
        return Triple(sma + stdDevMult * stdDev, sma, sma - stdDevMult * stdDev)
    }

    private fun detectPattern(candles: List<Candle>): Pair<Int, String> {
        val current = candles.last()
        val prev = candles[candles.size - 2]
        
        // Engulfing
        if (!prev.isBullish && current.isBullish && current.close > prev.open && current.open < prev.close) {
            return Pair(1, "Bullish Engulfing")
        }
        if (prev.isBullish && !current.isBullish && current.close < prev.open && current.open > prev.close) {
            return Pair(-1, "Bearish Engulfing")
        }

        // Hammer / Shooting Star
        val bodySize = abs(current.close - current.open)
        val lowerWick = if (current.isBullish) current.open - current.low else current.close - current.low
        val upperWick = if (current.isBullish) current.high - current.close else current.high - current.open
        
        if (lowerWick > bodySize * 2 && upperWick < bodySize * 0.5) return Pair(1, "Hammer")
        if (upperWick > bodySize * 2 && lowerWick < bodySize * 0.5) return Pair(-1, "Shooting Star")
        
        // Doji
        if (bodySize < (current.high - current.low) * 0.1) return Pair(0, "Doji")

        return Pair(0, "Neutral")
    }

    private fun analyzeSR(candles: List<Candle>): Pair<Int, String> {
        val lastPrice = candles.last().close
        val window = candles.takeLast(20)
        val recentHigh = window.maxByOrNull { it.high }?.high ?: lastPrice
        val recentLow = window.minByOrNull { it.low }?.low ?: lastPrice
        
        return when {
            lastPrice <= recentLow * 1.001 -> Pair(1, "Near Support")
            lastPrice >= recentHigh * 0.999 -> Pair(-1, "Near Resistance")
            else -> Pair(0, "Mid-range")
        }
    }
}
