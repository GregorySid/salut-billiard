package com.salut.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.salut.domain.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    secondary = GreenGrey80,
    tertiary = Teal80,
    // Шапка тёмной темы — тёмная, а не зелёная
    primaryContainer = DarkHeader,
    onPrimaryContainer = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = BordeauxAccent,
    onPrimary = Color.White,
    // Шапка светлой темы — бордовая #800000 (контент и фон светлые)
    primaryContainer = Bordo,
    onPrimaryContainer = Color.White,
    secondary = BordeauxSecondary,
    onSecondary = Color.White,
    secondaryContainer = BordeauxContainer,
    onSecondaryContainer = OnBordeauxContainer,
    tertiary = BordeauxTertiary,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightBackground,
    onSurface = LightOnBackground,
    surfaceVariant = LightCard,
    onSurfaceVariant = LightOnCard,
    outline = LightOutline,
)

@Composable
fun SalutTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                // Шапка бордовая и в светлой теме -> иконки статус-бара светлые
                isAppearanceLightStatusBars = false
                // Фон светлой темы светлый -> иконки навигации тёмные; в тёмной теме — светлые
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
