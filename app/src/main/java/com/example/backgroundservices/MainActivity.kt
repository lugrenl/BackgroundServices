package com.example.backgroundservices

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.backgroundservices.ui.theme.BackgroundServicesTheme

class MainActivity : ComponentActivity() {

    // Лаунчер для обработки ответа на запрос разрешения
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Разрешение получено, теперь можно запускать сервис
                Toast.makeText(this, Constants.permissionGrantedToast,
                    Toast.LENGTH_SHORT).show()
                startService("Started")
            } else {
                // Пользователь отказал в разрешении
                Toast.makeText(this, Constants.permissionDeniedToast,
                    Toast.LENGTH_LONG).show()
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
                    Column {
                        Spacer(
                            modifier = Modifier.height(30.dp)
                        )
                        Text(
                            text = "Start",
                            fontSize = 20.sp,
                            modifier = Modifier.clickable { askForNotificationPermission() }
                        )
                        Spacer(
                            modifier = Modifier.height(30.dp)
                        )
                        Text(
                            text = "Stop",
                            fontSize = 20.sp,
                            modifier = Modifier.clickable { stopService() }
                        )
                        Spacer(
                            modifier = Modifier.height(30.dp)
                        )
                        Text(
                            text = "Download",
                            fontSize = 20.sp,
                            modifier = Modifier.clickable { download() }
                        )
                    }
                }
            }
        }
    }

    // Функция для проверки и запроса разрешения
    private fun askForNotificationPermission() {
        // Проверка нужна только для Android 13 (API 33) и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Проверяем, есть ли у нас уже разрешение
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            ) {
                // Если разрешение уже есть, просто запускаем сервис
                startService("Started")
            } else {
                // Если разрешения нет, запускаем лаунчер, который покажет системный диалог
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Для версий Android ниже 13 разрешение не требуется, запускаем сервис сразу
            startService("Started")
        }
    }

    private fun download() {
    }

    private fun startService(input: String) {
        val myServiceIntent = Intent(this, MyForegroundService::class.java)
        myServiceIntent.putExtra(Constants.inputExtra, input)
        ContextCompat.startForegroundService(this, myServiceIntent)
    }

    private fun stopService() {
        val myServiceIntent = Intent(this, MyForegroundService::class.java)
        stopService(myServiceIntent)
    }
}



