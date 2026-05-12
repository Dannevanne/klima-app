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

/**
 * Root composable responsible for app startup flow and initial screen selection.
 *
 * @param mapViewModel ViewModel managing map-related state and interactions.
 * @param onboardingCompleted Whether the onboarding process has been completed.
 * @param onOnboardingFinished Callback invoked when onboarding is completed.
 * @param soundEnabled Current sound enabled setting.
 * @param onSoundEnabledChanged Callback for updating sound preference state.
 */
@Composable
fun AppRoot(
    mapViewModel: MapViewModel,
    onboardingCompleted: Boolean?,
    onOnboardingFinished: () -> Unit,
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
            soundEnabled = soundEnabled,
            onSoundEnabledChanged = onSoundEnabledChanged
        )
    }
}