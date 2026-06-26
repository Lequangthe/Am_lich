package com.quangthe.amlich.notification

enum class FastingLevel(
    val displayName: String,
    val description: String,
    val days: List<Int> = emptyList(),
    val fastingMonths: List<Int> = emptyList(),
    val shortMonthSub: Map<Int, Int> = emptyMap()
) {
    OFF("Tắt", "Không nhắc"),
    TWO_DAY("Nhị trai", "Mùng 1, Rằm (15)", days = listOf(1, 15)),
    TU_TRAI("Tứ trai", "1, 14, 15, 29/30", days = listOf(1, 14, 15, 29, 30)),
    LUC_TRAI("Lục trai", "8, 14, 15, 23, 29/30 (tháng thiếu 28 thay 30)",
        days = listOf(8, 14, 15, 23, 29, 30), shortMonthSub = mapOf(30 to 28)),
    THAP_TRAI("Thập trai", "1, 8, 14, 15, 18, 23, 24, 28, 29/30 (tháng thiếu 27 thay 30)",
        days = listOf(1, 8, 14, 15, 18, 23, 24, 28, 29, 30), shortMonthSub = mapOf(30 to 27)),
    NHAT_NGOAT_TRAI("Nhứt ngoạt trai", "Tháng Giêng, tháng Bảy, tháng Mười",
        fastingMonths = listOf(1, 7, 10)),
    TAM_NGOAT_TRAI("Tam ngoạt trai", "Tháng Giêng, tháng Năm, tháng Chín",
        fastingMonths = listOf(1, 5, 9));

    val isMonthBased: Boolean get() = fastingMonths.isNotEmpty()

    fun isFastingDay(lunarDay: Int, monthLength: Int = 30): Boolean {
        if (isMonthBased) return false
        if (lunarDay in days) return true
        if (monthLength < 30) {
            return lunarDay in shortMonthSub.values
        }
        return false
    }

    fun isFastingMonth(lunarMonth: Int): Boolean = lunarMonth in fastingMonths
}

enum class MonthNotificationType(val label: String) {
    ALL_DAYS("Nhắc hết các ngày trong tháng"),
    FIRST_3_DAYS("Nhắc 3 ngày đầu tháng")
}

