package no.uio.ifi.in2000.team45.data.statistic

data class StationAssignment(
    val station: WeatherStation,
    val elements: Set<String>
)

object StationPicker {

    // Picks the smallest set of stations to cover targetElements for a place,
    // and tags each with the elements it should provide -- so callers fetch
    // only what they need from each.
    //
    // Candidates must already be sorted nearest-first and filtered to the
    // "reasonably close" radius (the caller's distance cap).
    //
    // Phase 1: scan nearest-first. The first station that covers all target
    // elements with at least minHistoryYears of history wins alone (Oslo ->
    // Blindern, since Blindern is in the candidate list and has all four
    // metrics with ~88 years).
    //
    // Phase 2: if no single perfect station exists, pick per element the
    // station with the longest history. Each station ends up tagged with just
    // the elements it was the best fit for. Ties go to the closer station
    // because the input is nearest-first.
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
