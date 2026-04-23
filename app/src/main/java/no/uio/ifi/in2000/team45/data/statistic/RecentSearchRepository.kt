package no.uio.ifi.in2000.team45.data.statistic

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.statisticDataStore by preferencesDataStore(name = "statistic_preferences")

class RecentSearchRepository(private val context: Context) {

    companion object {
        private val RECENT_SEARCHES_KEY = stringPreferencesKey("recent_searches")
        private const val MAX_HISTORY_SIZE = 5
    }

    val recentSearchesFlow: Flow<List<String>> =
        context.statisticDataStore.data.map { preferences ->
            parse(preferences[RECENT_SEARCHES_KEY])
        }

    suspend fun addSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        context.statisticDataStore.edit { preferences ->
            val current = parse(preferences[RECENT_SEARCHES_KEY])

            val updated = (listOf(trimmed) + current.filter { it != trimmed })
                .take(MAX_HISTORY_SIZE)

            preferences[RECENT_SEARCHES_KEY] = updated.joinToString("|")
        }
    }

    suspend fun removeSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        context.statisticDataStore.edit { preferences ->
            val current = parse(preferences[RECENT_SEARCHES_KEY])
            val updated = current.filter { it != trimmed }
            preferences[RECENT_SEARCHES_KEY] = updated.joinToString("|")
        }
    }

    private fun parse(raw: String?): List<String> {
        return raw
            ?.split("|")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }
}