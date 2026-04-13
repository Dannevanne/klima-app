package no.uio.ifi.in2000.team45.ui.statistic

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.R
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteOptions
import com.mapbox.search.common.IsoCountryCode
import no.uio.ifi.in2000.team45.data.statistic.AnnualObservation
import no.uio.ifi.in2000.team45.data.statistic.MonthlyClimateStats
import no.uio.ifi.in2000.team45.data.statistic.StatisticRepository
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation
import java.util.Calendar

sealed interface StatisticUiState {
    object Idle : StatisticUiState
    object Loading : StatisticUiState
    data class StationResults(val stations: List<WeatherStation>) : StatisticUiState
    data class ClimateData(
        val station: WeatherStation,
        val stats: List<MonthlyClimateStats>,
        val annualData: List<AnnualObservation> = emptyList()
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

    var selectedPeriod: Int by mutableStateOf(30)
        private set

    var searchHistory: List<String> by mutableStateOf(emptyList())
        private set

    fun updatePeriod(years: Int) {
        selectedPeriod = years
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        searchJob?.cancel()
        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(400)
                searchStations()
            }
        }
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
                    addToHistory(searchQuery)
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
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val yearFrom = currentYear - selectedPeriod
                val stats = repository.getClimateStats(station.id, clientId)
                val annual = repository.getAnnualObservations(
                    station.id, clientId, yearFrom, currentYear
                )
                uiState = if (stats.all { it.meanTemp == null && it.precipitation == null }) {
                    StatisticUiState.Error("Ingen klimadata tilgjengelig for ${station.name}")
                } else {
                    StatisticUiState.ClimateData(station, stats, annual)
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

    private fun addToHistory(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return
        searchHistory = (listOf(trimmed) + searchHistory.filter { it != trimmed }).take(5)
    }

    private suspend fun geocodeAndFindNearest(query: String): WeatherStation? {
        val options = PlaceAutocompleteOptions(
            limit = 1,
            countries = listOf(IsoCountryCode.NORWAY)
        )
        val response = placeAutocomplete.suggestions(query = query, options = options)
        val suggestion = response.value?.firstOrNull() ?: return null

        val selectResponse = placeAutocomplete.select(suggestion)
        val coord = selectResponse.value?.coordinate ?: return null

        return repository.findNearestStation(
            coord.latitude(), coord.longitude(), clientId
        )
    }
}
