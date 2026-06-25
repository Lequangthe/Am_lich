package com.quangthe.amlich.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.quangthe.amlich.R

// Định nghĩa FontFamily với Be Vietnam Pro
val BeVietnamPro = FontFamily(
    Font(R.font.be_vietnam_pro_thin, FontWeight.Thin),
    Font(R.font.be_vietnam_pro_thinitalic, FontWeight.Thin, androidx.compose.ui.text.font.FontStyle.Italic),
    Font(R.font.be_vietnam_pro_extralight, FontWeight.ExtraLight),
    Font(R.font.be_vietnam_pro_extralightitalic, FontWeight.ExtraLight, androidx.compose.ui.text.font.FontStyle.Italic),
    Font(R.font.be_vietnam_pro_light, FontWeight.Light),
    Font(R.font.be_vietnam_pro_lightitalic, FontWeight.Light, androidx.compose.ui.text.font.FontStyle.Italic),
    Font(R.font.be_vietnam_pro_regular, FontWeight.Normal),
    Font(R.font.be_vietnam_pro_italic, FontWeight.Normal, androidx.compose.ui.text.font.FontStyle.Italic),
    Font(R.font.be_vietnam_pro_medium, FontWeight.Medium),
    Font(R.font.be_vietnam_pro_mediumitalic, FontWeight.Medium, androidx.compose.ui.text.font.FontStyle.Italic),
    Font(R.font.be_vietnam_pro_semibold, FontWeight.SemiBold),
    Font(R.font.be_vietnam_pro_semibolditalic, FontWeight.SemiBold, androidx.compose.ui.text.font.FontStyle.Italic),
    Font(R.font.be_vietnam_pro_bold, FontWeight.Bold),
    Font(R.font.be_vietnam_pro_bolditalic, FontWeight.Bold, androidx.compose.ui.text.font.FontStyle.Italic),
    Font(R.font.be_vietnam_pro_extrabold, FontWeight.ExtraBold),
    Font(R.font.be_vietnam_pro_extrabolditalic, FontWeight.ExtraBold, androidx.compose.ui.text.font.FontStyle.Italic),
    Font(R.font.be_vietnam_pro_black, FontWeight.Black),
    Font(R.font.be_vietnam_pro_blackitalic, FontWeight.Black, androidx.compose.ui.text.font.FontStyle.Italic)
)

val AppFontFamily = BeVietnamPro

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.02).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.05.sp
    )
)

val LunarNumberStyle = TextStyle(
    fontFamily = AppFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 12.sp
)
