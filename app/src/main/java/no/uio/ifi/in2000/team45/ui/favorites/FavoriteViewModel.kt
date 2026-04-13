package no.uio.ifi.in2000.team45.ui.favorites

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.favorites.FavoriteLocation
import no.uio.ifi.in2000.team45.data.favorites.FavoriteRepository
import no.uio.ifi.in2000.team45.data.favorites.FavoriteStation
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
    val inWindZone: Boolean = false
)

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FavoriteRepository(application)
    private val statisticRepository = StatisticRepository()
    private val mapRepository = MapRepository()
    private val clientId: String = application.getString(R.string.frost_client_id)

    val stations = repository.favoriteStationsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val locations = repository.favoriteLocationsFlow.stateIn(
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

    fun removeLocation(location: FavoriteLocation) {
        viewModelScope.launch { repository.removeLocation(location) }
    }

    fun addLocation(lat: Double, lng: Double, label: String) {
        viewModelScope.launch {
            repository.addLocation(FavoriteLocation(lat, lng, label))
        }
    }

    fun fetchStatusForLocation(location: FavoriteLocation) {
        val key = "${location.latitude},${location.longitude}"
        if (locationStatuses.containsKey(key)) return

        viewModelScope.launch {
            try {
                val station = statisticRepository.findNearestStation(
                    location.latitude, location.longitude, clientId
                )
                val lat = location.latitude
                val lon = location.longitude
                val floodZone = mapRepository.isInFloodZone(lat, lon)
                val radonZone = mapRepository.isInRadonZone(lat, lon)
                val quickClayZone = mapRepository.isInQuickClayZone(lat, lon)
                val windZone = mapRepository.isInWindZone(lat, lon)

                if (station == null) {
                    locationStatuses[key] = LocationStatus(
                        stationName = "Ingen stasjon funnet",
                        inFloodZone = floodZone,
                        inRadonZone = radonZone,
                        inQuickClayZone = quickClayZone,
                        inWindZone = windZone
                    )
                    return@launch
                }

                val obs = statisticRepository.getLatestObservations(station.id, clientId)
                locationStatuses[key] = LocationStatus(
                    stationName = station.name,
                    temperature = obs["air_temperature"],
                    windSpeed = obs["wind_speed"],
                    precipitation = obs.entries.find {
                        it.key.contains("precipitation")
                    }?.value,
                    inFloodZone = floodZone,
                    inRadonZone = radonZone,
                    inQuickClayZone = quickClayZone,
                    inWindZone = windZone
                )
            } catch (e: Exception) {
                Log.e("FavoriteVM", "Failed to fetch status", e)
                locationStatuses[key] = LocationStatus(stationName = "Feil ved henting")
            }
        }
    }
}
