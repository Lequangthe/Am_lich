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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onTabClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    darkTheme: Boolean = false,
    onDarkThemeChange: (Boolean) -> Unit = {}
) {
    var tapCount by remember { mutableStateOf(0) }
    var showDeveloperInfo by remember { mutableStateOf(false) }

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
            SettingsSection(title = "THÔNG BÁO & NHẮC NHỞ") {
                var remindFullMoon by remember { mutableStateOf(true) }

                SettingsItem(
                    icon = Icons.Outlined.NotificationsActive,
                    title = "Nhắc ngày Rằm/Mùng một",
                    trailing = {
                        Switch(
                            checked = remindFullMoon,
                            onCheckedChange = { remindFullMoon = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White, //Chỗ này màu thumb switch (nút tròn)
                                checkedTrackColor = Color(0xFF3F51B5) //Chỗ này màu track switch (thanh) khi bật
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

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
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF8D7A5F), //Chỗ này màu chữ tiêu đề section (THÔNG BÁO & NHẮC NHỞ...)
            modifier = Modifier.padding(bottom = 12.dp),
            letterSpacing = 0.5.sp
        )
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
        Text(
            title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface //Chỗ này màu chữ title item cài đặt
        )
        if (subtitle != null) {
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, //Chỗ này màu chữ subtitle item cài đặt
                modifier = Modifier.padding(horizontal = 8.dp),
                maxLines = 2
            )
        }
        if (trailing != null) {
            trailing()
        } else if (onClick != null) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant //Chỗ này màu icon mũi tên phải
            )
        }
    }
}
