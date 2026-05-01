package no.uio.ifi.in2000.team45

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.mapbox.common.MapboxOptions
import no.uio.ifi.in2000.team45.data.map.MapCameraRepository
import no.uio.ifi.in2000.team45.ui.map.MapViewModel
import no.uio.ifi.in2000.team45.ui.map.useDarkThemeFrom
import no.uio.ifi.in2000.team45.ui.theme.Team45Theme
import no.uio.ifi.in2000.team45.data.settings.ThemePreferenceRepository
import no.uio.ifi.in2000.team45.ui.map.MapViewModelFactory
import no.uio.ifi.in2000.team45.data.settings.LanguagePreferenceRepository
import no.uio.ifi.in2000.team45.ui.AppRoot
import no.uio.ifi.in2000.team45.ui.settings.applyAppLanguage
import androidx.compose.runtime.collectAsState
import no.uio.ifi.in2000.team45.data.settings.MusicPreferenceRepository
import no.uio.ifi.in2000.team45.ui.settings.MusicViewModel
import no.uio.ifi.in2000.team45.ui.settings.MusicViewModelFactory
import no.uio.ifi.in2000.team45.data.music.MusicManager
import no.uio.ifi.in2000.team45.data.settings.SoundPreferenceRepository
import no.uio.ifi.in2000.team45.ui.settings.SoundViewModel
import no.uio.ifi.in2000.team45.ui.settings.SoundViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var musicManager: MusicManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        musicManager = MusicManager(applicationContext)

        // Creates ThemePreferenceRepository for the MapViewModelFactory
        val themePreferenceRepository = ThemePreferenceRepository(applicationContext)

        // Creates LanguagePreferenceRepository for the MapViewModelFactory
        val languagePreferenceRepository = LanguagePreferenceRepository(applicationContext)

        val cameraRepository = MapCameraRepository(applicationContext)
        val musicPreferenceRepository = MusicPreferenceRepository(applicationContext)
        val soundPreferenceRepository = SoundPreferenceRepository(applicationContext)

        // MapBox token must be set before the map is used in app
        MapboxOptions.accessToken = getString(R.string.mapbox_access_token)

        setContent {
            val mapViewModel: MapViewModel = viewModel(
                factory = MapViewModelFactory(
                    themePreferenceRepository = themePreferenceRepository,
                    languagePreferenceRepository = languagePreferenceRepository,
                    cameraRepository = cameraRepository
                )
            )

            val musicViewModel: MusicViewModel = viewModel(
                factory = MusicViewModelFactory(
                    musicPreferenceRepository = musicPreferenceRepository
                )
            )

            val soundViewModel: SoundViewModel = viewModel(
                factory = SoundViewModelFactory(
                    soundPreferenceRepository = soundPreferenceRepository
                )
            )

            val musicEnabled by musicViewModel.musicEnabledFlow.collectAsState(initial = false)

            val soundEnabled by soundViewModel.soundEnabledFlow.collectAsState(initial = true)

            LaunchedEffect(musicEnabled) {
                musicManager.updateMusicState(musicEnabled)
            }

            LaunchedEffect(mapViewModel.selectedLanguage, mapViewModel.isLanguageInitialized) {
                if (mapViewModel.isLanguageInitialized) {
                    applyAppLanguage(mapViewModel.selectedLanguage)
                }
            }

            // Collects the users theme preference to use for Theme45Theme
            val useDarkTheme = useDarkThemeFrom(
                theme = mapViewModel.appTheme,
                systemIsDark = isSystemInDarkTheme()
            )

            Team45Theme(darkTheme = useDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot(
                        mapViewModel = mapViewModel,
                        musicEnabled = musicEnabled,
                        onMusicEnabledChanged = { enabled ->
                            musicViewModel.updateMusicEnabled(enabled)
                        },
                        soundEnabled = soundEnabled,
                        onSoundEnabledChanged = { soundViewModel.updateSoundEnabled(it) }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        musicManager.release()
    }
}