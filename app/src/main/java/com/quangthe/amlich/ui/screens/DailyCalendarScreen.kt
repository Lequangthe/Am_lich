package com.quangthe.amlich.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quangthe.amlich.LunarCalendarUtils
import com.quangthe.amlich.QuoteService
import com.quangthe.amlich.ui.theme.SurfaceContainerLow
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCalendarScreen(onTabClick: (String) -> Unit = {}) {

    // ─── Ngày hiện tại (hôm nay) ──────────────────────────────────────────────
    val todayCal = remember { Calendar.getInstance() }
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)
    val todayMonth = todayCal.get(Calendar.MONTH) + 1
    val todayYear = todayCal.get(Calendar.YEAR)

    // ─── Ngày đang xem (có thể điều hướng) ──────────────────────────────────
    var viewDay by remember { mutableIntStateOf(todayDay) }
    var viewMonth by remember { mutableIntStateOf(todayMonth) }
    var viewYear by remember { mutableIntStateOf(todayYear) }

    val lunar = remember(viewDay, viewMonth, viewYear) {
        LunarCalendarUtils.solarToLunar(viewDay, viewMonth, viewYear)
    }
    val monthHeader = "${LunarCalendarUtils.solarMonthName(viewMonth)} $viewYear"
    val lunarPill   = "${lunar.day.toString().padStart(2, '0')} / ${lunar.month.toString().padStart(2, '0')}   ${lunar.tenNam}"
    val thu = LunarCalendarUtils.dayOfWeekFull(
        Calendar.getInstance().apply { set(viewYear, viewMonth - 1, viewDay) }
    )
    val isToday = viewDay == todayDay && viewMonth == todayMonth && viewYear == todayYear

    fun previousDay() {
        val c = Calendar.getInstance().apply { set(viewYear, viewMonth - 1, viewDay) }
        c.add(Calendar.DAY_OF_YEAR, -1)
        viewDay = c.get(Calendar.DAY_OF_MONTH)
        viewMonth = c.get(Calendar.MONTH) + 1
        viewYear = c.get(Calendar.YEAR)
    }

    fun nextDay() {
        val c = Calendar.getInstance().apply { set(viewYear, viewMonth - 1, viewDay) }
        c.add(Calendar.DAY_OF_YEAR, 1)
        viewDay = c.get(Calendar.DAY_OF_MONTH)
        viewMonth = c.get(Calendar.MONTH) + 1
        viewYear = c.get(Calendar.YEAR)
    }

    fun goToToday() {
        viewDay = todayDay
        viewMonth = todayMonth
        viewYear = todayYear
    }

    // Quote state
    var currentQuote by remember { mutableStateOf(QuoteService.getDailyQuote()) }

    // Lunar → Solar dialog state
    var showConvertDialog by remember { mutableStateOf(false) }

    // Input state for conversion
    var convertMode by remember { mutableIntStateOf(0) } // 0: Am->Duong, 1: Duong->Am
    var luDayIdx by remember { mutableIntStateOf(-1) }
    var luMonthIdx by remember { mutableIntStateOf(-1) }
    var luYearText by remember { mutableStateOf("") }
    
    var soDayIdx by remember { mutableIntStateOf(-1) }
    var soMonthIdx by remember { mutableIntStateOf(-1) }
    var soYearText by remember { mutableStateOf("") }

    var convertResult by remember { mutableStateOf<String?>(null) }
    var convertError by remember { mutableStateOf(false) }

    // Dropdown states
    var expandedMonth by remember { mutableStateOf(false) }
    var expandedDay by remember { mutableStateOf(false) }
    var expandedSoMonth by remember { mutableStateOf(false) }
    var expandedSoDay by remember { mutableStateOf(false) }

    // Computed from inputs
    val luYearInt = luYearText.toIntOrNull()
    val validMonths = remember(luYearInt) {
        if (luYearInt != null) LunarCalendarUtils.getValidMonthsInYear(luYearInt) else emptyList()
    }
    val selectedMonth = if (luMonthIdx in validMonths.indices) validMonths[luMonthIdx] else null
    val maxDays = remember(luYearInt, selectedMonth) {
        if (luYearInt != null && selectedMonth != null)
            LunarCalendarUtils.daysInLunarMonth(selectedMonth.first, luYearInt, selectedMonth.second)
        else 0
    }
    val dayItems = remember(maxDays) { if (maxDays > 0) (1..maxDays).toList() else emptyList() }

    val soYearInt = soYearText.toIntOrNull()
    val soMonthItems = (1..12).toList()
    val maxSoDays = remember(soYearInt, soMonthIdx) {
        if (soYearInt != null && soMonthIdx in 0..11)
            LunarCalendarUtils.daysInSolarMonth(soMonthIdx + 1, soYearInt)
        else 31
    }
    val soDayItems = (1..maxSoDays).toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.NightlightRound,
                            contentDescription = null,
                            tint = Color(0xFF3F51B5), //Chỗ này màu icon mặt trăng trên top bar
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Lịch ngày",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showConvertDialog = true }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Đổi âm dương", modifier = Modifier.size(28.dp))
                    }
                    if (!isToday) {
                        IconButton(onClick = { goToToday() }) {
                            Icon(Icons.Default.Today, contentDescription = "Hôm nay", modifier = Modifier.size(28.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomNavigationBar(currentTab = "Ngày", onTabClick = onTabClick)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Month Year Header — kéo lên cao
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { previousDay() }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Ngày trước", tint = Color(0xFF3F51B5))
                }
                Text(
                    monthHeader,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF000000), //Chỗ này màu chữ "Tháng Một 2026"...
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { nextDay() }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Ngày sau", tint = Color(0xFF3F51B5))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Large Solar Day — kéo dài cao hơn
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .height(440.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color.White) //Chỗ này màu nền ô vuông chứa số ngày
                    .border(1.5.dp, Color(0xFF03A9F4), RoundedCornerShape(40.dp)) //Chỗ này màu viền ô vuông số ngày
                    .pointerInput(Unit) {
                        var totalDrag = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                totalDrag += dragAmount
                                if (totalDrag > 100) {
                                    totalDrag = 0f; previousDay()
                                } else if (totalDrag < -100) {
                                    totalDrag = 0f; nextDay()
                                }
                            },
                            onDragEnd = { totalDrag = 0f },
                            onDragCancel = { totalDrag = 0f }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        viewDay.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 200.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1545A5) //Chỗ này màu chữ số ngày (lớn)
                        )
                    )
                    Text(
                        thu,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1545A5).copy(alpha = 0.7f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Lunar Date Pill
            Surface(
                color = Color(0xFF1271F3), //Chỗ này màu nền pill âm lịch
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 2.dp
            ) {
                Text(
                    lunarPill,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFFFFFF) //Chỗ này màu chữ pill âm lịch
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Buddhist Quote Card
            QuoteCard(
                quote = currentQuote,
                onClick = { currentQuote = QuoteService.getRandomQuote() }
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }

    // ─── Dialog đổi Âm ↔ Dương ─────────────────────────────────────────────
    if (showConvertDialog) {
        AlertDialog(
            onDismissRequest = { 
                showConvertDialog = false; convertResult = null; convertError = false 
            },
            title = { Text("Chuyển đổi Âm - Dương") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TabRow(
                        selectedTabIndex = convertMode,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = {}
                    ) {
                        Tab(
                            selected = convertMode == 0,
                            onClick = { convertMode = 0; convertResult = null; convertError = false },
                            text = { Text("Âm → Dương", style = MaterialTheme.typography.labelLarge) }
                        )
                        Tab(
                            selected = convertMode == 1,
                            onClick = { convertMode = 1; convertResult = null; convertError = false },
                            text = { Text("Dương → Âm", style = MaterialTheme.typography.labelLarge) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (convertMode == 0) {
                        // Giao diện Âm -> Dương (như cũ)
                        OutlinedTextField(
                            value = luYearText, onValueChange = {
                                luYearText = it; luMonthIdx = -1; luDayIdx = -1
                            },
                            label = { Text("Năm âm lịch") }, modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenuBox(
                            expanded = expandedMonth,
                            onExpandedChange = { if (validMonths.isNotEmpty()) expandedMonth = it }
                        ) {
                            OutlinedTextField(
                                value = if (selectedMonth != null) {
                                    if (selectedMonth.second) "Tháng ${selectedMonth.first} (Nhuận)"
                                    else "Tháng ${selectedMonth.first}"
                                } else "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tháng âm lịch") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = expandedMonth,
                                onDismissRequest = { expandedMonth = false }
                            ) {
                                validMonths.forEachIndexed { index, (month, isLeap) ->
                                    DropdownMenuItem(
                                        text = { Text(if (isLeap) "Tháng $month (Nhuận)" else "Tháng $month") },
                                        onClick = {
                                            luMonthIdx = index; luDayIdx = -1; expandedMonth = false
                                        }
                                    )
                                }
                            }
                        }
                        ExposedDropdownMenuBox(
                            expanded = expandedDay,
                            onExpandedChange = { if (dayItems.isNotEmpty()) expandedDay = it }
                        ) {
                            OutlinedTextField(
                                value = if (luDayIdx in dayItems.indices) dayItems[luDayIdx].toString() else "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Ngày âm lịch") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDay) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true,
                                enabled = selectedMonth != null
                            )
                            ExposedDropdownMenu(
                                expanded = expandedDay,
                                onDismissRequest = { expandedDay = false }
                            ) {
                                dayItems.forEachIndexed { index, day ->
                                    DropdownMenuItem(
                                        text = { Text("Ngày $day") },
                                        onClick = { luDayIdx = index; expandedDay = false }
                                    )
                                }
                            }
                        }
                    } else {
                        // Giao diện Dương -> Âm
                        OutlinedTextField(
                            value = soYearText,
                            onValueChange = { 
                                soYearText = it; soMonthIdx = -1; soDayIdx = -1 
                            },
                            label = { Text("Năm dương lịch") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        ExposedDropdownMenuBox(
                            expanded = expandedSoMonth,
                            onExpandedChange = { expandedSoMonth = it }
                        ) {
                            OutlinedTextField(
                                value = if (soMonthIdx in 0..11) "Tháng ${soMonthIdx + 1}" else "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tháng dương lịch") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSoMonth) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true,
                                enabled = soYearInt != null
                            )
                            ExposedDropdownMenu(
                                expanded = expandedSoMonth,
                                onDismissRequest = { expandedSoMonth = false }
                            ) {
                                soMonthItems.forEachIndexed { index, month ->
                                    DropdownMenuItem(
                                        text = { Text("Tháng $month") },
                                        onClick = {
                                            soMonthIdx = index; soDayIdx = -1; expandedSoMonth = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = expandedSoDay,
                            onExpandedChange = { if (soDayItems.isNotEmpty()) expandedSoDay = it }
                        ) {
                            OutlinedTextField(
                                value = if (soDayIdx in soDayItems.indices) soDayItems[soDayIdx].toString() else "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Ngày dương lịch") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSoDay) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true,
                                enabled = soMonthIdx != -1
                            )
                            ExposedDropdownMenu(
                                expanded = expandedSoDay,
                                onDismissRequest = { expandedSoDay = false }
                            ) {
                                soDayItems.forEach { day ->
                                    DropdownMenuItem(
                                        text = { Text("Ngày $day") },
                                        onClick = { 
                                            soDayIdx = day - 1; expandedSoDay = false 
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider()
                    Text("Kết quả", style = MaterialTheme.typography.labelLarge)
                    if (convertError) {
                        Text(
                            if (convertMode == 0) "Vui lòng chọn đầy đủ Ngày, Tháng, Năm âm lịch!"
                            else "Vui lòng nhập đầy đủ Ngày, Tháng, Năm dương lịch!",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (convertResult != null) {
                        Text(convertResult!!, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF1545A5))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (convertMode == 0) {
                        // Xử lý Âm -> Dương
                        if (luDayIdx in dayItems.indices && selectedMonth != null && luYearInt != null) {
                            val day = dayItems[luDayIdx]
                            val solar = LunarCalendarUtils.lunarToSolar(day, selectedMonth.first, luYearInt, selectedMonth.second)
                            val calC = Calendar.getInstance().apply {
                                set(solar.year, solar.month - 1, solar.day)
                            }
                            val thu = LunarCalendarUtils.dayOfWeekFull(calC)
                            convertResult = "Dương lịch: $thu, ${solar.day}/${solar.month}/${solar.year}"
                            convertError = false
                        } else {
                            convertResult = null
                            convertError = true
                        }
                    } else {
                        // Xử lý Dương -> Âm
                        val d = if (soDayIdx != -1) soDayItems[soDayIdx] else null
                        val m = if (soMonthIdx != -1) soMonthIdx + 1 else null
                        val y = soYearInt
                        if (d != null && m != null && y != null) {
                            try {
                                val lunarRes = LunarCalendarUtils.solarToLunar(d, m, y)
                                val calC = Calendar.getInstance().apply { set(y, m - 1, d) }
                                val thu = LunarCalendarUtils.dayOfWeekFull(calC)
                                convertResult = "Âm lịch: $thu, ngày ${lunarRes.day}/${lunarRes.month}${lunarRes.leapLabel}\nNăm ${lunarRes.tenNam}"
                                convertError = false
                            } catch (e: Exception) {
                                convertResult = "Ngày không hợp lệ!"
                                convertError = true
                            }
                        } else {
                            convertResult = null
                            convertError = true
                        }
                    }
                }) { Text("Chuyển đổi") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConvertDialog = false; convertResult = null; convertError = false
                }) { Text("Đóng") }
            }
        )
    }
}

@Composable
fun InfoCard(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceContainerLow, //Chỗ này màu nền InfoCard
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, Color.LightGray.copy(alpha = 0.2f)) //Chỗ này màu viền InfoCard
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8D7A5F), fontWeight = FontWeight.Bold) //Chỗ này màu chữ title InfoCard
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun CanChiItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.LightGray) //Chỗ này màu chữ nhãn Can Chi
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun HourItem(time: String, name: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White, //Chỗ này màu nền HourItem
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color(0xFFEEE0D6)) //Chỗ này màu viền HourItem
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(time, style = MaterialTheme.typography.labelSmall, color = Color.Gray) //Chỗ này màu chữ giờ
            Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF322214)) //Chỗ này màu chữ tên giờ
        }
    }
}

@Composable
fun DirectionCard(icon: ImageVector, title: String, direction: String, iconColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White, //Chỗ này màu nền DirectionCard
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, Color(0xFFEEE0D6)) //Chỗ này màu viền DirectionCard
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = iconColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray) //Chỗ này màu chữ title DirectionCard
                Text(direction, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF322214)) //Chỗ này màu chữ hướng
            }
        }
    }
}

@Composable
fun QuoteCard(quote: QuoteService.DailyQuote, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color(0xFFFDFCFB), //Chỗ này màu nền card danh ngôn
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF3EFE0)) //Chỗ này màu viền card danh ngôn
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.FormatQuote, contentDescription = null, tint = Color(0xFF8D7A5F).copy(alpha = 0.3f), modifier = Modifier.size(32.dp)) //Chỗ này màu icon dấu ngoặc kép
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                quote.content,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp),
                textAlign = TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = Color(0xFF4A4A4A) //Chỗ này màu chữ nội dung danh ngôn
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(modifier = Modifier.width(40.dp).height(1.dp).background(Color(0xFF8D7A5F).copy(alpha = 0.5f))) //Chỗ này màu đường kẻ divider
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                quote.author.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8D7A5F), //Chỗ này màu chữ tác giả danh ngôn
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
