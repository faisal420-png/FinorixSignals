package com.finorix.signals.data.local.dao

import androidx.room.*
import com.finorix.signals.data.local.entity.SignalEntity
import com.finorix.signals.data.local.entity.SignalOutcome
import kotlinx.coroutines.flow.Flow

@Dao
interface SignalDao {
    @Query("SELECT * FROM signals ORDER BY timestamp DESC LIMIT 50")
    fun getRecentSignals(): Flow<List<SignalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: SignalEntity)

    @Update
    suspend fun updateSignal(signal: SignalEntity)

    @Query("SELECT * FROM signals WHERE outcome = :outcome")
    suspend fun getSignalsByOutcome(outcome: SignalOutcome): List<SignalEntity>

    @Query("SELECT * FROM signals WHERE outcome != 'PENDING'")
    fun getResolvedSignals(): Flow<List<SignalEntity>>

    @Query("UPDATE signals SET outcome = :outcome WHERE timestamp = :timestamp")
    suspend fun updateOutcomeByTimestamp(timestamp: Long, outcome: SignalOutcome)
}
