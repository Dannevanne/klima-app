package no.uio.ifi.in2000.team45.data.statistic

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.statisticDataStore by preferencesDataStore(name = "statistic_preferences")

/**
 * Repository responsible for storing and retrieving recent search queries
 * using DataStore.
 */
class RecentSearchRepository(private val context: Context) {

    companion object {
        private val RECENT_SEARCHES_KEY = stringPreferencesKey("recent_searches")
        private const val MAX_HISTORY_SIZE = 5
    }

    /**
     * Flow of recent search queries stored in persistent storage.
     *
     * @return A Flow emitting the current list of recent search strings.
     */
    val recentSearchesFlow: Flow<List<String>> =
        context.statisticDataStore.data.map { preferences ->
            parse(preferences[RECENT_SEARCHES_KEY])
        }

    /**
     * Adds a search query to the recent search history.
     *
     * @param query The search query to store.
     */
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

    /**
     * Removes a search query from the recent search history.
     *
     * @param query The search query to remove.
     */
    suspend fun removeSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        context.statisticDataStore.edit { preferences ->
            val current = parse(preferences[RECENT_SEARCHES_KEY])
            val updated = current.filter { it != trimmed }
            preferences[RECENT_SEARCHES_KEY] = updated.joinToString("|")
        }
    }


    /**
     * Parses a serialized recent search history string into a list of queries.
     *
     * @param raw The raw stored string containing search queries.
     * @return A list of search queries.
     */
    private fun parse(raw: String?): List<String> {
        return raw
            ?.split("|")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    /**
     * Clears all stored recent search history.
     */
    suspend fun clearHistory() {
        context.statisticDataStore.edit { preferences ->
            preferences.remove(RECENT_SEARCHES_KEY)
        }
    }
}