package no.uio.ifi.in2000.team45.ui.savedplaces

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
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
                val avgWind = mapRepository.getWindSpeed(lat, lon)

                // emit zones first so risk chips update before frost finishes
                locationStatuses[key] = LocationStatus(
                    inFloodZone = floodZone,
                    inRadonZone = radonZone,
                    inQuickClayZone = quickClayZone,
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
}
