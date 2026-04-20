package com.finorix.signals.data.remote.dto

data class ChatMessageDto(val role: String, val content: String)

data class ChatRequestDto(
    val model: String,
    val messages: List<ChatMessageDto>,
    val stream: Boolean = true,
    val temperature: Double = 0.7,
    val max_tokens: Int = 500
)

data class ChatStreamResponseDto(
    val choices: List<ChoiceDto>
)

data class ChoiceDto(
    val delta: DeltaDto
)

data class DeltaDto(
    val content: String? = null
)
