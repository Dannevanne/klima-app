package no.uio.ifi.in2000.team45.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.uio.ifi.in2000.team45.ui.map.MapTheme

// Creates an extension property, a data store connected to Context.
// DataStore is Androids solution to save easy settings.

// Context is what provides access to the global information about the applications
// environment, and allows the code to interact with system-level services.
private val Context.dataStore by preferencesDataStore(name = "user_settings")
class ThemePreferenceRepository(private val context: Context){
    companion object{
        // This key is used to save, and fetch, the selected theme in DataStore.
        // This is a permanent key which represents the selected theme.
        private val MAP_THEME_KEY = stringPreferencesKey("map_theme")
    }

    // The theme gets exposed as a flow.
    // This means that other parts of the app can observe the value over time.
    // ViewModel can automatically get the new value if the saved setting changes.
    // DataStore has saved the selected theme as a String, so this gets translated
    // to the apps enum MapTheme.
    val mapThemeFlow: Flow<MapTheme> = context.dataStore.data.map{ preferences ->
        when(preferences[MAP_THEME_KEY]){
            MapTheme.DARK.name -> MapTheme.DARK
            MapTheme.LIGHT.name -> MapTheme.LIGHT
            else -> MapTheme.DEVICE // If the user hasn't selected a theme
                                    // or something has gone wrong,
                                    // it will use the device settings.
        }
    }

    // Function for when the user selects a new theme.
    // The repository saves the selected theme permanently
    // so the ViewModel doesn't need to know how the saving happens,
    // and just has the knowledge that there is a function to save the theme.
    suspend fun saveMapTheme(theme: MapTheme){
        context.dataStore.edit { preferences ->
            preferences[MAP_THEME_KEY] = theme.name // E.g MapTheme.DARK
                                                    // gets saved as "DARK"
        }
    }
}