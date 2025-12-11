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
    private var bindCount = 0 // Счетчик привязок

    override fun onCreate() {
        super.onCreate()
        Log.d(Constants.logTag, "MyBoundService: onCreate")

        // Показываем уведомление при создании сервиса
        showNotification("Bound Service", "Сервис создан и готов к привязке.")
    }

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
        bindCount++
        Log.d(Constants.logTag, "MyBoundService: onBind, активных привязок: $bindCount")

        if (timerJob == null) {
            startTimer()
        }

        // Показываем уведомление о привязке
        showNotification("Bound Service", "Клиент привязан к сервису. Активных привязок: $bindCount")

        return binder
    }

    // Вызывается при отвязке всех клиентов
    override fun onUnbind(intent: Intent?): Boolean {
        bindCount--
        Log.d(Constants.logTag, "MyBoundService: onUnbind, осталось привязок: $bindCount")

        if (bindCount <= 0) {
            // Последний клиент отвязался, можно остановить сервис
            stopTimer()
            showNotification("Bound Service", "Все клиенты отвязаны. Сервис остановится.")
        } else {
            showNotification("Bound Service", "Клиент отвязан. Активных привязок: $bindCount")
        }

        return true // true означает, что можно перепривязаться позже с тем же intent
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        bindCount++
        Log.d(Constants.logTag, "MyBoundService: onRebind, активных привязок: $bindCount")
        showNotification("Bound Service", "Клиент перепривязан. Активных привязок: $bindCount")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(Constants.logTag, "MyBoundService: onDestroy")

        // При уничтожении показываем уведомление об остановке
        showNotification("Bound Service", "Сервис уничтожен.")

        stopTimer()
        coroutineScope.cancel()
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

    private fun stopTimer() {
        Log.d(Constants.logTag, "MyBoundService: Timer stopped")
        timerJob?.cancel()
        timerJob = null
    }

    // Публичный метод для клиентов, которые привязались к сервису
    fun getCounter(): Int {
        return counter
    }
}
