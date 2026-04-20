package com.finorix.signals.di

import com.finorix.signals.data.remote.BinanceApi
import com.finorix.signals.data.remote.YahooFinanceApi
import com.finorix.signals.data.repository.AiRepositoryImpl
import com.finorix.signals.domain.repository.AiRepository
import com.finorix.signals.domain.repository.UserPreferencesRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAiRepository(
        gson: Gson,
        client: OkHttpClient,
        prefs: UserPreferencesRepository
    ): AiRepository = AiRepositoryImpl(gson, client, prefs)

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
}
