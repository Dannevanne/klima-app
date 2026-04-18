package no.uio.ifi.in2000.team45

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mapbox.common.MapboxOptions
import no.uio.ifi.in2000.team45.ui.AppNavigation
import no.uio.ifi.in2000.team45.ui.map.MapViewModel
import no.uio.ifi.in2000.team45.ui.map.useDarkThemeFrom
import no.uio.ifi.in2000.team45.ui.theme.Team45Theme
import no.uio.ifi.in2000.team45.data.settings.ThemePreferenceRepository
import no.uio.ifi.in2000.team45.ui.map.MapViewModelFactory
import no.uio.ifi.in2000.team45.data.settings.LanguagePreferenceRepository
import no.uio.ifi.in2000.team45.ui.settings.applyAppLanguage

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Creates ThemePreferenceRepository for the MapViewModelFactory
        val themePreferenceRepository = ThemePreferenceRepository(applicationContext)

        // Creates LanguagePreferenceRepository for the MapViewModelFactory
        val languagePreferenceRepository = LanguagePreferenceRepository(applicationContext)

        // MapBox token must be set before the map is used in app
        MapboxOptions.accessToken = getString(R.string.mapbox_access_token)

        setContent {
            val mapViewModel: MapViewModel = viewModel(
                factory = MapViewModelFactory(
                    themePreferenceRepository = themePreferenceRepository,
                    languagePreferenceRepository = languagePreferenceRepository)
            )

            LaunchedEffect(mapViewModel.selectedLanguage, mapViewModel.isLanguageInitialized) {
                if (mapViewModel.isLanguageInitialized) {
                    applyAppLanguage(mapViewModel.selectedLanguage)
                }
            }

            // Collects the users theme preference to use for Theme45Theme
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