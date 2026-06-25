package com.quangthe.amlich.widget

import android.content.Context
import android.util.Log
import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.quangthe.amlich.LunarCalendarUtils
import com.quangthe.amlich.MainActivity
import java.util.Calendar

class DetailedLunarWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            DpSize(140.dp, 140.dp),  // vuông nhỏ
            DpSize(180.dp, 180.dp),  // vuông vừa
            DpSize(250.dp, 130.dp),  // ngang nhỏ
            DpSize(320.dp, 130.dp),  // ngang vừa
            DpSize(320.dp, 200.dp),  // ngang lớn
            DpSize(160.dp, 240.dp),  // 2x3
            DpSize(160.dp, 320.dp),  // 2x4
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val cal: Calendar
        val lunar: LunarCalendarUtils.LunarDate
        try {
            val result = LunarCalendarUtils.today()
            cal   = result.first
            lunar = result.second
        } catch (e: Exception) {
            Log.e("DetailedLunarWidget", "Failed to load lunar data", e)
            provideContent { DetailedFallbackContent() }
            return
        }
        provideContent {
            DetailedLunarWidgetContent(cal, lunar)
        }
    }
}

class DetailedLunarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DetailedLunarWidget()
}

// ─── Màu sắc dùng chung ──────────────────────────────────────────────────────
private val COLOR_BG      = Color(0xFFFFFFFF)
private val COLOR_PRIMARY = Color(0xFF322214)
private val COLOR_DIM     = Color(0x26322214)
private val COLOR_ACCENT  = Color(0xFFB85C38)

@SuppressLint("RestrictedApi")
@Composable
private fun DetailedLunarWidgetContent(cal: Calendar, lunar: LunarCalendarUtils.LunarDate) {
    val size = LocalSize.current
    val w = size.width
    val h = size.height
    val isWide = w >= h * 1.35f

    when {
        isWide -> WideLayout(cal, lunar, w, h)
        else   -> SquareLayout(cal, lunar, w, h)
    }
}

// ─── Layout NGANG ─────────────────────────────────────────────────────────────
@SuppressLint("RestrictedApi")
@Composable
private fun WideLayout(cal: Calendar, lunar: LunarCalendarUtils.LunarDate, w: Dp, h: Dp) {
    val wValue = w.value
    val hValue = h.value

    val dayNumSize = (hValue * 0.25f).sp
    val thuSize    = (hValue * 0.12f).sp
    val solarSize  = (hValue * 0.08f).sp
    val lunarSize  = (hValue * 0.08f).sp
    val pad        = (hValue * 0.10f).dp
    val columnGap  = (wValue * 0.05f).dp
    val dividerGap = (hValue * 0.06f).dp

    val dd  = cal.get(Calendar.DAY_OF_MONTH)
    val mm  = cal.get(Calendar.MONTH) + 1
    val yy  = cal.get(Calendar.YEAR)
    val thu = LunarCalendarUtils.dayOfWeekFull(cal).uppercase()

    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(COLOR_BG)
            .padding(horizontal = (wValue * 0.05f).dp, vertical = pad)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxHeight()
                .width(w * 0.28f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dd.toString(),
                style = TextStyle(
                    fontSize = dayNumSize,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(COLOR_PRIMARY),
                    textAlign = TextAlign.Center
                )
            )
        }

        Spacer(modifier = GlanceModifier.width(columnGap))

        Box(
            modifier = GlanceModifier
                .width(1.dp)
                .fillMaxHeight()
                .background(COLOR_DIM)
        ) {}

        Spacer(modifier = GlanceModifier.width(columnGap))

        Column(
            modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = thu,
                style = TextStyle(
                    fontSize = thuSize,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(COLOR_PRIMARY.copy(alpha = 0.5f))
                )
            )
            Spacer(modifier = GlanceModifier.height((hValue * 0.05f).dp))
            Text(
                text = "ngày $dd tháng $mm năm $yy",
                style = TextStyle(
                    fontSize = solarSize,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(COLOR_PRIMARY)
                )
            )
            Spacer(modifier = GlanceModifier.height(dividerGap))
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(COLOR_DIM)
            ) {}
            Spacer(modifier = GlanceModifier.height(dividerGap))
            Text(
                text = "${lunar.day} tháng ${lunar.month}${lunar.leapLabel}  •  ${lunar.tenNam}",
                style = TextStyle(
                    fontSize = lunarSize,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(COLOR_ACCENT)
                )
            )
        }
    }
}

// ─── Layout VUÔNG / DỌC ────────────────────────────────────────────────────────
//
// NGUYÊN NHÂN số ngày bị lỏng:
//   defaultWeight() trong Glance KHÔNG giống Compose — Box giữa không thực sự
//   chiếm hết không gian còn lại, nên số ngày bị đẩy xuống thấp.
//
// GIẢI PHÁP: 3 Box overlay chồng lên nhau
//   Layer 1 (đáy)  : fillMaxSize + Center  → số ngày LUÔN căn giữa toàn widget
//   Layer 2 (trên) : TopCenter             → header đè lên góc trên
//   Layer 3 (dưới) : BottomCenter          → footer đè lên góc dưới
//
@SuppressLint("RestrictedApi")
@Composable
private fun SquareLayout(cal: Calendar, lunar: LunarCalendarUtils.LunarDate, w: Dp, h: Dp) {
    val wv   = w.value
    val hv   = h.value
    val base = if (wv < hv) wv else hv

    val dayNumSize    = (base * 1.2f).sp
    val labelSize     = (base * 0.14f).sp
    val monthYearSize = (base * 0.12f).sp

    val vPad = (hv * 0.05f).dp
    val hPad = (wv * 0.08f).dp

    val dd       = cal.get(Calendar.DAY_OF_MONTH)
    val mm       = cal.get(Calendar.MONTH) + 1
    val yy       = cal.get(Calendar.YEAR)
    val thu      = LunarCalendarUtils.dayOfWeekFull(cal).uppercase()
    val thuShort = LunarCalendarUtils.dayOfWeekShort(cal).uppercase()
    val isLarge  = base >= 200f

    // ── Layer 1: số ngày căn giữa toàn widget ────────────────────────────────
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(COLOR_BG)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dd.toString(),
            style = TextStyle(
                fontSize = dayNumSize,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(COLOR_PRIMARY),
                textAlign = TextAlign.Center
            )
        )
    }

    // ── Layer 2: header đè góc trên ──────────────────────────────────────────
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(start = hPad, end = hPad, top = vPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = if (isLarge) thu else thuShort,
                style = TextStyle(
                    fontSize = labelSize,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(COLOR_PRIMARY.copy(alpha = 0.5f))
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "T$mm/$yy",
                style = TextStyle(
                    fontSize = monthYearSize,
                    color = ColorProvider(COLOR_PRIMARY.copy(alpha = 0.4f))
                )
            )
        }
    }

    // ── Layer 3: footer đè góc dưới ──────────────────────────────────────────
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(start = hPad, end = hPad, bottom = vPad),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(COLOR_DIM)
            ) {}
            Spacer(modifier = GlanceModifier.height((hv * 0.04f).dp))
            Text(
                text = "${lunar.day} tháng ${lunar.month}${lunar.leapLabel}  •  ${lunar.tenNgay}",
                style = TextStyle(
                    fontSize = labelSize,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(COLOR_ACCENT),
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

// ─── Fallback ─────────────────────────────────────────────────────────────────
@SuppressLint("RestrictedApi")
@Composable
private fun DetailedFallbackContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(COLOR_BG)
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "--",
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(COLOR_PRIMARY)
            )
        )
    }
}