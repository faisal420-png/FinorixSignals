package com.finorix.signals.di

import android.content.Context
import androidx.room.Room
import com.finorix.signals.data.local.AppDatabase
import com.finorix.signals.data.local.dao.SignalDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "finorix_signals_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSignalDao(database: AppDatabase): SignalDao {
        return database.signalDao()
    }
}
