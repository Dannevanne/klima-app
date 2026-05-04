package no.uio.ifi.in2000.team45.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.uio.ifi.in2000.team45.ui.theme.AppTheme

private val Context.dataStore by preferencesDataStore(name = "user_settings")

class ThemePreferenceRepository(private val context: Context) {
    companion object {
        // Key kept as "map_theme" so existing installations don't lose their preference.
        private val THEME_KEY = stringPreferencesKey("map_theme")
    }

    val appThemeFlow: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        when (preferences[THEME_KEY]) {
            AppTheme.DARK.name -> AppTheme.DARK
            AppTheme.LIGHT.name -> AppTheme.LIGHT
            else -> AppTheme.DEVICE
        }
    }

    suspend fun saveAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }
}
