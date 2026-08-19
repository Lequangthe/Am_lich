# Walkthrough - Lunar Calendar Calculation & Widget Fix

I have fixed the errors in the lunar calendar calculation and resolved the widget displaying "--" due to exceptions.

## Changes Made

### LunarCalendarUtils.kt
- **Fixed `deltaT`**: Corrected the century reference year from 2000 to 1900. Updated the formulas for different year ranges to provide higher precision astronomical time correction.
- **Fixed `newMoon`**: Corrected the signs in the astronomical perturbation terms (specifically the `M - Mpr` term).
- **Improved Month Normalization**: Replaced basic `if` logic with robust `posMod` and leap month awareness to ensure `lunarMonth` is always in the range 1–12.
- **Unified Year Logic**: Updated `LunarDate.year` to return the actual Lunar Year number (the year of Tet), ensuring it matches the Can Chi name.
- **Added Index Safety**: Applied `posMod` to all `CAN` and `CHI` array accesses to prevent `ArrayIndexOutOfBoundsException`.

### Automated Tests
- Created [LunarCalendarUtilsTest.kt](file:///D:/AndroidStudioProjects/AM_LICH/app/src/test/java/com/quangthe/amlich/LunarCalendarUtilsTest.kt) to verify:
    - Today's date (2026-08-19 -> 07/07 Bính Ngọ)
    - Lunar New Year 2024 (2024-02-10 -> 01/01 Giáp Thìn)
    - Leap month 2023 (2023-03-22 -> 01/02 nhuận Quý Mão)

## Verification Results

### Unit Tests
- `testDebugUnitTest`: **Passed** (3 tests)

### Manual Verification
- **App UI**: Verified on device that 2026-08-19 displays as `07 / 07 Bính Ngọ`.
- **Widget**: Verified that the widget correctly displays the lunar date instead of "--".

![App Screenshot](file:///D:/AndroidStudioProjects/AM_LICH/.artifacts/1eda2159-a346-438d-9ef7-8390c70f539d/screenshot_app.png)
*(Note: Screenshot captured during execution shows correct date 07/07 Bính Ngọ)*
