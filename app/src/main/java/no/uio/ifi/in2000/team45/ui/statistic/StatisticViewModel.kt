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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import no.uio.ifi.in2000.team45.data.statistic.AnnualObservation
import no.uio.ifi.in2000.team45.data.statistic.MonthlyClimateStats
import no.uio.ifi.in2000.team45.data.statistic.RecentSearchRepository
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
    private val recentSearchRepository = RecentSearchRepository(application)
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

    // Fetched once per station for the longest selectable period; period
    // chips slice this locally so chart switching is instant and the
    // missing-data warning doesn't blink during a refetch.
    private val maxPeriod = 50
    private var fullAnnualData: List<AnnualObservation> = emptyList()

    val searchHistory = recentSearchRepository.recentSearchesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun resetAllStatisticData() {
        searchJob?.cancel()
        climateJob?.cancel()
        searchQuery = ""
        selectedPeriod = 30
        uiState = StatisticUiState.Idle
    }

    fun updatePeriod(years: Int) {
        selectedPeriod = years
        val current = uiState
        if (current is StatisticUiState.ClimateData) {
            uiState = current.copy(annualData = sliceAnnualForPeriod(years))
        }
    }

    private fun sliceAnnualForPeriod(years: Int): List<AnnualObservation> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val from = currentYear - years
        return fullAnnualData.filter { it.year >= from }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        searchJob?.cancel()
        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(400)
                performSearch(query)
            }
        }
    }

    fun searchStations() {
        val query = searchQuery
        if (query.isBlank()) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            performSearch(query)
        }
    }

    private suspend fun performSearch(query: String) {
        uiState = StatisticUiState.Loading
        try {
            val stations = repository.searchStations(query, clientId)
            if (stations.isNotEmpty()) {
                recentSearchRepository.addSearch(query)
                uiState = StatisticUiState.StationResults(stations)
                return
            }

            val nearby = geocodeAndFindNearest(query)
            uiState = if (nearby.isNotEmpty()) {
                recentSearchRepository.addSearch(query)
                StatisticUiState.StationResults(nearby)
            } else {
                StatisticUiState.Error(
                    getApplication<Application>().getString(
                        R.string.error_no_stations_found, query
                    )
                )
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // The user typed another character or moved on. Don't paint Error
            // for a cancelled search; just let the next coroutine take over.
            throw e
        } catch (e: Exception) {
            Log.e("StatisticVM", "Search failed", e)
            uiState = StatisticUiState.Error(
                getApplication<Application>().getString(
                    R.string.error_search_failed, e.message ?: ""
                )
            )
        }
    }

    fun searchNearbyStations(lat: Double, lon: Double) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            uiState = StatisticUiState.Loading
            try {
                val stations = repository.findNearestStations(lat, lon, clientId, count = 5)
                    .map { station ->
                        station.copy(
                            distanceMeters = distanceInMeters(
                                lat, lon, station.latitude, station.longitude
                            )
                        )
                    }
                uiState = if (stations.isNotEmpty()) {
                    StatisticUiState.StationResults(stations)
                } else {
                    StatisticUiState.Error(
                        getApplication<Application>().getString(
                            R.string.error_no_stations_found, ""
                        )
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("StatisticVM", "Nearby search failed", e)
                uiState = StatisticUiState.Error(
                    getApplication<Application>().getString(
                        R.string.error_search_failed, e.message ?: ""
                    )
                )
            }
        }
    }

    fun selectStation(station: WeatherStation) {
        // Get the full candidate list from StationResults if available
        val candidates = (uiState as? StatisticUiState.StationResults)?.stations ?: listOf(station)
        val startIndex = candidates.indexOfFirst { it.id == station.id }.coerceAtLeast(0)

        climateJob?.cancel()
        uiState = StatisticUiState.Loading

        climateJob = viewModelScope.launch {
            try {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val yearFrom = currentYear - maxPeriod

                // Try this station and fall through to next if no data
                for (i in startIndex until candidates.size) {
                    val candidate = candidates[i]
                    val stats = repository.getClimateStats(candidate.id, clientId)
                    if (stats.all { it.meanTemp == null && it.precipitation == null }) {
                        continue
                    }
                    val annual = repository.getAnnualObservations(
                        candidate.id, clientId, yearFrom, currentYear
                    )
                    fullAnnualData = annual
                    uiState = StatisticUiState.ClimateData(
                        candidate, stats, sliceAnnualForPeriod(selectedPeriod)
                    )
                    return@launch
                }

                uiState = StatisticUiState.Error(
                    getApplication<Application>().getString(
                        R.string.error_no_climate_data, station.name
                    )
                )
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("StatisticVM", "Climate data failed", e)
                uiState = StatisticUiState.Error(
                    getApplication<Application>().getString(
                        R.string.error_fetch_data, e.message ?: ""
                    )
                )
            }
        }
    }

    fun goBack() {
        uiState = StatisticUiState.Idle
    }

    // Tap-outside on Stats: drop the query, cancel any in-flight search,
    // and bounce back to Idle. Don't disturb the user when they're already
    // looking at climate data for a specific station.
    fun exitSearch() {
        if (uiState is StatisticUiState.ClimateData) return
        searchJob?.cancel()
        searchQuery = ""
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

    private suspend fun geocodeAndFindNearest(query: String): List<WeatherStation> {
        val options = PlaceAutocompleteOptions(
            limit = 1,
            countries = listOf(IsoCountryCode.NORWAY)
        )
        val response = placeAutocomplete.suggestions(query = query, options = options)
        val suggestion = response.value?.firstOrNull() ?: return emptyList()

        val selectResponse = placeAutocomplete.select(suggestion)
        val coord = selectResponse.value?.coordinate ?: return emptyList()

        val lat = coord.latitude()
        val lon = coord.longitude()
        return repository.findNearestStations(lat, lon, clientId, count = 5).map { station ->
            station.copy(distanceMeters = distanceInMeters(lat, lon, station.latitude, station.longitude))
        }
    }

    private fun distanceInMeters(
        lat1: Double, lon1: Double, lat2: Double, lon2: Double
    ): Int {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (r * c).toInt()
    }

    fun removeRecentSearch(query: String) {
        viewModelScope.launch {
            recentSearchRepository.removeSearch(query)
        }
    }
}
