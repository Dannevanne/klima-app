package no.uio.ifi.in2000.team45.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import no.uio.ifi.in2000.team45.ui.logo.LogoScreen
import no.uio.ifi.in2000.team45.ui.map.MapViewModel
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

@Composable
fun AppRoot(
    mapViewModel: MapViewModel,
    musicEnabled: Boolean,
    onMusicEnabledChanged: (Boolean) -> Unit
) {
    var showLoadingScreen by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1500)
        showLoadingScreen = false
    }

    if (showLoadingScreen) {
        LogoScreen()
    } else {
        AppNavigation(mapViewModel = mapViewModel,
            musicEnabled = musicEnabled,
            onMusicEnabledChanged = onMusicEnabledChanged)
    }
}