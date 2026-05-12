package no.uio.ifi.in2000.team45.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import no.uio.ifi.in2000.team45.data.map.MapCameraRepository
import no.uio.ifi.in2000.team45.data.settings.ThemePreferenceRepository
import no.uio.ifi.in2000.team45.data.settings.LanguagePreferenceRepository

/**
 * Factory for creating MapViewModel with required dependencies.
 *
 * Used because MapViewModel cannot be constructed by default ViewModelProvider.
 * Allows injection of repositories from Activity or DI layer.
 *
 * @param themePreferenceRepository Provides theme storage.
 * @param languagePreferenceRepository Provides language storage.
 * @param cameraRepository Provides camera state storage.
 */
class MapViewModelFactory(
    private val themePreferenceRepository: ThemePreferenceRepository,
    private val languagePreferenceRepository: LanguagePreferenceRepository,
    private val cameraRepository: MapCameraRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Checks which ViewModel was requested.
        // ViewModel is only created if a MapViewModel was requested.
        // If requested, MapViewModel is created with correct dependency.
        if(modelClass.isAssignableFrom(MapViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(
                themePreferenceRepository = themePreferenceRepository,
                languagePreferenceRepository = languagePreferenceRepository,
                cameraRepository = cameraRepository
            ) as T
        }
        throw IllegalArgumentException("Invalid ViewModel class")
    }
}