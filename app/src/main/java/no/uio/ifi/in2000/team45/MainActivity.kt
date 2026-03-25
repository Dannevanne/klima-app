package no.uio.ifi.in2000.team45

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import no.uio.ifi.in2000.team45.ui.AppNavigation
import no.uio.ifi.in2000.team45.ui.map.MapViewModel
import no.uio.ifi.in2000.team45.ui.map.useDarkThemeFrom
import no.uio.ifi.in2000.team45.ui.theme.Team45Theme
import no.uio.ifi.in2000.team45.data.settings.ThemePreferenceRepository
import no.uio.ifi.in2000.team45.ui.map.MapViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val themePreferenceRepository = ThemePreferenceRepository(applicationContext)
        setContent {
            val mapViewModel: MapViewModel = viewModel(
                factory = MapViewModelFactory(themePreferenceRepository)
            )
            val useDarkTheme = useDarkThemeFrom(
                theme = mapViewModel.mapTheme,
                systemIsDark = isSystemInDarkTheme()
            )
            Team45Theme(darkTheme = useDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        modifier = Modifier.fillMaxSize(),
                        mapViewModel = mapViewModel
                    )
                }
            }
        }
    }
}