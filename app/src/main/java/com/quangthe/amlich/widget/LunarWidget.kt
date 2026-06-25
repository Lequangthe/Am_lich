package com.quangthe.amlich.widget

import android.content.Context
import android.util.Log
import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.quangthe.amlich.LunarCalendarUtils
import com.quangthe.amlich.MainActivity
import java.util.Calendar

class LunarWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val cal: Calendar
        val lunar: LunarCalendarUtils.LunarDate
        try {
            val result = LunarCalendarUtils.today()
            cal = result.first
            lunar = result.second
        } catch (e: Exception) {
            Log.e("LunarWidget", "Failed to load lunar data", e)
            provideContent { FallbackContent() }
            return
        }
        provideContent {
            LunarWidgetContent(cal, lunar)
        }
    }
}

class LunarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LunarWidget()
}

@SuppressLint("RestrictedApi")
@Composable
private fun LunarWidgetContent(cal: Calendar, lunar: LunarCalendarUtils.LunarDate) {
    val primaryColor = Color(0xFF322214)
    val accentColor = Color(0xFFFDECE4)
    val redColor = Color(0xFFD32F2F)

    val size = LocalSize.current
    val w = size.width
    val h = size.height

    // Tính toán font size linh hoạt theo kích thước thực tế
    val minSide = if (w < h) w.value else h.value
    val daySize = (minSide * 0.5f).sp
    val labelSize = (minSide * 0.1f).sp
    val lunarSize = (minSide * 0.12f).sp
    val padValue = (minSide * 0.1f)
    val pad: Dp = padValue.dp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = pad, vertical = pad)
            .clickable(actionStartActivity<MainActivity>()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = LunarCalendarUtils.dayOfWeekFull(cal).uppercase(),
                style = TextStyle(fontSize = labelSize, color = ColorProvider(primaryColor.copy(alpha = 0.6f)))
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Box(
                modifier = GlanceModifier
                    .size(if (h > 200.dp) 14.dp else 10.dp)
                    .background(redColor)
            ) {}
        }

        Spacer(modifier = GlanceModifier.defaultWeight())

        Text(
            text = cal.get(Calendar.DAY_OF_MONTH).toString(),
            style = TextStyle(
                fontSize = daySize,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(primaryColor)
            )
        )

        Spacer(modifier = GlanceModifier.defaultWeight())

        Text(
            text = "HOÀNG ĐẠO",
            modifier = GlanceModifier.background(accentColor).padding(horizontal = 12.dp, vertical = 6.dp),
            style = TextStyle(fontSize = labelSize, fontWeight = FontWeight.Bold, color = ColorProvider(primaryColor))
        )

        Spacer(modifier = GlanceModifier.height(if (h > 200.dp) 12.dp else 8.dp))

        Text(
            text = "T${lunar.month} ${lunar.tenNam}",
            style = TextStyle(fontSize = lunarSize, color = ColorProvider(primaryColor.copy(alpha = 0.8f)))
        )
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun FallbackContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .clickable(actionStartActivity<MainActivity>()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "--",
            style = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFF322214)))
        )
    }
}
