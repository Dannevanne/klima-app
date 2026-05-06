package no.uio.ifi.in2000.team45.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import no.uio.ifi.in2000.team45.ui.logo.LogoScreen
import no.uio.ifi.in2000.team45.ui.map.MapViewModel
import no.uio.ifi.in2000.team45.ui.onboarding.OnboardingScreen
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

@Composable
fun AppRoot(
    mapViewModel: MapViewModel,
    onboardingCompleted: Boolean?,
    onOnboardingFinished: () -> Unit,
    musicEnabled: Boolean,
    onMusicEnabledChanged: (Boolean) -> Unit,
    soundEnabled: Boolean,
    onSoundEnabledChanged: (Boolean) -> Unit
) {
    var showLoadingScreen by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1500)
        showLoadingScreen = false
    }

    when {
        showLoadingScreen || onboardingCompleted == null -> LogoScreen()
        !onboardingCompleted -> OnboardingScreen(onFinished = onOnboardingFinished)
        else -> AppNavigation(
            mapViewModel = mapViewModel,
            musicEnabled = musicEnabled,
            onMusicEnabledChanged = onMusicEnabledChanged,
            soundEnabled = soundEnabled,
            onSoundEnabledChanged = onSoundEnabledChanged
        )
    }
}