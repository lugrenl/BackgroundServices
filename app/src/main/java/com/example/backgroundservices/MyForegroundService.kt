package com.example.backgroundservices

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*


/**
 * Запускается как сервис переднего плана, показывает постоянное уведомление о своей работе,
 * пока не будет остановлен. Содержит логику с таймером для имитации фоновой работы.
 */
class MyForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var serviceJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(Constants.logTag, "MyForegroundService: onCreate")
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val input = intent?.getStringExtra(Constants.inputExtra)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, Constants.channelID)
            .setContentTitle(Constants.foregroundServiceNotificationTitle)
            .setContentText(input)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        // Запускаем фоновую задачу
        serviceJob = serviceScope.launch {
            for (i in 1..Int.MAX_VALUE) {
                Log.d(Constants.logTag, "Foreground Service Timer: $i")
                delay(1000)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // При уничтожении показываем уведомление об остановке
        showNotification("MyForegroundService", "Сервис остановлен.")

        serviceJob?.cancel()
        serviceScope.cancel()
        Log.d(Constants.logTag, "MyForegroundService: onDestroy, сервис остановлен.")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null  // Этот сервис не предназначен для привязки
    }
}