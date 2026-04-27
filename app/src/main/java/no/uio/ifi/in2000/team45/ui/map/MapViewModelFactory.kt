package no.uio.ifi.in2000.team45.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import no.uio.ifi.in2000.team45.data.map.MapCameraRepository
import no.uio.ifi.in2000.team45.data.settings.ThemePreferenceRepository
import no.uio.ifi.in2000.team45.data.settings.LanguagePreferenceRepository

// Factory is needed because our MapViewModel needs dependency.
// The MapViewModel therefore can't be made automatically with Androids ViewModel system.
// MainActivity can now create a repository once, and send it in to this factory.
// The regular MapViewModel doesn't need to know how the repository was made.
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