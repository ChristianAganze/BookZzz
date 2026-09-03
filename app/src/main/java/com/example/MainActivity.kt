package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.data.BookZzzRepository
import com.example.data.NotificationHelper
import com.example.data.PreferencesManager
import com.example.ui.screens.MainScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Initialize our persistent repository
    val repository = BookZzzRepository(applicationContext)

    // Setup notification channel
    NotificationHelper.createNotificationChannel(applicationContext)

    // Request notification permission for Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
      }
    }

    // Proactively schedule reminders for all current bookings
    NotificationHelper.scheduleAllReminders(applicationContext, repository.bookings.value)

    setContent {
      var showSplash by remember { mutableStateOf(true) }
      val preferencesManager = remember { PreferencesManager(applicationContext) }
      val isDarkMode by preferencesManager.isDarkMode.collectAsState(initial = false)
      
      MyApplicationTheme(darkTheme = isDarkMode) {
        if (showSplash) {
          SplashScreen { showSplash = false }
        } else {
          MainScreen(repository, preferencesManager)
        }
      }
    }
  }
}

