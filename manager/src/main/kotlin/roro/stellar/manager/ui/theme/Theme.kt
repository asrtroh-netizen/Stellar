package roro.stellar.manager.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

val Color.glassPanel: Color
    get() = copy(alpha = 0.72f)

private val OneInk = Color(0xFF1A1B20)

// One 家族中性色。不跟壁纸动态取色，强调色只留黑白灰。
private val LightColorScheme = lightColorScheme(
    primary = OneInk,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF0F0F4),
    onPrimaryContainer = OneInk,
    secondary = Color(0xFF575E71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8E8EF),
    onSecondaryContainer = Color(0xFF141B2C),
    tertiary = Color(0xFF5F5E62),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8E8EF),
    onTertiaryContainer = Color(0xFF1B1B1F),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFF9F9FF),
    onBackground = OneInk,
    surface = Color(0xFFF9F9FF),
    onSurface = OneInk,
    surfaceVariant = Color(0xFFE2E2E9),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
    surfaceTint = Color(0xFF74777F),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = OneInk,
    primaryContainer = Color(0xFF2B2930),
    onPrimaryContainer = Color(0xFFE2E2E9),
    secondary = Color(0xFFC8C5D0),
    onSecondary = Color(0xFF2A3042),
    secondaryContainer = Color(0xFF3F4759),
    onSecondaryContainer = Color(0xFFE4E7F6),
    tertiary = Color(0xFFC8C5D0),
    onTertiary = Color(0xFF303034),
    tertiaryContainer = Color(0xFF48464C),
    onTertiaryContainer = Color(0xFFE5E1E6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474F),
    surfaceTint = Color(0xFF8E9099),
)

@Composable
fun StellarTheme(
    themeMode: ThemeMode = ThemePreferences.themeMode.value,
    content: @Composable () -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.AUTO -> systemInDarkTheme
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val shapes = Shapes(
        extraSmall = RoundedCornerShape(10.dp),
        small = RoundedCornerShape(14.dp),
        medium = RoundedCornerShape(18.dp),
        large = RoundedCornerShape(22.dp),
        extraLarge = RoundedCornerShape(32.dp),
    )
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        content = content
    )
}
