package com.quangthe.amlich

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.quangthe.amlich.ui.screens.DailyCalendarScreen
import com.quangthe.amlich.ui.screens.MonthlyCalendarScreen
import com.quangthe.amlich.ui.screens.SettingsScreen
import com.quangthe.amlich.ui.theme.AmLichTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var darkTheme by remember { mutableStateOf(false) }

            AmLichTheme(darkTheme = darkTheme) {
                // Request notification permission on Android 13+
                val context = LocalContext.current
                val requestNotifPermission = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { /* granted or denied — no-op */ }
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context, Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentTab by remember { mutableStateOf("Ngày") }

                    when (currentTab) {
                        "Tháng" -> MonthlyCalendarScreen(onTabClick = { currentTab = it })
                        "Ngày"  -> DailyCalendarScreen(onTabClick = { currentTab = it })
                        "Cài đặt" -> SettingsScreen(
                            onTabClick = { currentTab = it },
                            onBackClick = { currentTab = "Ngày" },
                            darkTheme = darkTheme,
                            onDarkThemeChange = { darkTheme = it }
                        )
                        else -> DailyCalendarScreen(onTabClick = { currentTab = it })
                    }
                }
            }
        }
    }
}
