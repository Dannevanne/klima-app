package no.uio.ifi.in2000.team45.ui.savedplaces

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.savedplaces.SavedPlacesLocation
import no.uio.ifi.in2000.team45.data.savedplaces.SavedPlacesRepository
import no.uio.ifi.in2000.team45.data.savedplaces.FavoriteStation
import no.uio.ifi.in2000.team45.data.map.MapRepository
import no.uio.ifi.in2000.team45.data.statistic.StatisticRepository
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation

data class LocationStatus(
    val stationName: String = "",
    val temperature: Double? = null,
    val windSpeed: Double? = null,
    val precipitation: Double? = null,
    val inFloodZone: Boolean = false,
    val inRadonZone: Boolean = false,
    val inQuickClayZone: Boolean = false,
    val inLandslideZone: Boolean = false,
    // Gradated risk: "High" if the saved point itself is in the zone,
    // "Moderate" if a hazard zone sits within ~500 m, "Low" otherwise.
    val floodLevel: String = "Low",
    val landslideLevel: String = "Low",
    val avgWindSpeed: Double? = null
)

class SavedPlacesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SavedPlacesRepository(application)
    private val statisticRepository = StatisticRepository()
    private val mapRepository = MapRepository()
    private val clientId: String = application.getString(R.string.frost_client_id)

    val stations = repository.favoriteStationsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val locations = repository.savedPlacesLocationsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Current weather per saved location (keyed by "lat,lon")
    val locationStatuses = mutableStateMapOf<String, LocationStatus>()

    // Set when another screen wants the saved-places list scrolled to a
    // specific row (e.g. the map popup tapping "Open in Saved places").
    // PopulatedSavedPlacesContent observes this and clears it after scrolling.
    var pendingFocus: SavedPlacesLocation? by mutableStateOf(null)
        private set

    fun requestFocus(location: SavedPlacesLocation) {
        pendingFocus = location
    }

    fun clearPendingFocus() {
        pendingFocus = null
    }

    fun addStation(station: WeatherStation) {
        viewModelScope.launch {
            repository.addStation(
                FavoriteStation(
                    stationId = station.id,
                    name = station.name,
                    municipality = station.municipality,
                    county = station.county
                )
            )
        }
    }

    fun removeStation(stationId: String) {
        viewModelScope.launch { repository.removeStation(stationId) }
    }

    fun removeLocation(location: SavedPlacesLocation) {
        viewModelScope.launch { repository.removeLocation(location) }
    }

    fun addLocation(lat: Double, lng: Double, label: String) {
        viewModelScope.launch {
            repository.addLocation(SavedPlacesLocation(lat, lng, label))
        }
    }

    fun removeAllSavedPlaces() {
        viewModelScope.launch {
            repository.removeAll()
            locationStatuses.clear()
        }
    }

    fun fetchStatusForLocation(location: SavedPlacesLocation) {
        val key = "${location.latitude},${location.longitude}"
        if (locationStatuses.containsKey(key)) return

        viewModelScope.launch {
            try {
                val lat = location.latitude
                val lon = location.longitude
                val floodZone = mapRepository.isInFloodZone(lat, lon)
                val radonZone = mapRepository.isInRadonZone(lat, lon)
                val quickClayZone = mapRepository.isInQuickClayZone(lat, lon)
                val landslideZone = mapRepository.isInLandslideZone(lat, lon)
                val avgWind = mapRepository.getWindSpeed(lat, lon)

                val floodLevel = zoneRiskLevel(lat, lon, floodZone) { la, lo ->
                    mapRepository.isInFloodZone(la, lo)
                }
                val landslideLevel = zoneRiskLevel(
                    lat, lon, landslideZone || quickClayZone
                ) { la, lo ->
                    mapRepository.isInLandslideZone(la, lo) ||
                            mapRepository.isInQuickClayZone(la, lo)
                }

                // emit zones first so risk chips update before frost finishes
                locationStatuses[key] = LocationStatus(
                    inFloodZone = floodZone,
                    inRadonZone = radonZone,
                    inQuickClayZone = quickClayZone,
                    inLandslideZone = landslideZone,
                    floodLevel = floodLevel,
                    landslideLevel = landslideLevel,
                    avgWindSpeed = avgWind
                )

                val stations = statisticRepository.findNearestStations(
                    lat, lon, clientId, count = 5
                )

                if (stations.isEmpty()) {
                    locationStatuses[key] = LocationStatus(
                        stationName = getApplication<Application>().getString(R.string.station_not_found),
                        inFloodZone = floodZone,
                        inRadonZone = radonZone,
                        inQuickClayZone = quickClayZone,
                        inLandslideZone = landslideZone,
                        floodLevel = floodLevel,
                        landslideLevel = landslideLevel,
                        avgWindSpeed = avgWind
                    )
                    return@launch
                }

                // Try stations until we have temp, wind, and precip
                var temp: Double? = null
                var wind: Double? = null
                var precip: Double? = null
                var usedStation = stations.first().name

                for (station in stations) {
                    val obs = statisticRepository.getLatestObservations(
                        station.id, clientId
                    )
                    if (temp == null) {
                        obs["air_temperature"]?.let {
                            temp = it
                            usedStation = station.name
                        }
                    }
                    if (wind == null) {
                        obs["wind_speed"]?.let { wind = it }
                    }
                    if (precip == null) {
                        obs.entries.find {
                            it.key.contains("precipitation")
                        }?.value?.let { precip = it }
                    }
                    if (temp != null && wind != null && precip != null) break
                }

                locationStatuses[key] = LocationStatus(
                    stationName = usedStation,
                    temperature = temp,
                    windSpeed = wind,
                    precipitation = precip,
                    inFloodZone = floodZone,
                    inRadonZone = radonZone,
                    inQuickClayZone = quickClayZone,
                    inLandslideZone = landslideZone,
                    floodLevel = floodLevel,
                    landslideLevel = landslideLevel,
                    avgWindSpeed = avgWind
                )
            } catch (e: Exception) {
                Log.e("SavedPlacesVM", "Failed to fetch status", e)
                locationStatuses[key] = LocationStatus(
                    stationName = getApplication<Application>().getString(R.string.fetch_error)
                )
            }
        }
    }

    // High when the saved point itself sits in the hazard zone, Moderate when
    // any of four cardinal offsets ~500 m away does, Low otherwise. Cheap
    // proxy for true distance-to-zone without pulling polygons.
    private suspend fun zoneRiskLevel(
        lat: Double,
        lon: Double,
        atPoint: Boolean,
        check: suspend (Double, Double) -> Boolean
    ): String {
        if (atPoint) return "High"
        val offsetMeters = 500.0
        val offsetLat = offsetMeters / 111_000.0
        val offsetLon = offsetLat / kotlin.math.cos(Math.toRadians(lat))
        val nearbyHit = check(lat + offsetLat, lon) ||
                check(lat - offsetLat, lon) ||
                check(lat, lon + offsetLon) ||
                check(lat, lon - offsetLon)
        return if (nearbyHit) "Moderate" else "Low"
    }
}
