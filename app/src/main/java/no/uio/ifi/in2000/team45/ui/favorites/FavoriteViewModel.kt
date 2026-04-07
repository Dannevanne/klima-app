package no.uio.ifi.in2000.team45.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.data.favorites.FavoriteLocation
import no.uio.ifi.in2000.team45.data.favorites.FavoriteRepository
import no.uio.ifi.in2000.team45.data.favorites.FavoriteStation
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FavoriteRepository(application)

    val stations = repository.favoriteStationsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val locations = repository.favoriteLocationsFlow.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

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
}
