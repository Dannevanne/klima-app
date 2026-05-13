package no.uio.ifi.in2000.team45.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * Composable that forces a fixed font scale, ignoring the system font scaling setting.
 *
 * @param content Composable content that will be rendered with a fixed font scale.
 */
@Composable
fun UnscaledFont(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val pinned = Density(density = density.density, fontScale = 1f)
    CompositionLocalProvider(LocalDensity provides pinned) {
        content()
    }
}
