package no.uio.ifi.in2000.team45.data.statistic

object StationPicker {

    // Picks the smallest set of stations to cover targetElements for a place.
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
    // station with the longest history. Ties go to the closer station because
    // the input is nearest-first.
    fun pickStationsForPlace(
        candidates: List<WeatherStation>,
        availability: Map<String, List<TimeSeriesAvailability>>,
        targetElements: List<String>,
        currentYear: Int,
        minHistoryYears: Int = 50
    ): List<WeatherStation> {
        if (candidates.isEmpty() || targetElements.isEmpty()) return emptyList()

        for (station in candidates) {
            val avail = availability[station.id].orEmpty()
            val covered = avail.map { it.elementId }.toSet()
            if (!covered.containsAll(targetElements)) continue
            val history = (currentYear - avail.minOf { it.validFromYear })
                .coerceAtLeast(0)
            if (history >= minHistoryYears) return listOf(station)
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

        val chosenIds = bestPerElement.values.map { it.id }.toSet()
        return candidates.filter { it.id in chosenIds }
    }
}
