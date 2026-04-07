package no.uio.ifi.in2000.team45.data.favorites

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.favoritesDataStore by preferencesDataStore(name = "favorites")

@Serializable
data class FavoriteStation(
    val stationId: String,
    val name: String,
    val municipality: String = "",
    val county: String = ""
)

@Serializable
data class FavoriteLocation(
    val latitude: Double,
    val longitude: Double,
    val label: String = ""
)

class FavoriteRepository(private val context: Context) {

    companion object {
        private val STATIONS_KEY = stringPreferencesKey("favorite_stations")
        private val LOCATIONS_KEY = stringPreferencesKey("favorite_locations")
    }

    private val json = Json { ignoreUnknownKeys = true }

    val favoriteStationsFlow: Flow<List<FavoriteStation>> =
        context.favoritesDataStore.data.map { prefs ->
            prefs[STATIONS_KEY]?.let {
                json.decodeFromString<List<FavoriteStation>>(it)
            } ?: emptyList()
        }

    val favoriteLocationsFlow: Flow<List<FavoriteLocation>> =
        context.favoritesDataStore.data.map { prefs ->
            prefs[LOCATIONS_KEY]?.let {
                json.decodeFromString<List<FavoriteLocation>>(it)
            } ?: emptyList()
        }

    suspend fun addStation(station: FavoriteStation) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[STATIONS_KEY]?.let {
                json.decodeFromString<List<FavoriteStation>>(it)
            } ?: emptyList()
            if (current.none { it.stationId == station.stationId }) {
                prefs[STATIONS_KEY] = json.encodeToString(current + station)
            }
        }
    }

    suspend fun removeStation(stationId: String) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[STATIONS_KEY]?.let {
                json.decodeFromString<List<FavoriteStation>>(it)
            } ?: emptyList()
            prefs[STATIONS_KEY] = json.encodeToString(
                current.filter { it.stationId != stationId }
            )
        }
    }

    suspend fun addLocation(location: FavoriteLocation) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[LOCATIONS_KEY]?.let {
                json.decodeFromString<List<FavoriteLocation>>(it)
            } ?: emptyList()
            prefs[LOCATIONS_KEY] = json.encodeToString(current + location)
        }
    }

    suspend fun removeLocation(location: FavoriteLocation) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[LOCATIONS_KEY]?.let {
                json.decodeFromString<List<FavoriteLocation>>(it)
            } ?: emptyList()
            prefs[LOCATIONS_KEY] = json.encodeToString(
                current.filter {
                    it.latitude != location.latitude || it.longitude != location.longitude
                }
            )
        }
    }
}
