package no.uio.ifi.in2000.team45.data.statistic.utils

import no.uio.ifi.in2000.team45.data.statistic.models.TimeSeriesAvailability
import no.uio.ifi.in2000.team45.data.statistic.models.WeatherStation
import kotlin.collections.iterator

/**
 * Represents a mapping between a weather station and the climate elements
 * it is responsible for providing data for.
 *
 * @property station The selected weather station.
 * @property elements Set of climate element IDs assigned to this station.
 */
data class StationAssignment(
    val station: WeatherStation,
    val elements: Set<String>
)

/**
 * Utility responsible for selecting optimal weather stations for a place
 * based on data availability and station history coverage.
 */
object StationPicker {

    /**
     * Selects the most suitable weather stations to cover the requested climate elements.
     *
     * The selection process works in two phases:
     * 1. Attempts to find a single station that covers all target elements
     * with sufficient historical data.
     * 2. If no single station qualifies, assigns the best station per element
     * based on longest available history.
     *
     * @param candidates List of candidate weather stations sorted by proximity.
     * @param availability Map of station IDs to available time series metadata.
     * @param targetElements List of required climate element IDs.
     * @param currentYear Current year used for calculating data history length.
     * @param minHistoryYears Minimum required years of historical data.
     * @return A list of station assignments covering the requested elements.
     */
    fun pickStationsForPlace(
        candidates: List<WeatherStation>,
        availability: Map<String, List<TimeSeriesAvailability>>,
        targetElements: List<String>,
        currentYear: Int,
        minHistoryYears: Int = 50
    ): List<StationAssignment> {
        if (candidates.isEmpty() || targetElements.isEmpty()) return emptyList()

        for (station in candidates) {
            val avail = availability[station.id].orEmpty()
            val covered = avail.map { it.elementId }.toSet()
            if (!covered.containsAll(targetElements)) continue
            // Use the shortest per-element history. Otherwise, Sola passes
            // Phase 1 for Stavanger because its temp goes back to 1935, even
            // though gust starts in 1982 (44 yr -- under the 50 yr threshold).
            val targets = avail.filter { it.elementId in targetElements }
            val history = (currentYear - targets.maxOf { it.validFromYear })
                .coerceAtLeast(0)
            if (history >= minHistoryYears) {
                return listOf(StationAssignment(station, targetElements.toSet()))
            }
        }

        val bestPerElement = mutableMapOf<String, WeatherStation>()
        for (element in targetElements) {
            var bestStation: WeatherStation? = null
            var bestHistory = -1
            for (station in candidates) {
                val match = availability[station.id]
                    ?.firstOrNull { it.elementId == element } ?: continue
                val history = (currentYear - match.validFromYear).coerceAtLeast(0)
                if (history > bestHistory) {
                    bestStation = station
                    bestHistory = history
                }
            }
            if (bestStation != null) bestPerElement[element] = bestStation
        }

        val elementsByStation = mutableMapOf<String, MutableSet<String>>()
        for ((element, station) in bestPerElement) {
            elementsByStation.getOrPut(station.id) { mutableSetOf() }.add(element)
        }
        return candidates.mapNotNull { c ->
            elementsByStation[c.id]?.let { StationAssignment(c, it) }
        }
    }
}
