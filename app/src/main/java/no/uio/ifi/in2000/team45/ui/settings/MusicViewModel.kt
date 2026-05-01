package no.uio.ifi.in2000.team45.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.data.settings.MusicPreferenceRepository

class MusicViewModel(
    private val musicPreferenceRepository: MusicPreferenceRepository
) : ViewModel() {

    val musicEnabledFlow = musicPreferenceRepository.musicEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun updateMusicEnabled(enabled: Boolean) {
        viewModelScope.launch {
            musicPreferenceRepository.saveMusicEnabled(enabled)
        }
    }
}