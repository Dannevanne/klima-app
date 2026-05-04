package no.uio.ifi.in2000.team45.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// primary is the brand accent (Material Blue 700/200) so default Buttons read
// as a real button -- previously primary == onPrimary collapsed Button text
// to invisible. surface/background colours are unchanged.
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