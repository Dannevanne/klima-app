package no.uio.ifi.in2000.team45.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

// Pins font scaling to 1x for the wrapped subtree. Use on small fixed-height
// chrome (bottom bar labels, zoom buttons) where huge system fonts would
// otherwise overflow the container.
@Composable
fun UnscaledFont(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val pinned = Density(density = density.density, fontScale = 1f)
    CompositionLocalProvider(LocalDensity provides pinned) {
        content()
    }
}
