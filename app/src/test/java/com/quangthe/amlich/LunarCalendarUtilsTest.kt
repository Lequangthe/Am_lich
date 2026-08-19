package com.quangthe.amlich

import org.junit.Assert.assertEquals
import org.junit.Test

class LunarCalendarUtilsTest {

    @Test
    fun testSolarToLunar_Today() {
        // 2026-08-19 -> 2026-07-07 (Bính Ngọ)
        val lunar = LunarCalendarUtils.solarToLunar(19, 8, 2026)
        assertEquals(7, lunar.day)
        assertEquals(7, lunar.month)
        assertEquals(2026, lunar.year)
        assertEquals("Bính Ngọ", lunar.tenNam)
    }

    @Test
    fun testSolarToLunar_Tet2024() {
        // 2024-02-10 -> 2024-01-01 (Giáp Thìn)
        val lunar = LunarCalendarUtils.solarToLunar(10, 2, 2024)
        assertEquals(1, lunar.day)
        assertEquals(1, lunar.month)
        assertEquals(2024, lunar.year)
        assertEquals("Giáp Thìn", lunar.tenNam)
    }

    @Test
    fun testSolarToLunar_LeapMonth2023() {
        // 2023-03-22 -> 2023-02-01 (Nhuận) (Quý Mão)
        val lunar = LunarCalendarUtils.solarToLunar(22, 3, 2023)
        assertEquals(1, lunar.day)
        assertEquals(2, lunar.month)
        assertEquals(true, lunar.leap)
        assertEquals(2023, lunar.year)
        assertEquals("Quý Mão", lunar.tenNam)
    }
}
