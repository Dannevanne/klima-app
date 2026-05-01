package no.uio.ifi.in2000.team45.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.data.settings.SoundPreferenceRepository

class SoundViewModel (
    private val soundPreferenceRepository: SoundPreferenceRepository
) : ViewModel() {

    val soundEnabledFlow = soundPreferenceRepository.soundEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            soundPreferenceRepository.saveSoundEnabled(enabled)
        }
    }
}