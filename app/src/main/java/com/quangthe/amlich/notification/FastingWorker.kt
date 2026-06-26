package com.quangthe.amlich.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quangthe.amlich.LunarCalendarUtils

class FastingWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext

        val level = FastingPreferences.getLevel(context)
        if (level == FastingLevel.OFF) return Result.success()

        val (_, lunarDate) = LunarCalendarUtils.today()
        val isFasting = if (level.isMonthBased) {
            val fasting = when (level) {
                FastingLevel.NHAT_NGOAT_TRAI -> {
                    val m = FastingPreferences.getNhatNgoatMonth(context)
                    lunarDate.month == m
                }
                else -> level.isFastingMonth(lunarDate.month)
            }
            if (!fasting) return@doWork Result.success()
            val notifType = FastingPreferences.getMonthNotifType(context)
            if (notifType == MonthNotificationType.FIRST_3_DAYS && lunarDate.day > 3) {
                return@doWork Result.success()
            }
            true
        } else {
            val monthLength = LunarCalendarUtils.daysInLunarMonth(lunarDate.month, lunarDate.year, lunarDate.leap)
            level.isFastingDay(lunarDate.day, monthLength)
        }
        if (!isFasting) return Result.success()

        val channelId = "fasting_notification"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nhắc ăn chay",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Nhắc ngày ăn chay theo Âm lịch"
            }
            manager.createNotificationChannel(channel)
        }

        val notificationText = if (level.isMonthBased) {
            "Tháng ${lunarDate.month} Âm lịch — tháng ăn chay."
        } else {
            "Hôm nay là ngày ${lunarDate.day} Âm lịch — ngày ăn chay."
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Nhắc ăn chay")
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
        return Result.success()
    }
}
