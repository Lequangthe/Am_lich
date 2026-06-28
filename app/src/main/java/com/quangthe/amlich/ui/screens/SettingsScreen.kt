package com.quangthe.amlich.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.quangthe.amlich.AmLichApp
import com.quangthe.amlich.notification.FastingLevel
import com.quangthe.amlich.notification.MonthNotificationType
import com.quangthe.amlich.notification.FastingPreferences
import com.quangthe.amlich.notification.FastingWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onTabClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    darkTheme: Boolean = false,
    onDarkThemeChange: (Boolean) -> Unit = {}
) {
    var tapCount by remember { mutableIntStateOf(0) }
    var showDeveloperInfo by remember { mutableStateOf(false) }
    var showFastingInfo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Nút dark mode nhanh ở top bar
                    IconButton(onClick = { onDarkThemeChange(!darkTheme) }) {
                        Icon(
                            if (darkTheme) Icons.Default.WbSunny else Icons.Default.NightlightRound,
                            contentDescription = "Toggle Dark Mode",
                            tint = Color(0xFF3F51B5) //Chỗ này màu icon dark mode trên top bar
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomNavigationBar(currentTab = "Cài đặt", onTabClick = onTabClick)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Section: THÔNG BÁO & NHẮC NHỞ
            SettingsSection(
                title = "THÔNG BÁO & NHẮC NHỞ",
                trailing = {
                    IconButton(onClick = { showFastingInfo = true }) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = "Chi tiết chế độ ăn chay",
                            tint = Color(0xFF8D7A5F),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            ) {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val selectedLevel by FastingPreferences.getLevelFlow(context)
                    .collectAsState(initial = FastingLevel.OFF)
                val selectedMonth by FastingPreferences.getNhatNgoatMonthFlow(context)
                    .collectAsState(initial = 1)
                val notifType by FastingPreferences.getMonthNotifTypeFlow(context)
                    .collectAsState(initial = MonthNotificationType.ALL_DAYS)

                // Chế độ ngày
                FastingLevel.entries.take(5).forEach { level ->
                    SettingsItem(
                        icon = if (level == FastingLevel.OFF) Icons.Outlined.NotificationsOff
                               else Icons.Outlined.NotificationsActive,
                        title = level.displayName,
                        subtitle = level.description,
                        trailing = {
                            RadioButton(
                                selected = selectedLevel == level,
                                onClick = {
                                    scope.launch {
                                        FastingPreferences.setLevel(context, level)
                                        updateWorker(context, level)
                                    }
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF3F51B5)
                                )
                            )
                        },
                        onClick = {
                            scope.launch {
                                FastingPreferences.setLevel(context, level)
                                updateWorker(context, level)
                            }
                        }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Notification type cho chế độ tháng
                val showMonthNotif = selectedLevel == FastingLevel.NHAT_NGOAT_TRAI
                        || selectedLevel == FastingLevel.TAM_NGOAT_TRAI
                if (showMonthNotif) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MonthNotificationType.entries.forEach { type ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        scope.launch {
                                            FastingPreferences.setMonthNotifType(context, type)
                                        }
                                    },
                                color = if (notifType == type) Color(0xFF3F51B5)
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    type.label,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (notifType == type) Color.White
                                            else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Nhứt ngoạt trai
                val nhatLevel = FastingLevel.NHAT_NGOAT_TRAI
                SettingsItem(
                    icon = Icons.Outlined.Event,
                    title = nhatLevel.displayName,
                    subtitle = "Chọn tháng ăn chay",
                    trailing = {
                        RadioButton(
                            selected = selectedLevel == nhatLevel,
                            onClick = {
                                scope.launch {
                                    FastingPreferences.setLevel(context, nhatLevel)
                                    updateWorker(context, nhatLevel)
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3F51B5))
                        )
                    },
                    onClick = {
                        scope.launch {
                            FastingPreferences.setLevel(context, nhatLevel)
                            updateWorker(context, nhatLevel)
                        }
                    }
                )
                // Sub-options chọn tháng cho Nhứt ngoạt trai
                if (selectedLevel == nhatLevel) {
                    val monthNames = listOf(
                        Pair(1, "Tháng Giêng"),
                        Pair(7, "Tháng Bảy"),
                        Pair(10, "Tháng Mười")
                    )
                    monthNames.forEach { (m, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        FastingPreferences.setNhatNgoatMonth(context, m)
                                    }
                                }
                                .padding(start = 56.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMonth == m,
                                onClick = {
                                    scope.launch {
                                        FastingPreferences.setNhatNgoatMonth(context, m)
                                    }
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF3F51B5)
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Tam ngoạt trai
                val tamLevel = FastingLevel.TAM_NGOAT_TRAI
                SettingsItem(
                    icon = Icons.Outlined.Event,
                    title = tamLevel.displayName,
                    subtitle = "Tháng Giêng, tháng Năm, tháng Chín",
                    trailing = {
                        RadioButton(
                            selected = selectedLevel == tamLevel,
                            onClick = {
                                scope.launch {
                                    FastingPreferences.setLevel(context, tamLevel)
                                    updateWorker(context, tamLevel)
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3F51B5))
                        )
                    },
                    onClick = {
                        scope.launch {
                            FastingPreferences.setLevel(context, tamLevel)
                            updateWorker(context, tamLevel)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        scope.launch {
                            if (selectedLevel == FastingLevel.OFF) {
                                FastingPreferences.setLevel(context, FastingLevel.TWO_DAY)
                            }
                            val testWork = OneTimeWorkRequestBuilder<FastingWorker>()
                                .setInputData(androidx.work.workDataOf(FastingWorker.EXTRA_FORCE_NOTIFY to true))
                                .build()
                            WorkManager.getInstance(context).enqueue(testWork)
                            android.widget.Toast.makeText(context, "Đã gửi thông báo kiểm tra", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                ) { Text("Test thông báo ăn chay") }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Section: GIAO DIỆN & WIDGET
            SettingsSection(title = "GIAO DIỆN & WIDGET") {
                SettingsItem(
                    icon = Icons.Outlined.Contrast,
                    title = "Chế độ giao diện",
                    trailing = {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (darkTheme) Color(0xFF2A2A2A) else Color(0xFFF2F2F2) //Chỗ này màu nền toggle Sáng/Tối
                                )
                                .padding(2.dp)
                        ) {
                            Text(
                                "Sáng",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (!darkTheme) Color.White else Color.Transparent //Chỗ này màu nền nút Sáng khi được chọn
                                    )
                                    .clickable { onDarkThemeChange(false) }
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (!darkTheme) Color(0xFF3F51B5) else Color.Gray //Chỗ này màu chữ nút Sáng
                            )
                            Text(
                                "Tối",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (darkTheme) Color(0xFF444444) else Color.Transparent //Chỗ này màu nền nút Tối khi được chọn
                                    )
                                    .clickable { onDarkThemeChange(true) }
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (darkTheme) Color(0xFFD4AA8C) else Color.Gray //Chỗ này màu chữ nút Tối
                            )
                        }
                    }
                )

            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: THÔNG TIN ỨNG DỤNG
            SettingsSection(title = "THÔNG TIN ỨNG DỤNG") {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = "Phiên bản",
                    subtitle = if (showDeveloperInfo) "Lê Quang Thế\nZalo: 0989884524" else "1.0.0",
                    onClick = {
                        tapCount++
                        if (tapCount >= 4) {
                            showDeveloperInfo = !showDeveloperInfo
                            tapCount = 0
                        }
                    }
                )

            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
    // ─── Dialog chi tiết chế độ ăn chay ─────────────────────────────────
    if (showFastingInfo) {
        AlertDialog(
            onDismissRequest = { showFastingInfo = false },
            title = { Text("Ý nghĩa các chế độ ăn chay") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Với người ăn chay kỳ, quy định ăn chay với từng chế độ như sau:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FastingInfoItem(
                        title = "Nhị trai",
                        detail = "Ăn chay mỗi tháng 2 lần vào ngày mồng 1 và ngày rằm (15) hàng tháng."
                    )
                    FastingInfoItem(
                        title = "Tứ trai",
                        detail = "Ăn chay 4 ngày trong tháng: mồng 1, mồng 8, rằm (15) và ngày 23 (hoặc 30)."
                    )
                    FastingInfoItem(
                        title = "Lục trai",
                        detail = "Ăn chay 6 ngày mỗi tháng: mồng 8, 14, 15, 23 — " +
                                "cuối tháng chọn 1 trong 2: 29 hoặc 30 (tháng thiếu dùng 28 thay 30)."
                    )
                    FastingInfoItem(
                        title = "Thập trai",
                        detail = "Ăn chay 10 ngày mỗi tháng: mồng 1, 8, 14, 15, 18, 23, 24, 28 — " +
                                "cuối tháng chọn 1 trong 2: 29 hoặc 30 (tháng thiếu dùng 27 thay 30)."
                    )
                    FastingInfoItem(
                        title = "Nhứt ngoạt trai",
                        detail = "Ăn chay liên tục 1 tháng trong năm, chọn 1 trong 3: tháng Giêng, tháng Bảy hoặc tháng Mười."
                    )
                    FastingInfoItem(
                        title = "Tam ngoạt trai",
                        detail = "Ăn chay liên tục cả tháng, mỗi năm 3 tháng: tháng Giêng, tháng Năm và tháng Chín."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showFastingInfo = false }) {
                    Text("Đã hiểu")
                }
            }
        )
    }
}

@Composable
private fun FastingInfoItem(title: String, detail: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFF3F51B5),
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun updateWorker(context: android.content.Context, level: FastingLevel) {
    val workManager = WorkManager.getInstance(context)
    if (level == FastingLevel.OFF) {
        workManager.cancelUniqueWork("fasting_check")
        workManager.cancelUniqueWork("fasting_check_immediate")
    } else {
        val request = PeriodicWorkRequestBuilder<FastingWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(AmLichApp.getInitialDelay(), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "fasting_check",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
        val immediate = OneTimeWorkRequestBuilder<FastingWorker>().build()
        workManager.enqueueUniqueWork(
            "fasting_check_immediate",
            ExistingWorkPolicy.REPLACE,
            immediate
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF8D7A5F), //Chỗ này màu chữ tiêu đề section (THÔNG BÁO & NHẮC NHỞ...)
                modifier = Modifier.weight(1f),
                letterSpacing = 0.5.sp
            )
            if (trailing != null) {
                trailing()
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), //Chỗ này màu nền card section
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface, //Chỗ này màu icon item cài đặt
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface //Chỗ này màu chữ title item cài đặt
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, //Chỗ này màu chữ subtitle item cài đặt
                    maxLines = 3
                )
            }
        }
        if (trailing != null) {
            Box(modifier = Modifier.padding(start = 8.dp)) {
                trailing()
            }
        } else if (onClick != null) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant //Chỗ này màu icon mũi tên phải
            )
        }
    }
}
