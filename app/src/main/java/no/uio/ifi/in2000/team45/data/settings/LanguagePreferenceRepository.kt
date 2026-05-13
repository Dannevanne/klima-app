package no.uio.ifi.in2000.team45.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import no.uio.ifi.in2000.team45.data.settings.models.LanguageOption
import java.util.Locale

private val Context.languageDataStore by preferencesDataStore(name = "language_settings")

/**
 * Repository responsible for storing and retrieving the user's selected app language
 * using DataStore persistence.
 */
class LanguagePreferenceRepository(private val context: Context) {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")

        /**
         * Returns a sensible default language based on the device system locale.
         *
         * @return The default LanguageOption derived from the system locale.
         */
        fun systemDefault(): LanguageOption = when (Locale.getDefault().language) {
            "nb", "no" -> LanguageOption.BOKMAL
            "smi" -> LanguageOption.SAMI
            "nn" -> LanguageOption.NYNORSK
            else -> LanguageOption.ENGLISH
        }
    }

    /**
     * Flow of the currently selected app language.
     *
     * @return A Flow emitting the current LanguageOption.
     */
    val languageFlow: Flow<LanguageOption> = context.languageDataStore.data.map { preferences ->
        when (preferences[LANGUAGE_KEY]) {
            LanguageOption.BOKMAL.name -> LanguageOption.BOKMAL
            "BOKMÅL" -> LanguageOption.BOKMAL
            LanguageOption.NYNORSK.name -> LanguageOption.NYNORSK
            LanguageOption.ENGLISH.name -> LanguageOption.ENGLISH
            LanguageOption.SAMI.name -> LanguageOption.SAMI
            else -> systemDefault()
        }
    }

    /**
     * Saves the selected app language to storage.
     *
     * @param language The language option selected by the user.
     */
    suspend fun saveLanguage(language: LanguageOption) {
        context.languageDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = language.name
        }
    }
}