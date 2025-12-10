package com.example.backgroundservices

import android.app.DownloadManager
import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.backgroundservices.ui.theme.BackgroundServicesTheme
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {

    // Это действие будет выполнено после получения разрешения
    private var onPermissionGrantedAction: (() -> Unit)? = null

    // Универсальный лаунчер для обработки ответа на запрос разрешения
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Разрешение получено, теперь можно запускать сервис
                Toast.makeText(this, Constants.permissionGrantedToast, Toast.LENGTH_SHORT).show()
                // Выполняем сохраненное действие (например, запуск сервиса)
                onPermissionGrantedAction?.invoke()
                onPermissionGrantedAction = null // Очищаем действие после выполнения
            } else {
                // Пользователь отказал в разрешении
                Toast.makeText(this, Constants.permissionDeniedToast, Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BackgroundServicesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column (
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        // --- Foreground Service ---
                        Text(
                            text = "Start Foreground Service",
                            fontSize = 20.sp,
                            modifier = Modifier.clickable { askForNotificationPermission {
                                startService(MyForegroundService::class.java, "Foreground сервис запущен")
                            } }
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        Text(
                            text = "Stop Foreground Service",
                            fontSize = 20.sp,
                            modifier = Modifier.clickable { stopService(MyForegroundService::class.java) }
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        // --- Download Manager ---
                        Text(
                            text = "Download",
                            fontSize = 20.sp,
                            modifier = Modifier.clickable { download() }
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        // --- Bound Service ---
                        Text(
                            text = "Start Bound Service",
                            fontSize = 20.sp,
                            modifier = Modifier.clickable {
                                // Для запуска Bound Service разрешение на уведомления не требуется,
                                // но сервис сам покажет уведомление, если у приложения есть разрешение.
                                startService(MyBoundService::class.java)
                            }
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        Text(
                            text = "Stop Bound Service",
                            fontSize = 20.sp,
                            modifier = Modifier.clickable { stopService(MyBoundService::class.java) }
                        )
                    }
                }
            }
        }
    }

    /**
     * Универсальная функция запроса разрешения на уведомления.
     * Принимает лямбда-выражение, которое будет выполнено, если разрешение есть или будет получено.
     */
    private fun askForNotificationPermission(onGranted: () -> Unit) {
        // Проверка нужна только для Android 13 (API 33) и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                onGranted() // Разрешение уже есть
            } else {
                // Сохраняем действие и запрашиваем разрешение
                onPermissionGrantedAction = onGranted
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            onGranted() // Для версий Android ниже 13 разрешение не требуется
        }
    }

    /**
     * Фунцкия для запуска Download Manager
     */
    private fun download() {
        val fileUrl = "https://cloud.telecombg.ru/s/xmnaoycZDCpZfSM/download/house.jpg"
        val fileName = "house.jpg"

        val request = DownloadManager.Request(fileUrl.toUri())
            .setTitle(fileName)
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(this, "Загрузка началась...", Toast.LENGTH_SHORT).show()
    }

    /**
     * Универсальная функция для запуска любого сервиса.
     */
    private fun <T : Service> startService(serviceClass: Class<T>, input: String = "") {
        val intent = Intent(this, serviceClass).apply {
            if (input.isNotEmpty()) {
                putExtra(Constants.inputExtra, input)
            }
        }
        // Foreground сервис требует специального вызова
        if (MyForegroundService::class.java.isAssignableFrom(serviceClass)) {
            ContextCompat.startForegroundService(this, intent)
        } else {
            startService(intent) // Для обычных и bound сервисов
        }
    }

    /**
     * Универсальная функция для остановки любого сервиса.
     */
    private fun <T : Service> stopService(serviceClass: Class<T>) {
        val intent = Intent(this, serviceClass)
        stopService(intent)
    }
}



