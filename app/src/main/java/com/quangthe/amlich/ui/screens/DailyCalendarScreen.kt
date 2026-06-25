package com.quangthe.amlich.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    // ─── Ngày hôm nay động ────────────────────────────────────────────────────
    val (cal, lunar) = remember { LunarCalendarUtils.today() }
    val dd   = cal.get(Calendar.DAY_OF_MONTH)
    val mm   = cal.get(Calendar.MONTH) + 1
    val yy   = cal.get(Calendar.YEAR)
    val monthHeader = "${LunarCalendarUtils.solarMonthName(mm)} $yy"
    val lunarPill   = "${lunar.day.toString().padStart(2, '0')} / ${lunar.month.toString().padStart(2, '0')}   ${lunar.tenNam}"

    // Quote state
    var currentQuote by remember { mutableStateOf(QuoteService.getDailyQuote()) }

    // Lunar → Solar dialog state
    var showConvertDialog by remember { mutableStateOf(false) }

    // Input state for conversion
    var luDay by remember { mutableStateOf("") }
    var luMonth by remember { mutableStateOf("") }
    var luYear by remember { mutableStateOf("") }
    var luLeap by remember { mutableStateOf(false) }
    var convertResult by remember { mutableStateOf<String?>(null) }
    var convertError by remember { mutableStateOf(false) }

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
            Text(
                monthHeader,
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF000000), //Chỗ này màu chữ "Tháng Một 2026"...
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            // Large Solar Day — kéo dài cao hơn
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .height(440.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color.White) //Chỗ này màu nền ô vuông chứa số ngày
                    .border(1.5.dp, Color(0xFF03A9F4), RoundedCornerShape(40.dp)), //Chỗ này màu viền ô vuông số ngày
                contentAlignment = Alignment.Center
            ) {
                Text(
                    dd.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 200.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1545A5) //Chỗ này màu chữ số ngày (lớn)
                    )
                )
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

    // ─── Dialog đổi Âm → Dương ─────────────────────────────────────────────
    if (showConvertDialog) {
        AlertDialog(
            onDismissRequest = { showConvertDialog = false; convertResult = null; convertError = false },
            title = { Text("Đổi Âm lịch → Dương lịch") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = luDay, onValueChange = { luDay = it },
                            label = { Text("Ngày") }, modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = luMonth, onValueChange = { luMonth = it },
                            label = { Text("Tháng") }, modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = luYear, onValueChange = { luYear = it },
                        label = { Text("Năm") }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = luLeap, onCheckedChange = { luLeap = it })
                        Text("Tháng nhuận")
                    }
                    HorizontalDivider()
                    Text("Kết quả", style = MaterialTheme.typography.labelLarge)
                    if (convertError) {
                        Text("Sai định dạng!", color = MaterialTheme.colorScheme.error)
                    }
                    if (convertResult != null) {
                        Text(convertResult!!, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val d = luDay.toIntOrNull()
                    val m = luMonth.toIntOrNull()
                    val y = luYear.toIntOrNull()
                    if (d != null && m != null && y != null) {
                        val solar = LunarCalendarUtils.lunarToSolar(d, m, y, luLeap)
                        val cal = Calendar.getInstance().apply {
                            set(solar.year, solar.month - 1, solar.day)
                        }
                        val thu = LunarCalendarUtils.dayOfWeekFull(cal)
                        convertResult = "$thu, ${solar.day} Tháng ${solar.month} Năm ${solar.year}"
                        convertError = false
                    } else {
                        convertResult = null
                        convertError = true
                    }
                }) { Text("Xem") }
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
