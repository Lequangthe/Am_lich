package com.quangthe.amlich.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD4AA8C),
    onPrimary = Color(0xFF2A1A0E),
    primaryContainer = Color(0xFF3D2B1C),
    onPrimaryContainer = Color(0xFFD4AA8C),
    secondary = Color(0xFFBBB8B4),
    onSecondary = Color(0xFF1C1B19),
    secondaryContainer = Color(0xFF3A3836),
    onSecondaryContainer = Color(0xFFBBB8B4),
    tertiary = Color(0xFFFF8C84),
    onTertiary = Color(0xFF2C0002),
    tertiaryContainer = Color(0xFF5A0007),
    onTertiaryContainer = Color(0xFFFF8C84),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1410),
    onBackground = Color(0xFFEDE0D5),
    surface = Color(0xFF1A1410),
    onSurface = Color(0xFFEDE0D5),
    surfaceVariant = Color(0xFF2E2420),
    onSurfaceVariant = Color(0xFFBBAFA8),
    outline = Color(0xFF8A7F78),
    outlineVariant = Color(0xFF3D332D),
)

@Composable
fun AmLichTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
