package com.finorix.signals.di

import com.finorix.signals.data.repository.UserPreferencesRepositoryImpl
import com.finorix.signals.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindCandleRepository(
        impl: com.finorix.signals.data.repository.CandleRepositoryImpl
    ): com.finorix.signals.domain.repository.CandleRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(
        impl: com.finorix.signals.data.repository.AiRepositoryImpl
    ): com.finorix.signals.domain.repository.AiRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: com.finorix.signals.data.repository.AuthRepositoryImpl
    ): com.finorix.signals.domain.repository.AuthRepository

    @Binds
    @Singleton
    abstract fun bindSignalRepository(
        impl: com.finorix.signals.data.repository.SignalRepositoryImpl
    ): com.finorix.signals.domain.repository.SignalRepository
}
