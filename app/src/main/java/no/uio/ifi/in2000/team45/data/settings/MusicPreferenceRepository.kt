package no.uio.ifi.in2000.team45.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "music_settings")

class MusicPreferenceRepository(private val context: Context) {
    companion object {
        private val MUSIC_ENABLED_KEY = booleanPreferencesKey("music_enabled")
        private val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
    }

    val musicEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[MUSIC_ENABLED_KEY] ?: false
    }

    val soundEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SOUND_ENABLED_KEY] ?: true
    }

    suspend fun saveMusicEnabled(enabled: Boolean){
        context.dataStore.edit { preferences ->
            preferences[MUSIC_ENABLED_KEY] = enabled
        }
    }

    suspend fun saveSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_ENABLED_KEY] = enabled
        }
    }
}