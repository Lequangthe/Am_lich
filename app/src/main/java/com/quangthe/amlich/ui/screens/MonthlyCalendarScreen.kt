package com.quangthe.amlich.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quangthe.amlich.LunarCalendarUtils
import com.quangthe.amlich.QuoteService
import com.quangthe.amlich.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Calendar

// Phạm vi năm hỗ trợ
private const val START_YEAR_OFFSET = 5   // 5 năm trước
private const val END_YEAR_OFFSET   = 5   // 5 năm sau

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MonthlyCalendarScreen(onTabClick: (String) -> Unit = {}) {

    // ─── Ngày hôm nay ─────────────────────────────────────────────────────────
    val (todayCal, _) = remember { LunarCalendarUtils.today() }
    val todayDay   = todayCal.get(Calendar.DAY_OF_MONTH)
    val todayMonth = todayCal.get(Calendar.MONTH) + 1
    val todayYear  = todayCal.get(Calendar.YEAR)

    val startYear = todayYear - START_YEAR_OFFSET
    val endYear   = todayYear + END_YEAR_OFFSET
    val totalPages = (endYear - startYear + 1) * 12

    // Trang tương ứng tháng hiện tại
    fun pageOf(year: Int, month: Int) = (year - startYear) * 12 + (month - 1)
    val initialPage = pageOf(todayYear, todayMonth)

    val pagerState    = rememberPagerState(pageCount = { totalPages }, initialPage = initialPage)
    val coroutineScope = rememberCoroutineScope()

    // ─── Ngày đang được chọn ──────────────────────────────────────────────────
    var selectedDay   by remember { mutableStateOf(todayDay) }
    var selectedMonth by remember { mutableStateOf(todayMonth) }
    var selectedYear  by remember { mutableStateOf(todayYear) }

    // Tháng/năm đang hiển thị theo pager
    val currentPageMonth = pagerState.currentPage % 12 + 1
    val currentPageYear  = startYear + pagerState.currentPage / 12

    // ─── Date Picker Dialog (mở khi bấm nút 3 gạch) ──────────────────────────
    var showDatePicker by remember { mutableStateOf(false) }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = run {
                Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth - 1, selectedDay, 12, 0, 0)
                }.timeInMillis
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        val y = cal.get(Calendar.YEAR)
                        val m = cal.get(Calendar.MONTH) + 1
                        val d = cal.get(Calendar.DAY_OF_MONTH)
                        selectedDay   = d
                        selectedMonth = m
                        selectedYear  = y
                        val targetPage = pageOf(y, m)
                        if (targetPage in 0 until totalPages) {
                            coroutineScope.launch { pagerState.animateScrollToPage(targetPage) }
                        }
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Huỷ") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // ① Chỉ hiện "Tháng X YYYY" — không có can chi
                    Text(
                        "Tháng $currentPageMonth $currentPageYear",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                },
                // ⑤ Nút 3 gạch → mở DatePicker để chọn ngày tháng năm
                navigationIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Default.EditCalendar,
                            contentDescription = "Chọn ngày",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // Nút quay về hôm nay
                    IconButton(onClick = {
                        selectedDay   = todayDay
                        selectedMonth = todayMonth
                        selectedYear  = todayYear
                        coroutineScope.launch { pagerState.animateScrollToPage(initialPage) }
                    }) {
                        Icon(
                            Icons.Default.Adjust,
                            contentDescription = "Hôm nay",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(currentTab = "Tháng", onTabClick = onTabClick)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            DayLabels()

            // ② HorizontalPager: vuốt qua cả nhiều năm, chọn ngày ở bất kỳ tháng nào
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val pageMonth = page % 12 + 1
                val pageYear  = startYear + page / 12

                CalendarGrid(
                    month      = pageMonth,
                    year       = pageYear,
                    todayDay   = if (pageMonth == todayMonth && pageYear == todayYear) todayDay else -1,
                    selectedDay = if (pageMonth == selectedMonth && pageYear == selectedYear) selectedDay else -1,
                    onDayClick = { day ->
                        selectedDay   = day
                        selectedMonth = pageMonth
                        selectedYear  = pageYear
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chi tiết ngày được chọn — hiển thị ở mọi tháng
            SelectedDayDetails(
                day   = selectedDay,
                month = selectedMonth,
                year  = selectedYear
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DayLabels() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val days = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
        days.forEachIndexed { index, day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = if (index == 6) Color(0xFF1A73E8) //Chỗ này màu chữ thứ Bảy (CN)
                        else MaterialTheme.colorScheme.onSurfaceVariant //Chỗ này màu chữ thứ Hai → thứ Sáu
            )
        }
    }
}

@Composable
fun CalendarGrid(
    month: Int,
    year: Int,
    todayDay: Int,
    selectedDay: Int,
    onDayClick: (Int) -> Unit
) {
    val totalDays  = LunarCalendarUtils.daysInSolarMonth(month, year)
    val emptyCells = LunarCalendarUtils.firstDayOfWeekOffset(month, year)
    val totalCells = emptyCells + totalDays
    val rows       = (totalCells + 6) / 7

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) //Chỗ này màu nền khung lịch tháng
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp)) //Chỗ này màu viền khung lịch tháng
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val index = row * 7 + col
                        if (index < emptyCells || index >= totalCells) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.8f)
                                    .background(MaterialTheme.colorScheme.surface) //Chỗ này màu nền ô trống (đầu tháng)
                                    .border(0.2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)) //Chỗ này màu viền ô trống
                            )
                        } else {
                            val day    = index - emptyCells + 1
                            val lunar  = LunarCalendarUtils.solarToLunar(day, month, year)
                            val lunarLabel = "${lunar.day}/${lunar.month}"
                            Box(modifier = Modifier.weight(1f)) {
                                CalendarCell(
                                    day       = day,
                                    lunarDay  = lunarLabel,
                                    isSelected = day == selectedDay,
                                    isToday   = day == todayDay,
                                    onClick   = { onDayClick(day) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarCell(
    day: Int,
    lunarDay: String,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(0.8f)
            .background(if (isSelected) SurfaceContainerHigh else MaterialTheme.colorScheme.surface) //Chỗ này màu nền ô vuông chứa số ngày (chọn/ko chọn)
            .border(
                width = if (isSelected) 2.dp else 0.2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary //Chỗ này màu viền ô ngày khi được chọn
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f) //Chỗ này màu viền ô ngày khi không chọn
            )
            .clickable { onClick() }
            .clipToBounds()
    ) {
        // Chấm xanh hôm nay (góc trên phải)
        if (isToday) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-3).dp, y = 3.dp)
                    .background(Color(0xFF00B8D4), CircleShape) //Chỗ này màu chấm tròn hôm nay
            )
        }
        Text(
            text = day.toString(),
            modifier = Modifier.padding(6.dp).align(Alignment.TopStart),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = when {
                isToday    -> Color(0xFF00B8D4) //Chỗ này màu chữ số ngày hôm nay
                isSelected -> MaterialTheme.colorScheme.primary //Chỗ này màu chữ số ngày khi được chọn
                else       -> MaterialTheme.colorScheme.onSurface //Chỗ này màu chữ số ngày bình thường
            }
        )
        Text(
            text = lunarDay,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 4.dp, end = 4.dp),
            style = LunarNumberStyle.copy(fontSize = 10.sp),
            maxLines = 1,
            softWrap = false,
            color = if (isSelected) MaterialTheme.colorScheme.primary //Chỗ này màu chữ âm lịch khi chọn
                    else Color(0xFF2E7D32) //Chỗ này màu chữ âm lịch bình thường (xanh lá)
        )
    }
}

@Composable
fun SelectedDayDetails(day: Int, month: Int, year: Int) {
    val lunar = remember(day, month, year) {
        LunarCalendarUtils.solarToLunar(day, month, year)
    }
    val thuTen = remember(day, month, year) {
        val cal = Calendar.getInstance().also { it.set(year, month - 1, day) }
        LunarCalendarUtils.dayOfWeekFull(cal)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow) //Chỗ này màu nền chi tiết ngày được chọn
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp)) //Chỗ này màu viền chi tiết ngày được chọn
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "$thuTen, ${day.toString().padStart(2, '0')} Tháng $month $year",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary //Chỗ này màu chữ thứ + ngày tháng năm
                )
                Text(
                    "Ngày ${lunar.day} tháng ${lunar.month}${lunar.leapLabel} năm ${lunar.tenNam}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant //Chỗ này màu chữ âm lịch + can chi
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "LỜI HAY Ý ĐẸP",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline //Chỗ này màu chữ "LỜI HAY Ý ĐẸP"
        )
        Spacer(modifier = Modifier.height(4.dp))

        var quote by remember { mutableStateOf<QuoteService.DailyQuote?>(null) }
        var loaded by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            quote = QuoteService.getDailyQuote()
            loaded = true
        }

        if (loaded) {
            Column(
                modifier = Modifier.clickable {
                    quote = null; loaded = false
                    scope.launch {
                        quote = QuoteService.getRandomQuote()
                        loaded = true
                    }
                }
            ) {
                Text(
                    "\"${quote?.content ?: ""}\"",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic
                    ),
                    color = MaterialTheme.colorScheme.primary //Chỗ này màu chữ nội dung danh ngôn
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "— ${quote?.author ?: ""}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline //Chỗ này màu chữ tác giả danh ngôn
                )
            }
        } else {
            Text(
                "\"...\"",
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun StatusChip(text: String, containerColor: Color, contentColor: Color) {
    Surface(color = containerColor, shape = CircleShape) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

@Composable
fun BottomNavigationBar(currentTab: String = "Tháng", onTabClick: (String) -> Unit = {}) {
    NavigationBar(
        containerColor = SurfaceContainerLow, //Chỗ này màu nền thanh điều hướng dưới (Tháng / Ngày / Cài đặt)
        modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        NavigationBarItem(
            selected  = currentTab == "Tháng",
            onClick   = { onTabClick("Tháng") },
            icon      = { Icon(Icons.Default.GridView, contentDescription = null) },
            label     = { Text("Tháng", style = MaterialTheme.typography.labelMedium) }
        )
        NavigationBarItem(
            selected  = currentTab == "Ngày",
            onClick   = { onTabClick("Ngày") },
            icon      = { Icon(Icons.Default.CalendarViewDay, contentDescription = null) },
            label     = { Text("Ngày", style = MaterialTheme.typography.labelMedium) }
        )
        NavigationBarItem(
            selected  = currentTab == "Cài đặt",
            onClick   = { onTabClick("Cài đặt") },
            icon      = { Icon(Icons.Default.Settings, contentDescription = null) },
            label     = { Text("Cài đặt", style = MaterialTheme.typography.labelMedium) }
        )
    }
}
