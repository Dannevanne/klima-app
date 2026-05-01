package no.uio.ifi.in2000.team45.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import no.uio.ifi.in2000.team45.data.settings.MusicPreferenceRepository
import no.uio.ifi.in2000.team45.data.settings.SoundPreferenceRepository

class SoundViewModelFactory (
    private val soundPreferenceRepository: SoundPreferenceRepository
) : ViewModelProvider.Factory{
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SoundViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SoundViewModel(
                soundPreferenceRepository = soundPreferenceRepository
            ) as T
        }
        throw IllegalArgumentException("Invalid ViewModel class")
    }
}