package com.finorix.signals.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.finorix.signals.data.local.dao.SignalDao
import com.finorix.signals.data.local.entity.SignalEntity

@Database(entities = [SignalEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun signalDao(): SignalDao
}
