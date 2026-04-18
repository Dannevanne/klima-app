package no.uio.ifi.in2000.team45.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.uio.ifi.in2000.team45.ui.settings.LanguageOption

private val Context.languageDataStore by preferencesDataStore(name = "language_settings")

class LanguagePreferenceRepository(private val context: Context) {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
    }

    val languageFlow: Flow<LanguageOption> = context.languageDataStore.data.map { preferences ->
        when (preferences.[LANGUAGE_KEY]) {
            LanguageOption.BOKMÅL.name -> LanguageOption.BOKMÅL
            LanguageOption.NYNORSK.name -> LanguageOption.NYNORSK
            LanguageOption.ENGLISH.name -> LanguageOption.ENGLISH
            else -> LanguageOption.ENGLISH
        }
    }

    suspend fun saveLanguage(language: LanguageOption) {
        context.languageDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.name
        }
    }
}