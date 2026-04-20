package com.finorix.signals.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.finorix.signals.domain.engine.SignalEngine
import com.finorix.signals.domain.model.SignalDirection
import com.finorix.signals.domain.repository.CandleRepository
import com.finorix.signals.presentation.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SignalWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: CandleRepository,
    private val signalEngine: SignalEngine
) : CoroutineWorker(appContext, workerParams) {

    private val CHANNEL_ID = "signal_background_channel"

    override suspend fun doWork(): Result {
        val pairs = listOf("EUR/USD", "GBP/USD", "BTC/USDT")
        
        pairs.forEach { pair ->
            try {
                val result = repository.getCandles(pair, "1m").first()
                if (result is com.finorix.signals.domain.model.Result.Success) {
                    val signal = signalEngine.generateSignal(pair, result.data)
                    if (signal != null && signal.confidence >= 80) {
                        fireNotification(signal)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return Result.success()
    }

    private fun fireNotification(signal: com.finorix.signals.domain.model.Signal) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channel = NotificationChannel(CHANNEL_ID, "Background Signals", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("PAIR", signal.pair)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 
            signal.pair.hashCode() + 1000, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val icon = if (signal.direction == SignalDirection.UP) "🟢" else "🔴"
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$icon ${signal.pair} — ${signal.direction} (${signal.confidence}%)")
            .setContentText("Background Signal Detected! Tap to analyze.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(signal.pair.hashCode() + 1000, notification)
    }
}
