package no.uio.ifi.in2000.team45.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import no.uio.ifi.in2000.team45.data.settings.ThemePreferenceRepository

class MapViewModelFactory(
    private val themePreferenceRepository: ThemePreferenceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MapViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(
                themePreferenceRepository = themePreferenceRepository
            ) as T
        }
        throw IllegalArgumentException("Invalid ViewModel class")
    }
}