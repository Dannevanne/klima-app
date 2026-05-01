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
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.autocomplete.PlaceAutocompleteOptions
import com.mapbox.search.common.IsoCountryCode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import no.uio.ifi.in2000.team45.data.statistic.AnnualObservation
import no.uio.ifi.in2000.team45.data.statistic.MonthlyClimateStats
import no.uio.ifi.in2000.team45.data.statistic.RecentSearchRepository
import no.uio.ifi.in2000.team45.data.statistic.StatisticRepository
import no.uio.ifi.in2000.team45.data.statistic.TimeSeriesAvailability
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

data class Place(
    val name: String,
    val subtitle: String,
    val latitude: Double,
    val longitude: Double,
    // Stations already known to belong to this place (e.g., grouped from a
    // Frost name match like "OSLO - BLINDERN", "OSLO - VINDERN"). Empty for
    // places that come from geocoding only.
    val stations: List<WeatherStation> = emptyList()
)

class StatisticViewModel(
    application: Application,
    private val repository: StatisticRepository = StatisticRepository(),
    private val recentSearchRepository: RecentSearchRepository = RecentSearchRepository(application),
    private val placeAutocomplete: PlaceAutocomplete = PlaceAutocomplete.create(locationProvider = null),
    private val clientId: String = application.getString(R.string.frost_client_id)
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
                val places = groupStationsToPlaces(stations)
                uiState = if (places.isNotEmpty()) {
                    StatisticUiState.PlaceResults(places)
                } else {
                    StatisticUiState.StationResults(stations)
                }
                return
            }

            val geocoded = geocodePlaces(query)
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
        // Mapbox autocomplete is a typeahead -- it returns variants of the same
        // query. We only want the canonical place, so cap at 1.
        val options = PlaceAutocompleteOptions(
            limit = 1,
            countries = listOf(IsoCountryCode.NORWAY)
        )
        val response = placeAutocomplete.suggestions(query = query, options = options)
        val suggestion = response.value?.firstOrNull() ?: return emptyList()
        val selected = placeAutocomplete.select(suggestion).value ?: return emptyList()
        val coord = selected.coordinate ?: return emptyList()
        return listOf(
            Place(
                name = suggestion.name,
                subtitle = (suggestion.formattedAddress ?: "").trim(),
                latitude = coord.latitude(),
                longitude = coord.longitude()
            )
        )
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
                val rawCandidates = if (place.stations.isNotEmpty()) {
                    place.stations
                } else {
                    repository.findNearestStations(
                        place.latitude, place.longitude, clientId, count = 20
                    )
                }
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

                val ranked = rankStations(candidates, byStation, currentYear)
                val chosen = pickStationsForCoverage(ranked)

                if (chosen.isEmpty()) {
                    // Frost availability returned nothing for any candidate.
                    // Fall back to fetching the closest station blind so we
                    // at least show something rather than an error wall.
                    chosen.add(candidates.first())
                }

                val fetched = coroutineScope {
                    chosen.map { station ->
                        async {
                            val a = async {
                                repository.getAnnualObservations(
                                    station.id, clientId, yearFrom, yearTo
                                )
                            }
                            val s = async { repository.getClimateStats(station.id, clientId) }
                            Triple(station, a.await(), s.await())
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
                            maxSnowDepth = existing.maxSnowDepth ?: obs.maxSnowDepth
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

    private data class RankedStation(
        val station: WeatherStation,
        val score: Double,
        val coveredElements: Set<String>,
        val historyYears: Int
    )

    private fun rankStations(
        candidates: List<WeatherStation>,
        byStation: Map<String, List<TimeSeriesAvailability>>,
        currentYear: Int
    ): List<RankedStation> {
        return candidates.map { station ->
            val available = byStation[station.id].orEmpty()
            val covered = available.map { it.elementId }.toSet()
            // Earliest validFrom year across the elements we care about gives
            // us a rough "history span". A station with 90 years of temp beats
            // a station with 5.
            val earliestFromYear = available.minOfOrNull { it.validFromYear }
            val historyYears = earliestFromYear
                ?.let { (currentYear - it).coerceAtLeast(0) }
                ?: 0
            val distanceKm = (station.distanceMeters ?: 0) / 1000.0
            val score = covered.size * 1000.0 + historyYears.toDouble() - distanceKm
            RankedStation(station, score, covered, historyYears)
        }.sortedByDescending { it.score }
    }

    // Greedy set-cover: take the top-ranked station, then add stations only if
    // they fill in metrics nobody before them had. Single-station result for
    // canonical places (Oslo -> Blindern). Multi-station only when forced.
    private fun pickStationsForCoverage(ranked: List<RankedStation>): MutableList<WeatherStation> {
        val chosen = mutableListOf<WeatherStation>()
        val coveredSoFar = mutableSetOf<String>()
        for (entry in ranked) {
            if (entry.coveredElements.isEmpty()) continue
            val newCoverage = entry.coveredElements - coveredSoFar
            val isPrimary = chosen.isEmpty()
            if (newCoverage.isEmpty() && !isPrimary) continue
            chosen.add(entry.station)
            coveredSoFar.addAll(entry.coveredElements)
            if (coveredSoFar.containsAll(targetElements)) break
        }
        return chosen
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
