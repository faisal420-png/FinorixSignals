package com.finorix.signals.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.finorix.signals.domain.engine.SignalEngine
import com.finorix.signals.domain.model.Result
import com.finorix.signals.domain.model.SignalDirection
import com.finorix.signals.domain.repository.CandleRepository
import com.finorix.signals.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class SignalWatcherService : Service() {

    @Inject lateinit var repository: CandleRepository
    @Inject lateinit var signalEngine: SignalEngine
    @Inject lateinit var signalDao: com.finorix.signals.data.local.dao.SignalDao
    @Inject lateinit var gson: com.google.gson.Gson
    @Inject lateinit var prefs: com.finorix.signals.domain.repository.UserPreferencesRepository
    @Inject lateinit var signalRepository: com.finorix.signals.domain.repository.SignalRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var watchJob: Job? = null

    private val CHANNEL_ID = "signal_watcher_channel"
    private val NOTIFICATION_ID = 101

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            val pairs = prefs.enabledPairs.first().toList()
            val finalPairs = if (pairs.isEmpty()) listOf("EUR/USD", "BTC/USDT") else pairs
            
            startForeground(NOTIFICATION_ID, createPersistentNotification(finalPairs.size))
            startWatching(finalPairs)
            startOutcomeChecking()
        }
        return START_STICKY
    }

    private fun startWatching(pairs: List<String>) {
        watchJob?.cancel()
        watchJob = serviceScope.launch {
            val threshold = prefs.notificationConfidence.first()
            val timeframe = prefs.defaultTimeframe.first()
            
            while (isActive) {
                pairs.forEach { pair ->
                    try {
                        val result = repository.getCandles(pair, timeframe).first()
                        if (result is Result.Success) {
                            val signal = signalEngine.generateSignal(pair, result.data)
                            if (signal != null && signal.confidence >= threshold) {
                                fireSignalNotification(signal)
                                saveSignalToDb(signal, result.data.last().close)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                delay(30000L) // Poll every 30s
            }
        }
    }

    private fun fireSignalNotification(signal: com.finorix.signals.domain.model.Signal) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("PAIR", signal.pair)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 
            signal.pair.hashCode(), 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val directionIcon = if (signal.direction == SignalDirection.UP) "🟢" else "🔴"
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$directionIcon ${signal.pair} — ${signal.direction} Signal (${signal.confidence}%)")
            .setContentText("Expires in ${signal.expiresIn}s — Tap to view analysis")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(signal.pair.hashCode(), notification)
    }

    private fun createPersistentNotification(count: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Finorix Auto Signals Active")
            .setContentText("Finorix is watching $count pairs")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, 
            "Signal Watcher", 
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Channel for real-time market signal alerts"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun saveSignalToDb(signal: com.finorix.signals.domain.model.Signal, entryPrice: Double) {
        serviceScope.launch {
            signalDao.insertSignal(
                com.finorix.signals.data.local.entity.SignalEntity(
                    pair = signal.pair,
                    direction = signal.direction,
                    confidence = signal.confidence,
                    timestamp = signal.timestamp,
                    expiryTimestamp = signal.timestamp + (signal.expiresIn * 1000),
                    entryPrice = entryPrice,
                    indicatorJson = gson.toJson(signal.indicators)
                )
            )
            signalRepository.saveSignal(signal).collect {}
        }
    }

    private fun startOutcomeChecking() {
        serviceScope.launch {
            while (isActive) {
                val pending = signalDao.getSignalsByOutcome(com.finorix.signals.data.local.entity.SignalOutcome.PENDING)
                val now = System.currentTimeMillis()
                
                pending.filter { it.expiryTimestamp < now }.forEach { signal ->
                    try {
                        val result = repository.getCandles(signal.pair, "1m").first()
                        if (result is Result.Success) {
                            val currentPrice = result.data.last().close
                            val isWin = if (signal.direction == SignalDirection.UP) {
                                currentPrice > signal.entryPrice
                            } else {
                                currentPrice < signal.entryPrice
                            }
                            signal.outcome = if (isWin) {
                                com.finorix.signals.data.local.entity.SignalOutcome.WIN
                            } else {
                                com.finorix.signals.data.local.entity.SignalOutcome.LOSS
                            }
                            signalDao.updateSignal(signal)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                delay(60000L) // Check every minute
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
