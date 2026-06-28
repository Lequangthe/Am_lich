package com.quangthe.amlich.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quangthe.amlich.LunarCalendarUtils

class FastingWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        const val EXTRA_FORCE_NOTIFY = "force_notify"
    }

    override suspend fun doWork(): Result {
        val context = applicationContext
        val isForced = inputData.getBoolean(EXTRA_FORCE_NOTIFY, false)
        Log.d("FastingWorker", "doWork: isForced=$isForced")

        val level = FastingPreferences.getLevel(context)
        Log.d("FastingWorker", "level=$level")

        val (cal, lunarDate) = LunarCalendarUtils.today()
        Log.d("FastingWorker", "lunar=${lunarDate.day}/${lunarDate.month}/${lunarDate.year}")

        val isFasting = if (isForced) {
            true
        } else if (level == FastingLevel.OFF) {
            Log.d("FastingWorker", "skip: OFF")
            return Result.success()
        } else if (level.isMonthBased) {
            val fasting = when (level) {
                FastingLevel.NHAT_NGOAT_TRAI -> {
                    val m = FastingPreferences.getNhatNgoatMonth(context)
                    lunarDate.month == m
                }
                else -> level.isFastingMonth(lunarDate.month)
            }
            Log.d("FastingWorker", "monthBased=$fasting")
            if (!fasting) return@doWork Result.success()
            val notifType = FastingPreferences.getMonthNotifType(context)
            if (notifType == MonthNotificationType.FIRST_3_DAYS && lunarDate.day > 3) {
                Log.d("FastingWorker", "skip: FIRST_3_DAYS")
                return@doWork Result.success()
            }
            true
        } else {
            val monthLength = LunarCalendarUtils.daysInLunarMonth(lunarDate.month, lunarDate.year, lunarDate.leap)
            val ret = level.isFastingDay(lunarDate.day, monthLength)
            Log.d("FastingWorker", "dayBased=$ret monthLength=$monthLength")
            ret
        }

        if (!isFasting) {
            Log.d("FastingWorker", "skip: not fasting day")
            return Result.success()
        }

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else true
        Log.d("FastingWorker", "hasNotificationPermission=$hasPermission")

        val channelId = "fasting_notification"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nhắc ăn chay",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Nhắc ngày ăn chay theo Âm lịch"
            }
            manager.createNotificationChannel(channel)
        }

        val notificationText = if (isForced) {
            "Đây là thông báo kiểm tra. Hôm nay là ngày ${lunarDate.day}/${lunarDate.month} Âm lịch."
        } else if (level.isMonthBased) {
            "Tháng ${lunarDate.month} Âm lịch — tháng ăn chay."
        } else {
            "Hôm nay là ngày ${lunarDate.day} Âm lịch — ngày ăn chay."
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Nhắc ăn chay")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        Log.d("FastingWorker", "Firing notification now...")
        manager.notify(1001, notification)
        Log.d("FastingWorker", "notification sent successfully")
        return Result.success()
    }
}
