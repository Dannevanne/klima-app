package no.uio.ifi.in2000.team45.data.savedplaces

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

private val Context.favoritesDataStore by preferencesDataStore(name = "savedplaces")
class SavedPlacesRepository(private val context: Context) {

    companion object {
        private val STATIONS_KEY = stringPreferencesKey("savedplaces_stations")
        private val LOCATIONS_KEY = stringPreferencesKey("savedplaces_locations")
    }

    private val json = Json { ignoreUnknownKeys = true }

    val favoriteStationsFlow: Flow<List<FavoriteStation>> =
        context.favoritesDataStore.data.map { prefs ->
            prefs[STATIONS_KEY]?.let { parseStations(it) } ?: emptyList()
        }

    val savedPlacesLocationsFlow: Flow<List<SavedPlacesLocation>> =
        context.favoritesDataStore.data.map { prefs ->
            prefs[LOCATIONS_KEY]?.let { parseLocations(it) } ?: emptyList()
        }

    suspend fun addStation(station: FavoriteStation) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[STATIONS_KEY]?.let { parseStations(it) } ?: emptyList()
            if (current.none { it.stationId == station.stationId }) {
                prefs[STATIONS_KEY] = encodeStations(current + station)
            }
        }
    }

    suspend fun removeStation(stationId: String) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[STATIONS_KEY]?.let { parseStations(it) } ?: emptyList()
            prefs[STATIONS_KEY] = encodeStations(current.filter { it.stationId != stationId })
        }
    }

    suspend fun addLocation(location: SavedPlacesLocation) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[LOCATIONS_KEY]?.let { parseLocations(it) } ?: emptyList()
            prefs[LOCATIONS_KEY] = encodeLocations(current + location)
        }
    }

    suspend fun removeLocation(location: SavedPlacesLocation) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[LOCATIONS_KEY]?.let { parseLocations(it) } ?: emptyList()
            prefs[LOCATIONS_KEY] = encodeLocations(
                current.filter {
                    it.latitude != location.latitude || it.longitude != location.longitude
                }
            )
        }
    }

    suspend fun removeAll() {
        context.favoritesDataStore.edit { prefs ->
            prefs.remove(STATIONS_KEY)
            prefs.remove(LOCATIONS_KEY)
        }
    }

    // Manual JSON - no serialization compiler plugin needed
    private fun parseStations(raw: String): List<FavoriteStation> {
        return try {
            json.parseToJsonElement(raw).jsonArray.map { el ->
                val obj = el.jsonObject
                FavoriteStation(
                    stationId = obj["stationId"]!!.jsonPrimitive.content,
                    name = obj["name"]!!.jsonPrimitive.content,
                    municipality = obj["municipality"]?.jsonPrimitive?.content ?: "",
                    county = obj["county"]?.jsonPrimitive?.content ?: ""
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    private fun encodeStations(list: List<FavoriteStation>): String {
        return buildJsonArray {
            list.forEach { s ->
                add(buildJsonObject {
                    put("stationId", s.stationId)
                    put("name", s.name)
                    put("municipality", s.municipality)
                    put("county", s.county)
                })
            }
        }.toString()
    }

    private fun parseLocations(raw: String): List<SavedPlacesLocation> {
        return try {
            json.parseToJsonElement(raw).jsonArray.map { el ->
                val obj = el.jsonObject
                SavedPlacesLocation(
                    latitude = obj["latitude"]!!.jsonPrimitive.double,
                    longitude = obj["longitude"]!!.jsonPrimitive.double,
                    label = obj["label"]?.jsonPrimitive?.content ?: ""
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    private fun encodeLocations(list: List<SavedPlacesLocation>): String {
        return buildJsonArray {
            list.forEach { l ->
                add(buildJsonObject {
                    put("latitude", l.latitude)
                    put("longitude", l.longitude)
                    put("label", l.label)
                })
            }
        }.toString()
    }
}
