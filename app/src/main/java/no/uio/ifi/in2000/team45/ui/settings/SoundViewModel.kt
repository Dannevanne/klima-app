package no.uio.ifi.in2000.team45.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.data.settings.SoundPreferenceRepository

/**
 * ViewModel responsible for managing sound settings in the app.
 *
 * @property soundPreferenceRepository repository used to read and store sound preferences.
 */
class SoundViewModel (
    private val soundPreferenceRepository: SoundPreferenceRepository
) : ViewModel() {

    val soundEnabledFlow = soundPreferenceRepository.soundEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    /**
     * Updates the sound enabled setting and sends it to the repository for saving.
     *
     * @param enabled whether sound effects should be enabled or disabled.
     */
    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            soundPreferenceRepository.saveSoundEnabled(enabled)
        }
    }
}