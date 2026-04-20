package com.finorix.signals.di

import com.finorix.signals.data.remote.api.BinanceApi
import com.finorix.signals.data.remote.api.YahooFinanceApi
import com.finorix.signals.data.repository.CandleRepositoryImpl
import com.finorix.signals.domain.repository.CandleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import com.google.gson.Gson
import com.finorix.signals.data.remote.api.OpenRouterApi
import com.finorix.signals.domain.repository.AiRepository
import com.finorix.signals.data.repository.AiRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { 
            level = HttpLoggingInterceptor.Level.BODY 
        }
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("Binance")
    fun provideBinanceRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.binance.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("Yahoo")
    fun provideYahooRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://query1.finance.yahoo.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideBinanceApi(@Named("Binance") retrofit: Retrofit): BinanceApi {
        return retrofit.create(BinanceApi::class.java)
    }

    @Provides
    @Singleton
    fun provideYahooApi(@Named("Yahoo") retrofit: Retrofit): YahooFinanceApi {
        return retrofit.create(YahooFinanceApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    @Named("OpenRouter")
    fun provideOpenRouterRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://openrouter.ai/api/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenRouterApi(@Named("OpenRouter") retrofit: Retrofit): OpenRouterApi {
        return retrofit.create(OpenRouterApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAiRepository(
        api: OpenRouterApi,
        gson: Gson,
        prefs: com.finorix.signals.domain.repository.UserPreferencesRepository
    ): AiRepository {
        return AiRepositoryImpl(api, gson, prefs)
    }

    @Provides
    @Singleton
    fun provideCandleRepository(
        binanceApi: BinanceApi,
        yahooApi: YahooFinanceApi
    ): CandleRepository {
        return CandleRepositoryImpl(binanceApi, yahooApi)
    }
}
