package no.uio.ifi.in2000.team45.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "sound_settings")

/**
 * Repository responsible for storing and retrieving the user's sound preference
 * using DataStore.
 */
class SoundPreferenceRepository (private val context: Context) {

    companion object {
        private val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
    }

    /**
     * Flow indicating whether sound is enabled in the application.
     *
     * @return A Flow emitting the current sound enabled state, defaulting to true.
     */
    val soundEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SOUND_ENABLED_KEY] ?: true
    }

    /**
     * Saves the user's sound enabled preference.
     *
     * @param enabled True if sound should be enabled, false otherwise.
     */
    suspend fun saveSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_ENABLED_KEY] = enabled
        }
    }
}