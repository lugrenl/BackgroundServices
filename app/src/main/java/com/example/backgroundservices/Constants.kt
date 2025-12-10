package com.example.backgroundservices

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.backgroundservices.Constants.Companion.logTag
import com.example.backgroundservices.Constants.Companion.notificationChannelId
import com.example.backgroundservices.Constants.Companion.notificationChannelName

class Constants {

    companion object {
        // Логирование
        const val logTag = "SERVICE_LOG"

        // Каналы уведомлений
        const val channelID = "myServiceChannel"  // Для постоянного уведомления Foreground
        const val notificationChannelId = "notification_channel"  // Для временных уведомлений
        const val notificationChannelName = "My Service Channel"  // Для временных уведомлений
        const val foregroundServiceNotificationTitle = "My Foreground Service Notification Title"

        // Тексты
        const val inputExtra = "inputExtra"
        const val permissionGrantedToast = "Permission Granted! Starting Service..."
        const val permissionDeniedToast = "Permission Denied for starting service!"
    }
}

/**
 * Глобальная функция-расширение для Context, позволяющая легко показывать временные уведомления.
 */
fun Context.showNotification(title: String, message: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Если у приложения нет разрешения, мы не можем ничего показать.
            Log.w(logTag, "Notification not shown: POST_NOTIFICATIONS permission is missing.")
            return
        }
    }

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Создаем канал, если его еще нет (на случай, если класс App не отработал)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            notificationChannelId,
            notificationChannelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    val notification = NotificationCompat.Builder(this, notificationChannelId)
        .setContentTitle(title)
        .setContentText(message)
        .setSmallIcon(R.drawable.ic_launcher_foreground) // Убедитесь, что эта иконка есть
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true) // Уведомление исчезнет после нажатия на него
        .build()

    // Используем уникальный ID, чтобы уведомления не перезаписывали друг друга
    val notificationId = System.currentTimeMillis().toInt()
    notificationManager.notify(notificationId, notification)
}