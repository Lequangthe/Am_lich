package com.quangthe.amlich

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
