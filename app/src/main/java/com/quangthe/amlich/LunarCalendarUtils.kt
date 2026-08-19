package com.quangthe.amlich

import java.util.Calendar
import kotlin.math.floor

/**
 * Tiện ích chuyển đổi dương lịch ↔ âm lịch (Việt Nam, múi giờ +7)
 * Thuật toán dựa trên Ho Ngoc Duc & Jean Meeus
 */
object LunarCalendarUtils {

    private const val TIME_ZONE_OFFSET = 7.0 // GMT+7

    /** Epoch new moon JD (gần 01/01/1900) */
    private const val EPOCH = 2415021.076998695

    // ─── Can Chi ─────────────────────────────────────────────────────────────

    private val CAN  = listOf("Giáp","Ất","Bính","Đinh","Mậu","Kỷ","Canh","Tân","Nhâm","Quý")
    private val CHI  = listOf("Tý","Sửu","Dần","Mão","Thìn","Tỵ","Ngọ","Mùi","Thân","Dậu","Tuất","Hợi")
    private val THU       = listOf("Chủ Nhật","Thứ Hai","Thứ Ba","Thứ Tư","Thứ Năm","Thứ Sáu","Thứ Bảy")
    private val THU_VIET  = listOf("CN","T2","T3","T4","T5","T6","T7")

    // ─── Kết quả âm lịch ─────────────────────────────────────────────────────

    data class LunarDate(
        val day   : Int,
        val month : Int,
        val year  : Int,
        val leap  : Boolean,      // tháng nhuận
        val canNam   : String,    // Can năm
        val chiNam   : String,    // Chi năm
        val canThang : String,    // Can tháng
        val chiThang : String,    // Chi tháng
        val canNgay  : String,    // Can ngày
        val chiNgay  : String     // Chi ngày
    ) {
        val tenNam   : String get() = "$canNam $chiNam"
        val tenThang : String get() = "$canThang $chiThang"
        val tenNgay  : String get() = "$canNgay $chiNgay"
        val leapLabel: String get() = if (leap) " (Nhuận)" else ""
    }

    data class SolarDate(val day: Int, val month: Int, val year: Int)

    // ─── Helper toán học ─────────────────────────────────────────────────────

    private fun int(d: Double): Int = floor(d).toInt()

    /** Modulo luôn dương, tránh kết quả âm */
    private fun posMod(value: Int, mod: Int): Int =
        ((value % mod + mod) % mod)

    private fun posMod(value: Long, mod: Int): Int =
        ((value % mod + mod) % mod).toInt()

    /** Julian Day Number từ ngày dương lịch */
    private fun jdFromDate(dd: Int, mm: Int, yy: Int): Int {
        val a = int((14 - mm) / 12.0)
        val y = yy + 4800 - a
        val m = mm + 12 * a - 3
        var jd = dd + int((153 * m + 2) / 5.0) + 365 * y +
                int(y / 4.0) - int(y / 100.0) + int(y / 400.0) - 32045
        if (jd < 2299161) {
            jd = dd + int((153 * m + 2) / 5.0) + 365 * y +
                    int(y / 4.0) - 32083
        }
        return jd
    }

    /** Dương lịch từ Julian Day Number */
    private fun jdToDate(jd: Int): Triple<Int, Int, Int> {
        val a: Int
        val b: Int
        val c: Int
        if (jd > 2299160) {
            a = jd + 32044
            b = int((4 * a + 3) / 146097.0)
            c = a - int(146097 * b / 4.0)
        } else {
            b = 0; c = jd + 32082
        }
        val d = int((4 * c + 3) / 1461.0)
        val e = c - int(1461 * d / 4.0)
        val m = int((5 * e + 2) / 153.0)
        val day   = e - int((153 * m + 2) / 5.0) + 1
        val month = m + 3 - 12 * int(m / 10.0)
        val year  = 100 * b + d - 4800 + int(m / 10.0)
        return Triple(day, month, year)
    }

    /** ΔT (sai số đồng hồ thiên văn) tính theo ngày */
    private fun deltaT(T: Double): Double {
        val y = 1900 + T * 100
        return when {
            y < 948 -> {
                val t = (y - 1820) / 100.0
                (21.7 + 77.0 * t + 158.0 * t * t) / 86400.0
            }
            y < 2000 -> {
                val t = (y - 2000) / 100.0
                (64.12 + 202.36 * t + 303.3 * t * t + 570.6 * t * t * t + 222.0 * t * t * t * t) / 86400.0
            }
            y < 2050 -> {
                val t = y - 2000
                (62.92 + 0.32217 * t + 0.005589 * t * t) / 86400.0
            }
            else -> {
                val t = (y - 2000) / 100.0
                (0.5 + 32.0 * t * t) / 86400.0
            }
        }
    }

    /** Thời điểm trăng mới thứ k (tính bằng Julian Day) */
    private fun newMoon(k: Int): Double {
        val T  = k / 1236.85
        val T2 = T * T
        val T3 = T2 * T
        val dr = Math.PI / 180

        var Jd1 = 2415020.75933 + 29.53058868 * k +
                0.0001178 * T2 - 0.000000155 * T3
        Jd1 += 0.00033 * Math.sin((166.56 + 132.87 * T - 0.009173 * T2) * dr)

        val M   = 359.2242  + 29.10535608  * k - 0.0000333  * T2 - 0.00000347  * T3
        val Mpr = 306.0253  + 385.81691806 * k + 0.0107306  * T2 + 0.00001236  * T3
        val F   = 21.2964   + 390.67050646 * k - 0.0016528  * T2 - 0.00000239  * T3

        var C1  = (0.1734 - 0.000393 * T) * Math.sin(M * dr)
        C1 += 0.0021 * Math.sin(2 * M * dr)
        C1 -= 0.4068 * Math.sin(Mpr * dr)
        C1 += 0.0161 * Math.sin(2 * Mpr * dr)
        C1 -= 0.0004 * Math.sin(3 * Mpr * dr)
        C1 += 0.0104 * Math.sin(2 * F * dr)
        C1 -= 0.0051 * Math.sin((M + Mpr) * dr)
        C1 += 0.0074 * Math.sin((M - Mpr) * dr)
        C1 += 0.0004 * Math.sin((2 * F + M) * dr)
        C1 -= 0.0004 * Math.sin((2 * F - M) * dr)
        C1 -= 0.0006 * Math.sin((2 * F + Mpr) * dr)
        C1 += 0.0010 * Math.sin((2 * F - Mpr) * dr)
        C1 += 0.0005 * Math.sin((M + 2 * Mpr) * dr)

        return Jd1 + C1 - deltaT(T)
    }

    /** Vị trí mặt trời (độ kinh độ hoàng đạo) */
    private fun sunLongitude(jdn: Double): Double {
        val T  = (jdn - 2451545.0) / 36525
        val T2 = T * T
        val dr = Math.PI / 180
        var M  = 357.5291 + 35999.0503 * T - 0.0001559 * T2 - 0.00000048 * T * T2
        val L0 = 280.46646 + 36000.76983 * T + 0.0003032 * T2
        var DL = (1.9146 - 0.004817 * T - 0.000014 * T2) * Math.sin(dr * M)
        DL += (0.019993 - 0.000101 * T) * Math.sin(dr * 2 * M)
        DL += 0.00029 * Math.sin(dr * 3 * M)
        // Nhiễu loạn hành tinh (Jupiter, Saturn)
        DL += 0.000005 * Math.sin(dr * (-1.4 + 4812.679 * T))
        DL += 0.000005 * Math.sin(dr * (15.1 + 10733.587 * T))
        var L  = L0 + DL - 20.4922 / 3600
        L -= 360 * int(L / 360)
        return L
    }

    private fun getSunLongitude(dayNumber: Int, timeZone: Double): Int =
        int(sunLongitude(dayNumber - 0.5 - timeZone / 24) / 30)

    private fun getNewMoonDay(k: Int, timeZone: Double): Int =
        int(newMoon(k) + 0.5 + timeZone / 24)

    private fun getLunarMonth11(yy: Int, timeZone: Double): Int {
        val off = jdFromDate(31, 12, yy) - 2415021
        val k   = int(off / 29.530588853)
        var nm  = getNewMoonDay(k, timeZone)
        if (getSunLongitude(nm, timeZone) >= 9)
            nm = getNewMoonDay(k - 1, timeZone)
        return nm
    }

    private fun getLeapMonthOffset(a11: Int, timeZone: Double): Int {
        val k   = int((a11 - EPOCH) / 29.530588853 + 0.5)
        var i   = 1
        var arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        var last: Int
        do {
            last = arc
            i++
            arc = getSunLongitude(getNewMoonDay(k + i, timeZone), timeZone)
        } while (arc != last && i < 14)
        return i - 1
    }

    // ─── API chính ────────────────────────────────────────────────────────────

    /** Chuyển ngày dương lịch sang âm lịch */
    fun solarToLunar(dd: Int, mm: Int, yy: Int): LunarDate {
        require(mm in 1..12) { "Month must be 1–12, got $mm" }

        val tz        = TIME_ZONE_OFFSET
        val dayNumber = jdFromDate(dd, mm, yy)
        val k         = int((dayNumber - EPOCH) / 29.530588853)
        var monthStart = getNewMoonDay(k + 1, tz)
        if (monthStart > dayNumber) monthStart = getNewMoonDay(k, tz)

        var a11 = getLunarMonth11(yy, tz)
        var b11 = a11
        val lunarYear: Int
        if (a11 < monthStart) {
            lunarYear = yy
            b11 = getLunarMonth11(yy + 1, tz)
        } else {
            lunarYear = yy - 1
            b11 = a11
            a11 = getLunarMonth11(yy - 1, tz)
        }

        val lunarDay  = dayNumber - monthStart + 1
        val diff      = int((monthStart - a11) / 29.0)
        var lunarLeap = false
        var lunarMonth = diff + 11

        if (b11 - a11 > 365) {
            val leapMonthDiff = getLeapMonthOffset(a11, tz)
            if (diff > leapMonthDiff) {
                lunarMonth = diff + 10
            } else if (diff == leapMonthDiff) {
                lunarMonth = diff + 10
                lunarLeap = true
            }
        }
        lunarMonth = posMod(lunarMonth - 1, 12) + 1

        // ── Can Chi ──────────────────────────────────────────────────────────

        // Năm âm lịch (tên năm) chuyển đổi vào ngày mồng 1 tết (tháng 1)
        val canChiYear = if (lunarMonth >= 11) lunarYear else lunarYear + 1

        val canNamIdx  = posMod(canChiYear + 6, 10)
        val chiNamIdx  = posMod(canChiYear + 8, 12)

        // Tháng: dùng Long để tránh overflow; guard âm bằng posMod
        val canThangIdx = posMod((canChiYear.toLong() * 12 + lunarMonth + 3), 10)
        val chiThangIdx = posMod(lunarMonth + 1, 12)

        // Ngày: dùng posMod tránh JVM % âm
        val canNgayIdx = posMod(dayNumber + 9, 10)
        val chiNgayIdx = posMod(dayNumber + 1, 12)

        return LunarDate(
            day      = lunarDay,
            month    = lunarMonth,
            year     = canChiYear, // Trả về năm âm lịch khớp với Can Chi
            leap     = lunarLeap,
            canNam   = CAN[canNamIdx],
            chiNam   = CHI[chiNamIdx],
            canThang = CAN[canThangIdx],
            chiThang = CHI[chiThangIdx],
            canNgay  = CAN[canNgayIdx],
            chiNgay  = CHI[chiNgayIdx]
        )
    }

    /** Chuyển âm lịch → dương lịch */
    fun lunarToSolar(lunarDay: Int, lunarMonth: Int, lunarYear: Int, lunarLeap: Boolean = false): SolarDate {
        require(lunarMonth in 1..12) { "Lunar month must be 1–12, got $lunarMonth" }

        val tz = TIME_ZONE_OFFSET
        val a11: Int
        val b11: Int
        if (lunarMonth >= 11) {
            a11 = getLunarMonth11(lunarYear, tz)
            b11 = getLunarMonth11(lunarYear + 1, tz)
        } else {
            a11 = getLunarMonth11(lunarYear - 1, tz)
            b11 = getLunarMonth11(lunarYear, tz)
        }

        val baseOffset = if (lunarMonth >= 11) lunarMonth - 11 else lunarMonth + 1

        val leapOff = if (b11 - a11 > 365) getLeapMonthOffset(a11, tz) else -1
        require(!lunarLeap || leapOff >= 0) { "Năm $lunarYear không có tháng nhuận" }

        val off = when {
            leapOff < 0 -> baseOffset
            lunarLeap -> leapOff
            baseOffset > leapOff -> baseOffset + 1
            else -> baseOffset
        }

        val k = int((a11 - EPOCH) / 29.530588853 + 0.5)
        val targetNewMoon = getNewMoonDay(k + off, tz)
        val jd = targetNewMoon + lunarDay - 1
        val (sDay, sMonth, sYear) = jdToDate(jd)
        return SolarDate(sDay, sMonth, sYear)
    }

    // ─── Nâng cấp tự động hóa tháng nhuận ─────────────────────────────────

    fun getLeapMonthInYear(lunarYear: Int): Int {
        val tz = TIME_ZONE_OFFSET
        val a11 = getLunarMonth11(lunarYear - 1, tz)
        val b11 = getLunarMonth11(lunarYear, tz)
        if (b11 - a11 > 365) {
            val leapMonthDiff = getLeapMonthOffset(a11, tz)
            var leapMonth = leapMonthDiff + 11
            if (leapMonth > 12) leapMonth -= 12
            return leapMonth
        }
        return 0
    }

    fun getValidMonthsInYear(lunarYear: Int): List<Pair<Int, Boolean>> {
        val list = mutableListOf<Pair<Int, Boolean>>()
        val leapMonth = getLeapMonthInYear(lunarYear)
        for (m in 1..12) {
            list.add(Pair(m, false))
            if (m == leapMonth) {
                list.add(Pair(m, true))
            }
        }
        return list
    }

    fun daysInLunarMonth(lunarMonth: Int, lunarYear: Int, lunarLeap: Boolean = false): Int {
        val solarDate30 = lunarToSolar(30, lunarMonth, lunarYear, lunarLeap)
        val backToLunar = solarToLunar(solarDate30.day, solarDate30.month, solarDate30.year)
        return if (backToLunar.day == 30 && backToLunar.month == lunarMonth && backToLunar.leap == lunarLeap) {
            30
        } else {
            29
        }
    }

    /** Lấy thông tin ngày hôm nay */
    fun today(): Pair<Calendar, LunarDate> {
        val cal = Calendar.getInstance()
        val dd  = cal.get(Calendar.DAY_OF_MONTH)
        val mm  = cal.get(Calendar.MONTH) + 1
        val yy  = cal.get(Calendar.YEAR)
        return Pair(cal, solarToLunar(dd, mm, yy))
    }

    /** Tên thứ đầy đủ (Thứ Hai … Chủ Nhật) */
    fun dayOfWeekFull(cal: Calendar): String  = THU[cal.get(Calendar.DAY_OF_WEEK) - 1]

    /** Tên thứ viết tắt (T2 … CN) */
    fun dayOfWeekShort(cal: Calendar): String = THU_VIET[cal.get(Calendar.DAY_OF_WEEK) - 1]

    /** Tên tháng dương lịch */
    fun solarMonthName(month: Int): String {
        val names = listOf(
            "THÁNG MỘT","THÁNG HAI","THÁNG BA","THÁNG TƯ",
            "THÁNG NĂM","THÁNG SÁU","THÁNG BẢY","THÁNG TÁM",
            "THÁNG CHÍN","THÁNG MƯỜI","THÁNG MƯỜI MỘT","THÁNG MƯỜI HAI"
        )
        return if (month in 1..12) names[month - 1] else "THÁNG $month"
    }

    /** Số ngày trong tháng dương lịch */
    fun daysInSolarMonth(month: Int, year: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    /** Offset ngày đầu tuần của ngày 1 trong tháng (0=T2 … 6=CN) */
    fun firstDayOfWeekOffset(month: Int, year: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        return (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    }
}