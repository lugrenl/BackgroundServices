package com.example.backgroundservices

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*


/**
 * Запускается как "started" сервис. При запуске и остановке показывает временные уведомления.
 * Также содержит логику с таймером и Binder для потенциальной привязки.
 */
class MyBoundService : Service() {

    // Binder для клиентов (Activity)
    inner class MyBinder : Binder() {
        fun getService(): MyBoundService = this@MyBoundService
    }

    private val binder = MyBinder()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null
    private var counter = 0

    // Вызывается при запуске через startService()
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(Constants.logTag, "MyBoundService: onStartCommand")

        // Показываем обычное (смахиваемое) уведомление о запуске
        showNotification("Bound Service", "Сервис запущен и работает в фоне.")

        if (timerJob == null) {
            startTimer()
        }

        return START_STICKY // Перезапускать, если система убьет сервис
    }

    // Вызывается при привязке через bindService()
    override fun onBind(intent: Intent?): IBinder {
        Log.d(Constants.logTag, "MyBoundService: onBind")
        if (timerJob == null) {
            startTimer()
        }
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        // При уничтожении показываем уведомление об остановке
        showNotification("Bound Service", "Сервис остановлен.")

        timerJob?.cancel()
        coroutineScope.cancel()
        Log.d(Constants.logTag, "MyBoundService: onDestroy (timer stopped)")
    }

    private fun startTimer() {
        Log.d(Constants.logTag, "MyBoundService: Timer started")
        timerJob = coroutineScope.launch {
            while (true) {
                delay(1000)
                counter++
                Log.d(Constants.logTag, "MyBoundService Timer: $counter")
            }
        }
    }

    // Публичный метод для клиентов, которые привязались к сервису
    fun getCounter(): Int {
        return counter
    }
}
