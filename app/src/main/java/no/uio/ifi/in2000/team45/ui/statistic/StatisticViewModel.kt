package no.uio.ifi.in2000.team45.ui.statistic

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.R
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import no.uio.ifi.in2000.team45.data.statistic.AnnualObservation
import no.uio.ifi.in2000.team45.data.statistic.MonthlyClimateStats
import no.uio.ifi.in2000.team45.data.statistic.Place
import no.uio.ifi.in2000.team45.data.statistic.RecentSearchRepository
import no.uio.ifi.in2000.team45.data.statistic.StationAssignment
import no.uio.ifi.in2000.team45.data.statistic.StationPicker
import no.uio.ifi.in2000.team45.data.statistic.StatisticRepository
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation
import java.util.Calendar

sealed interface StatisticUiState {
    object Idle : StatisticUiState
    object Loading : StatisticUiState
    data class StationResults(val stations: List<WeatherStation>) : StatisticUiState
    data class PlaceResults(val places: List<Place>) : StatisticUiState
    data class ClimateData(
        val station: WeatherStation,
        val stats: List<MonthlyClimateStats>,
        val annualData: List<AnnualObservation> = emptyList(),
        val place: Place? = null,
        val distanceFromPlaceMeters: Int? = null,
        val stationsUsed: List<WeatherStation> = emptyList()
    ) : StatisticUiState
    data class Error(val message: String) : StatisticUiState
}

// @JvmOverloads is needed so AndroidViewModelFactory can find a single-arg
// (Application) constructor at the JVM level. Without it, viewModel() in
// AppNavigation crashes at startup because Kotlin only emits the all-args
// constructor for default-valued parameters.
class StatisticViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: StatisticRepository = StatisticRepository(),
    private val recentSearchRepository: RecentSearchRepository = RecentSearchRepository(application),
    private val clientId: String = application.getString(R.string.frost_client_id),
    private val mapboxAccessToken: String = application.getString(R.string.mapbox_access_token)
) : AndroidViewModel(application) {

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
        val lastYear = Calendar.getInstance().get(Calendar.YEAR) - 1
        val from = lastYear - years + 1
        return fullAnnualData.filter { it.year >= from }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        searchJob?.cancel()
        if (query.isEmpty()) {
            uiState = StatisticUiState.Idle
            return
        }
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
        // Only show the loading spinner when there's nothing useful on screen.
        // While refining a query (results already visible) keep the previous
        // list around so the screen doesn't blink between every keystroke.
        if (uiState !is StatisticUiState.StationResults &&
            uiState !is StatisticUiState.PlaceResults
        ) {
            uiState = StatisticUiState.Loading
        }
        try {
            val stations = repository.searchStations(query, clientId)
            if (stations.isNotEmpty()) {
                recentSearchRepository.addSearch(query)
                val places = groupStationsToPlaces(stations)
                if (places.size == 1) {
                    selectPlace(places.first())
                    return
                }
                uiState = if (places.isNotEmpty()) {
                    StatisticUiState.PlaceResults(places)
                } else {
                    StatisticUiState.StationResults(stations)
                }
                return
            }

            val geocoded = geocodePlaces(query)
            if (geocoded.size == 1) {
                recentSearchRepository.addSearch(query)
                selectPlace(geocoded.first())
                return
            }
            uiState = if (geocoded.isNotEmpty()) {
                recentSearchRepository.addSearch(query)
                StatisticUiState.PlaceResults(geocoded)
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

    private fun groupStationsToPlaces(stations: List<WeatherStation>): List<Place> {
        val byKey = linkedMapOf<String, MutableList<WeatherStation>>()
        for (s in stations) {
            val muni = s.municipality.trim()
            if (muni.isBlank()) continue
            val key = "${muni.lowercase()}|${s.county.lowercase()}"
            byKey.getOrPut(key) { mutableListOf() }.add(s)
        }
        return byKey.values.map { group ->
            val first = group.first()
            // Centre = mean of station coords. Lets us compute a sensible
            // distance from "Oslo" to each station in Oslo, instead of a
            // distance-to-first-station that's near zero for one station.
            val centerLat = group.map { it.latitude }.average()
            val centerLon = group.map { it.longitude }.average()
            Place(
                name = first.municipality.trim().toTitleCase(),
                subtitle = first.county.trim().toTitleCase(),
                latitude = centerLat,
                longitude = centerLon,
                stations = group.toList()
            )
        }
    }

    private suspend fun geocodePlaces(query: String): List<Place> {
        return repository.geocodePlaces(query, mapboxAccessToken)
    }

    private fun String.toTitleCase(): String {
        if (isBlank()) return this
        return split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
    }

    private val targetElements = listOf(
        "mean(air_temperature P1Y)",
        "sum(precipitation_amount P1Y)",
        "max(wind_speed_of_gust P1M)",
        "max(surface_snow_thickness P1M)"
    )

    fun selectPlace(place: Place) {
        climateJob?.cancel()
        uiState = StatisticUiState.Loading
        climateJob = viewModelScope.launch {
            try {
                val maxDistanceMeters = 30_000
                // Always pull spatial neighbours, even when place.stations
                // is non-empty. City searches group by municipality, so
                // Stavanger's stations don't include Sola airport 9.5km away
                // -- and Sola is the only one near Stavanger with gust data.
                val nearby = repository.findNearestStations(
                    place.latitude, place.longitude, clientId, count = 20
                )
                val rawCandidates = (place.stations + nearby).distinctBy { it.id }
                val candidates = rawCandidates
                    .map { c ->
                        c.copy(
                            distanceMeters = distanceInMeters(
                                place.latitude, place.longitude,
                                c.latitude, c.longitude
                            )
                        )
                    }
                    .filter { (it.distanceMeters ?: Int.MAX_VALUE) <= maxDistanceMeters }
                    .sortedBy { it.distanceMeters ?: Int.MAX_VALUE }

                if (candidates.isEmpty()) {
                    uiState = StatisticUiState.Error(
                        getApplication<Application>().getString(
                            R.string.error_no_climate_data, place.name
                        )
                    )
                    return@launch
                }

                // One batch probe: which station has which of our 4 elements
                // and how far back. This is what lets Blindern win for Oslo
                // -- it has all four with a 1937 start, beating sparser
                // sensors that happen to be closer.
                val availability = repository.getAvailableTimeSeries(
                    candidates.map { it.id }, targetElements, clientId
                )
                val byStation = availability.groupBy { it.stationId }

                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val yearTo = currentYear - 1
                val yearFrom = yearTo - maxPeriod + 1

                val chosen = StationPicker.pickStationsForPlace(
                    candidates, byStation, targetElements, currentYear
                ).toMutableList()

                if (chosen.isEmpty()) {
                    // Frost availability returned nothing for any candidate.
                    // Fall back to fetching the closest station blind so we
                    // at least show something rather than an error wall.
                    chosen.add(StationAssignment(candidates.first(), targetElements.toSet()))
                }

                val fetched = coroutineScope {
                    chosen.map { assignment ->
                        async {
                            val a = async {
                                repository.getAnnualObservations(
                                    assignment.station.id, clientId, yearFrom, yearTo,
                                    assignment.elements
                                )
                            }
                            val s = async {
                                repository.getClimateStats(assignment.station.id, clientId)
                            }
                            Triple(assignment.station, a.await(), s.await())
                        }
                    }.map { it.await() }
                }

                val annualByYear = mutableMapOf<Int, AnnualObservation>()
                val statsByMonth = (1..12).associateWith { month ->
                    MonthlyClimateStats(
                        month = month,
                        meanTemp = null,
                        maxTemp = null,
                        minTemp = null,
                        precipitation = null,
                        windSpeed = null
                    )
                }.toMutableMap()
                val stationsUsed = linkedSetOf<WeatherStation>()

                for ((station, annual, stats) in fetched) {
                    var contributed = false

                    for (obs in annual) {
                        val existing = annualByYear[obs.year]
                            ?: AnnualObservation(obs.year, null, null)
                        val merged = existing.copy(
                            meanTemp = existing.meanTemp ?: obs.meanTemp,
                            totalPrecip = existing.totalPrecip ?: obs.totalPrecip,
                            maxWindGust = existing.maxWindGust ?: obs.maxWindGust,
                            maxWindSpeed = existing.maxWindSpeed ?: obs.maxWindSpeed,
                            maxSnowDepth = existing.maxSnowDepth ?: obs.maxSnowDepth,
                            heavyPrecipDays = existing.heavyPrecipDays ?: obs.heavyPrecipDays
                        )
                        if (merged != existing) contributed = true
                        annualByYear[obs.year] = merged
                    }

                    for (stat in stats) {
                        val existing = statsByMonth[stat.month] ?: continue
                        val merged = existing.copy(
                            meanTemp = existing.meanTemp ?: stat.meanTemp,
                            maxTemp = existing.maxTemp ?: stat.maxTemp,
                            minTemp = existing.minTemp ?: stat.minTemp,
                            precipitation = existing.precipitation ?: stat.precipitation,
                            windSpeed = existing.windSpeed ?: stat.windSpeed,
                            recordHigh = existing.recordHigh ?: stat.recordHigh,
                            recordLow = existing.recordLow ?: stat.recordLow,
                            snowDepth = existing.snowDepth ?: stat.snowDepth,
                            windGust = existing.windGust ?: stat.windGust,
                            cloudCover = existing.cloudCover ?: stat.cloudCover
                        )
                        if (merged != existing) contributed = true
                        statsByMonth[stat.month] = merged
                    }

                    if (contributed) stationsUsed.add(station)
                }

                if (stationsUsed.isEmpty()) {
                    uiState = StatisticUiState.Error(
                        getApplication<Application>().getString(
                            R.string.error_no_climate_data, place.name
                        )
                    )
                    return@launch
                }

                val annual = annualByYear.values.sortedBy { it.year }
                val stats = statsByMonth.values.sortedBy { it.month }
                fullAnnualData = annual

                val primary = stationsUsed.first()

                uiState = StatisticUiState.ClimateData(
                    station = primary,
                    stats = stats,
                    annualData = sliceAnnualForPeriod(selectedPeriod),
                    place = place,
                    distanceFromPlaceMeters = primary.distanceMeters,
                    stationsUsed = stationsUsed.toList()
                )
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
                // Skip the in-progress year for both queries. P1Y temp/precip
                // returns nothing for it, but P1M wind/snow returns partial
                // months, which would offset the two charts by a year.
                val yearTo = currentYear - 1
                val yearFrom = yearTo - maxPeriod + 1

                for (i in startIndex until candidates.size) {
                    val candidate = candidates[i]
                    val stats = repository.getClimateStats(candidate.id, clientId)
                    if (stats.all { it.meanTemp == null && it.precipitation == null }) {
                        continue
                    }
                    val annual = repository.getAnnualObservations(
                        candidate.id, clientId, yearFrom, yearTo
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
        searchQuery = ""
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
