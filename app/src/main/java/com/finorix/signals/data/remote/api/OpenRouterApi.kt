package com.finorix.signals.data.remote.api

import com.finorix.signals.data.remote.dto.ChatRequestDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

interface OpenRouterApi {
    @Streaming
    @POST("chat/completions")
    fun chatCompletion(
        @Header("Authorization") auth: String,
        @Header("HTTP-Referer") referer: String = "https://finorix.app",
        @Header("X-Title") title: String = "Finorix Signals",
        @Body request: ChatRequestDto
    ): Call<ResponseBody>
}
