package com.finorix.signals.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.finorix.signals.domain.repository.AuthRepository
import com.finorix.signals.presentation.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FinorixMessagingService : FirebaseMessagingService() {

    @Inject lateinit var authRepository: AuthRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            authRepository.updateFcmToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        val type = message.data["type"] ?: "broadcast"
        val title = message.notification?.title ?: message.data["title"] ?: "Finorix Update"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        
        showNotification(title, body, type, message.data)
    }

    private fun showNotification(title: String, body: String, type: String, data: Map<String, String>) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = when (type) {
            "signal" -> "signals_channel"
            "broadcast" -> "broadcasts_channel"
            else -> "updates_channel"
        }

        createNotificationChannels(notificationManager)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data.forEach { (key, value) -> putExtra(key, value) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(if (type == "signal") NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0x00FF88) // Neon Green
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannels(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val signalChannel = NotificationChannel(
                "signals_channel", "Signals", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Real-time trading signal alerts" }
            
            val broadcastChannel = NotificationChannel(
                "broadcasts_channel", "Broadcasts", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Important platform announcements" }
            
            val updateChannel = NotificationChannel(
                "updates_channel", "Updates", NotificationManager.IMPORTANCE_LOW
            ).apply { description = "General app updates" }

            manager.createNotificationChannel(signalChannel)
            manager.createNotificationChannel(broadcastChannel)
            manager.createNotificationChannel(updateChannel)
        }
    }
}
