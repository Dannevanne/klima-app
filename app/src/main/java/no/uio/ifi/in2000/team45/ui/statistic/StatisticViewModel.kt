package no.uio.ifi.in2000.team45.ui.statistic

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.R
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteOptions
import no.uio.ifi.in2000.team45.data.statistic.MonthlyClimateStats
import no.uio.ifi.in2000.team45.data.statistic.StatisticRepository
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation
import kotlinx.coroutines.Job

sealed interface StatisticUiState {
    object Idle : StatisticUiState
    object Loading : StatisticUiState
    data class StationResults(val stations: List<WeatherStation>) : StatisticUiState
    data class ClimateData(
        val station: WeatherStation,
        val stats: List<MonthlyClimateStats>
    ) : StatisticUiState
    data class Error(val message: String) : StatisticUiState
}

class StatisticViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = StatisticRepository()
    private val clientId: String = application.getString(R.string.frost_client_id)
    private val placeAutocomplete = PlaceAutocomplete.create(locationProvider = null)
    private var searchJob: Job? = null
    private var climateJob: Job? = null

    var uiState: StatisticUiState by mutableStateOf(StatisticUiState.Idle)
        private set

    var searchQuery: String by mutableStateOf("")
        private set

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun searchStations() {
        if (searchQuery.isBlank()) return

        searchJob?.cancel()
        uiState = StatisticUiState.Loading

        val queryAtLaunch = searchQuery

        searchJob = viewModelScope.launch {
            try {
                val stations = repository.searchStations(queryAtLaunch, clientId)
                if (stations.isNotEmpty()) {
                    uiState = StatisticUiState.StationResults(stations)
                    return@launch
                }

                // No station name match -- geocode the query and find nearest
                val nearest = geocodeAndFindNearest(queryAtLaunch)
                uiState = if (nearest != null) {
                    StatisticUiState.StationResults(listOf(nearest))
                } else {
                    StatisticUiState.Error("Ingen stasjoner funnet for \"$queryAtLaunch\"")
                }
            } catch (e: Exception) {
                Log.e("StatisticVM", "Search failed", e)
                uiState = StatisticUiState.Error("Feil ved sok: ${e.message}")
            }
        }
    }

    fun selectStation(station: WeatherStation) {
        climateJob?.cancel()
        uiState = StatisticUiState.Loading

        climateJob = viewModelScope.launch {
            try {
                val stats = repository.getClimateStats(station.id, clientId)
                uiState = if (stats.all { it.meanTemp == null && it.precipitation == null }) {
                    StatisticUiState.Error("Ingen klimadata tilgjengelig for ${station.name}")
                } else {
                    StatisticUiState.ClimateData(station, stats)
                }
            } catch (e: Exception) {
                Log.e("StatisticVM", "Climate data failed", e)
                uiState = StatisticUiState.Error("Feil ved henting av data: ${e.message}")
            }
        }
    }

    fun goBack() {
        uiState = StatisticUiState.Idle
    }

    fun cancelStatisticScreenWork() {
        searchJob?.cancel()
        climateJob?.cancel()
    }

    override fun onCleared() {
        searchJob?.cancel()
        climateJob?.cancel()
        super.onCleared()
    }

    private suspend fun geocodeAndFindNearest(query: String): WeatherStation? {
        val options = PlaceAutocompleteOptions(
            limit = 1,
            countries = listOf(PlaceAutocompleteOptions.Country.NORWAY)
        )
        val suggestions = placeAutocomplete.suggestions(query, options)
            .getOrNull() ?: return null

        val first = suggestions.firstOrNull() ?: return null
        val result = placeAutocomplete.select(first).getOrNull() ?: return null
        val coord = result.coordinate ?: return null

        return repository.findNearestStation(coord.latitude(), coord.longitude(), clientId)
    }
}
