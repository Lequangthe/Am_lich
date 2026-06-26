package com.quangthe.amlich

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.quangthe.amlich.notification.FastingWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AmLichApp : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleFastingCheck()
    }

    private fun scheduleFastingCheck() {
        val initialDelay = getInitialDelay()
        val request = PeriodicWorkRequestBuilder<FastingWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "fasting_check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    companion object {
        fun getInitialDelay(): Long {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 6)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            return cal.timeInMillis - now
        }
    }
}
