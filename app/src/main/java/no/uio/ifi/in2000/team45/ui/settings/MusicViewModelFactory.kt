package no.uio.ifi.in2000.team45.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import no.uio.ifi.in2000.team45.data.settings.MusicPreferenceRepository

class MusicViewModelFactory(
    private val musicPreferenceRepository: MusicPreferenceRepository
) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(
                musicPreferenceRepository = musicPreferenceRepository
            ) as T
        }
        throw IllegalArgumentException("Invalid ViewModel class")
    }
}