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
import no.uio.ifi.in2000.team45.data.savedplaces.models.FavoriteStation
import no.uio.ifi.in2000.team45.data.savedplaces.models.SavedPlacesLocation
import no.uio.ifi.in2000.team45.data.savedplaces.utils.NorwayLocationValidator

private val Context.favoritesDataStore by preferencesDataStore(name = "savedplaces")

/**
 * Repository for storing and retrieving saved stations and locations
 * using DataStore.
 *
 * @property context Application context used to access DataStore and resources.
 */
class SavedPlacesRepository(private val context: Context) {

    companion object {
        private val STATIONS_KEY = stringPreferencesKey("savedplaces_stations")
        private val LOCATIONS_KEY = stringPreferencesKey("savedplaces_locations")
    }

    private val json = Json { ignoreUnknownKeys = true }

    init {
        NorwayLocationValidator.loadBoundary(context)
    }

    /**
     * Retrieves a flow of saved favorite weather stations.
     *
     * @return A flow emitting the current list of favorite stations.
     */
    val favoriteStationsFlow: Flow<List<FavoriteStation>> =
        context.favoritesDataStore.data.map { prefs ->
            prefs[STATIONS_KEY]?.let { parseStations(it) } ?: emptyList()
        }

    /**
     * Retrieves a flow of saved user-defined locations.
     *
     * @return A flow emitting the current list of saved locations.
     */
    val savedPlacesLocationsFlow: Flow<List<SavedPlacesLocation>> =
        context.favoritesDataStore.data.map { prefs ->
            prefs[LOCATIONS_KEY]?.let { parseLocations(it) } ?: emptyList()
        }

    /**
     * Adds a new station to favorites if it is not already saved.
     *
     * @param station The station to add.
     */
    suspend fun addStation(station: FavoriteStation) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[STATIONS_KEY]?.let { parseStations(it) } ?: emptyList()
            if (current.none { it.stationId == station.stationId }) {
                prefs[STATIONS_KEY] = encodeStations(current + station)
            }
        }
    }

    /**
     * Removes a station from favorites by its station ID.
     *
     * @param stationId The unique identifier of the station to remove.
     */
    suspend fun removeStation(stationId: String) {
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[STATIONS_KEY]?.let { parseStations(it) } ?: emptyList()
            prefs[STATIONS_KEY] = encodeStations(current.filter { it.stationId != stationId })
        }
    }

    /**
     * Adds a new saved location if it is inside Norway.
     *
     * @param location The location to save.
     */
    suspend fun addLocation(location: SavedPlacesLocation) {
        if (!NorwayLocationValidator.isInsideNorway(location)) return
        context.favoritesDataStore.edit { prefs ->
            val current = prefs[LOCATIONS_KEY]?.let { parseLocations(it) } ?: emptyList()
            prefs[LOCATIONS_KEY] = encodeLocations(current + location)
        }
    }

    /**
     * Removes a saved location matching the given coordinates.
     *
     * @param location The location to remove.
     */
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

    /**
     * Removes all saved stations and locations from storage.
     */
    suspend fun removeAll() {
        context.favoritesDataStore.edit { prefs ->
            prefs.remove(STATIONS_KEY)
            prefs.remove(LOCATIONS_KEY)
        }
    }

    /**
     * Parses a JSON string into a list of FavoriteStation objects.
     *
     * @param raw The raw JSON string.
     * @return A list of parsed stations, or an empty list if parsing fails.
     */
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

    /**
     * Encodes a list of FavoriteStation objects into a Json string.
     *
     * @param list The list of stations to encode.
     * @return A Json string representation of the stations.
     */
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

    /**
     * Parses a Json string into a list of SavedPlacesLocation objects,
     * filtering out locations outside Norway.
     *
     * @param raw The raw Json string.
     * @return A list of valid saved locations, or an empty list if parsing fails.
     */
    private fun parseLocations(raw: String): List<SavedPlacesLocation> {
        return try {
            json.parseToJsonElement(raw).jsonArray.mapNotNull { el ->
                val obj = el.jsonObject
                val location = SavedPlacesLocation(
                    latitude = obj["latitude"]!!.jsonPrimitive.double,
                    longitude = obj["longitude"]!!.jsonPrimitive.double,
                    label = obj["label"]?.jsonPrimitive?.content ?: ""
                )

                if (NorwayLocationValidator.isInsideNorway(location)) location else null
            }
        } catch (_: Exception) { emptyList() }
    }

    /**
     * Encodes a list of SavedPlacesLocation objects into a Json string.
     *
     * @param list The list of locations to encode.
     * @return A JSON string representation of the locations.
     */
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
