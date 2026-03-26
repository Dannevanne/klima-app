package no.uio.ifi.in2000.team45.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.uio.ifi.in2000.team45.ui.map.MapTheme

private val Context.dataStore by preferencesDataStore(name = "user_settings")
class ThemePreferenceRepository(private val context: Context){
    companion object{
        private val MAP_THEME_KEY = stringPreferencesKey("map_theme")
    }

    val mapThemeFlow: Flow<MapTheme> = context.dataStore.data.map{ preferences ->
        when(preferences[MAP_THEME_KEY]){
            MapTheme.DARK.name -> MapTheme.DARK
            MapTheme.LIGHT.name -> MapTheme.LIGHT
            else -> MapTheme.DEVICE
        }
    }

    suspend fun saveMapTheme(theme: MapTheme){
        context.dataStore.edit { preferences ->
            preferences[MAP_THEME_KEY] = theme.name
        }
    }
}