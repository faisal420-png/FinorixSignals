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
        prefs: UserPreferencesRepository
    ): AiRepository {
        return AiRepositoryImpl(api, gson, prefs)
    }
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
