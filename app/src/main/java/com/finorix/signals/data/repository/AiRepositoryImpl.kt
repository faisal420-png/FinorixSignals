package com.finorix.signals.data.repository

import com.finorix.signals.BuildConfig
import com.finorix.signals.data.remote.api.OpenRouterApi
import com.finorix.signals.data.remote.dto.ChatMessageDto
import com.finorix.signals.data.remote.dto.ChatRequestDto
import com.finorix.signals.data.remote.dto.ChatStreamResponseDto
import com.finorix.signals.domain.model.Candle
import com.finorix.signals.domain.model.Signal
import com.finorix.signals.domain.repository.AiRepository
import com.finorix.signals.domain.repository.UserPreferencesRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import okhttp3.ResponseBody
import java.util.Locale
import javax.inject.Inject

class AiRepositoryImpl @Inject constructor(
    private val api: OpenRouterApi,
    private val gson: Gson,
    private val prefs: UserPreferencesRepository
) : AiRepository {

    private val models = listOf(
        "deepseek/deepseek-chat-v3.1:free",
        "meta-llama/llama-3.3-70b-instruct:free",
        "google/gemini-2.0-flash-exp:free",
        "mistralai/mistral-small-3.2-24b-instruct:free"
    )

    override fun explainSignal(signal: Signal, candles: List<Candle>): Flow<String> = flow {
        val last5 = candles.takeLast(5).joinToString(", ") { String.format(Locale.US, "%.5f", it.close) }
        val indicators = signal.indicators.entries.joinToString("; ") { "${it.key}: ${it.value}" }
        
        val systemPrompt = "You are Finorix AI, an expert technical analyst. Given indicator readings and recent candles, explain in 3 short sentences why the signal is UP or DOWN. Be confident and clear. Always end with: 'Educational analysis only, not financial advice.'"
        val userPrompt = "Pair: ${signal.pair}, Direction: ${signal.direction}, Confidence: ${signal.confidence}%. Indicators: $indicators. Last 5 candle closes: $last5."

        emitAll(streamWithFallback(systemPrompt, userPrompt))
    }.flowOn(Dispatchers.IO)

    override fun chatWithAssistant(userMessage: String, marketContext: String): Flow<String> = flow {
        val systemPrompt = "You are Finorix AI, a friendly trading assistant. Help the user understand markets, indicators, and their current signals. Never give direct financial advice. Keep replies under 100 words."
        val userPrompt = "Context: $marketContext\nUser: $userMessage"

        emitAll(streamWithFallback(systemPrompt, userPrompt))
    }.flowOn(Dispatchers.IO)

    private fun streamWithFallback(systemPrompt: String, userPrompt: String): Flow<String> = flow {
        val userModel = prefs.selectedModel.first()
        val userKey = prefs.openRouterApiKey.first()
        val apiKey = if (userKey.isNotEmpty()) userKey else BuildConfig.OPENROUTER_API_KEY
        
        val priorityModels = mutableListOf<String>().apply {
            add(userModel)
            addAll(models.filter { it != userModel })
        }

        var success = false
        for (model in priorityModels) {
            try {
                val response = api.chatCompletion(
                    auth = "Bearer $apiKey",
                    request = ChatRequestDto(
                        model = model,
                        messages = listOf(
                            ChatMessageDto("system", systemPrompt),
                            ChatMessageDto("user", userPrompt)
                        )
                    )
                ).execute()

                if (response.isSuccessful) {
                    val body = response.body() ?: continue
                    parseStream(body).collect { emit(it) }
                    success = true
                    break
                }
            } catch (e: Exception) {
                // Try next model
            }
        }
        if (!success) {
            emit("AI services are currently busy or your API key is invalid. Please check your OpenRouter key in Settings.")
        }
    }

    private fun parseStream(body: ResponseBody): Flow<String> = flow {
        body.byteStream().bufferedReader().use { reader ->
            reader.lineSequence().forEach { line ->
                if (line.startsWith("data: ")) {
                    val data = line.substring(6).trim()
                    if (data == "[DONE]") return@forEach
                    try {
                        val chunk = gson.fromJson(data, ChatStreamResponseDto::class.java)
                        val content = chunk.choices.firstOrNull()?.delta?.content
                        if (content != null) {
                            emit(content)
                        }
                    } catch (e: Exception) {
                        // Skip malformed chunks
                    }
                }
            }
        }
    }
}
