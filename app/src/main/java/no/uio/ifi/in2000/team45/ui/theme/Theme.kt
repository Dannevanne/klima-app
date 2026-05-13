package no.uio.ifi.in2000.team45.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Dark color scheme for the application theme.
 *
 * Defines color roles for dark mode, ensuring sufficient contrast for UI elements
 * such as buttons, surfaces, backgrounds, and text. The primary color is used as
 * the brand accent, while surface and background colors are tuned for readability
 * in low-light environments.
 */
private val DarkColorScheme = darkColorScheme(
    primary = LoadingBlueLight,
    secondary = DarkOnSurface,
    tertiary = StarGold,

    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    surfaceContainerLow = DarkBar,
    surfaceContainerHigh = DarkBar,
    surfaceContainer = DarkBox,

    primaryContainer = DarkSurface,
    secondaryContainer = DarkSurface,
    tertiaryContainer = DarkSurface,

    onPrimary = DarkBackground,
    onSecondary = DarkOnSurface,
    onTertiary = DarkOnSurface,

    outline = IconWhite,
    outlineVariant = IconBlack
)

/**
 * Light color scheme for the application theme.
 *
 * Defines color roles for light mode, optimized for readability in bright environments.
 * Maintains consistent branding via the primary color while adjusting surfaces and text
 * for appropriate contrast in light UI contexts.
 */
private val LightColorScheme = lightColorScheme(
    primary = LoadingBlue,
    secondary = LightOnSurface,
    tertiary = StarGold,

    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    surfaceContainerLow = LightBar,
    surfaceContainerHigh = LightBar,
    surfaceContainer = LightBox,

    primaryContainer = LightSurface,
    secondaryContainer = LightSurface,
    tertiaryContainer = LightSurface,

    onPrimary = LightSurface,
    onSecondary = LightOnSurface,
    onTertiary = LightOnSurface,

    outline = IconBlack,
    outlineVariant = IconWhite
)

@Composable
fun Team45Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit){
    val colorScheme = if(darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme,
        typography = Typography,
        content = content)
}