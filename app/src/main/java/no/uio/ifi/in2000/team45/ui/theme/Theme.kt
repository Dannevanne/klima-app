package no.uio.ifi.in2000.team45.ui.theme

import android.R
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkOnSurface,
    secondary = DarkOnSurface,
    tertiary = StarGold,

    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,

    primaryContainer = DarkSurface,
    secondaryContainer = DarkSurface,
    tertiaryContainer = DarkSurface,

    onPrimary = DarkOnSurface,
    onSecondary = DarkOnSurface,
    onTertiary = DarkOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = LightOnSurface,
    secondary = LightOnSurface,
    tertiary = StarGold,

    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,

    primaryContainer = LightSurface,
    secondaryContainer = LightSurface,
    tertiaryContainer = LightSurface,

    onPrimary = LightOnSurface,
    onSecondary = LightOnSurface,
    onTertiary = LightOnSurface
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