package com.finorix.signals.data.remote.dto

data class YahooFinanceResponse(val chart: YahooChartData?)
data class YahooChartData(val result: List<YahooResult>?, val error: Any?)
data class YahooResult(
    val meta: YahooMeta?, 
    val timestamp: List<Long>?, 
    val indicators: YahooIndicators?
)
data class YahooMeta(val currency: String?, val symbol: String?)
data class YahooIndicators(val quote: List<YahooQuote>?)
data class YahooQuote(
    val open: List<Double?>?,
    val high: List<Double?>?,
    val low: List<Double?>?,
    val close: List<Double?>?,
    val volume: List<Double?>?
)
