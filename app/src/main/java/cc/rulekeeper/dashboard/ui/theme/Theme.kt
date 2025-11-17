package cc.rulekeeper.dashboard.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = RuleKeeperLightBlue,
    onPrimary = TextPrimaryDark,
    primaryContainer = RuleKeeperDarkBlue,
    onPrimaryContainer = TextPrimaryDark,
    secondary = DiscordBlurple,
    onSecondary = TextPrimaryDark,
    tertiary = DiscordGreen,
    onTertiary = TextPrimaryDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondaryDark,
    error = DiscordRed,
    onError = TextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = RuleKeeperBlue,
    onPrimary = SurfaceLight,
    primaryContainer = RuleKeeperLightBlue,
    onPrimaryContainer = TextPrimary,
    secondary = DiscordBlurple,
    onSecondary = SurfaceLight,
    tertiary = DiscordGreen,
    onTertiary = TextPrimary,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextSecondary,
    error = DiscordRed,
    onError = SurfaceLight
)

@Composable
fun RuleKeeperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
