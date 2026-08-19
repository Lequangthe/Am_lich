# Fix Lunar Calendar Calculation and Widget Errors

This plan addresses the widget displaying "--" due to exceptions, and corrects multiple errors in the lunar calendar algorithm, including sign errors in `newMoon`, century reference errors in `deltaT`, and year/month normalization issues.

## User Review Required

> [!IMPORTANT]
> The `LunarDate.year` property will now return the actual Lunar Year number (e.g., 2024 for Giáp Thìn) instead of the base solar year used for calculations. This ensures consistency with the Can Chi name.

## Proposed Changes

### Core Logic

#### [MODIFY] [LunarCalendarUtils.kt](file:///D:/AndroidStudioProjects/AM_LICH/app/src/main/java/com/quangthe/amlich/LunarCalendarUtils.kt)

- **Fix `deltaT`**: Change the century reference year from 2000 to 1900 to match the `T` century offset from the 1900 epoch.
- **Fix `newMoon` signs**: Review and correct the signs in the `newMoon` calculation to ensure high precision. (Verified against Jean Meeus / Ho Ngoc Duc algorithms).
- **Robust Normalization**: Replace `if` statements with `posMod` or loops for `lunarMonth` to prevent negative values.
- **Year Logic**: Update `solarToLunar` to return the correct Lunar Year number that matches the Can Chi name (changing at Lunar New Year).
- **Index Safety**: Use `posMod` for all `CAN` and `CHI` array accesses to prevent `ArrayIndexOutOfBoundsException`.

---

## Verification Plan

### Automated Tests
- I will verify the lunar date for known edge cases (e.g., Lunar New Year 2024, leap months in 2023).
- I will run the app to ensure the widget no longer shows "--".

### Manual Verification
- Deploy to device and check the widget display.
- Compare lunar dates with a reliable source (e.g., Ho Ngoc Duc's calendar).
