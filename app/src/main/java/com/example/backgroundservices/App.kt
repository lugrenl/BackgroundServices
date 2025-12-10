package com.example.backgroundservices

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build


/**
 * Создает каналы уведомлений при самом первом запуске приложения.
 * Это обязательный шаг для Android 8.0 и выше.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // Канал для Foreground сервиса (постоянное уведомление)
            val serviceChannel = NotificationChannel(
                Constants.channelID,
                "Канал фонового сервиса",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(serviceChannel)

            // Канал для обычных, временных уведомлений
            val notificationChannel = NotificationChannel(
                Constants.notificationChannelId,
                Constants.notificationChannelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(notificationChannel)
        }
    }
}