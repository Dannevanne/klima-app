package no.uio.ifi.in2000.team45.data.statistic

object StationPicker {

    private data class RankedStation(
        val station: WeatherStation,
        val score: Double,
        val coveredElements: Set<String>,
        val historyYears: Int
    )

    fun pickStationsForPlace(
        candidates: List<WeatherStation>,
        availability: Map<String, List<TimeSeriesAvailability>>,
        targetElements: List<String>,
        currentYear: Int
    ): List<WeatherStation> {
        val ranked = rank(candidates, availability, currentYear)
        return greedyCover(ranked, targetElements)
    }

    private fun rank(
        candidates: List<WeatherStation>,
        availability: Map<String, List<TimeSeriesAvailability>>,
        currentYear: Int
    ): List<RankedStation> {
        return candidates.map { station ->
            val available = availability[station.id].orEmpty()
            val covered = available.map { it.elementId }.toSet()
            val earliestFromYear = available.minOfOrNull { it.validFromYear }
            val historyYears = earliestFromYear
                ?.let { (currentYear - it).coerceAtLeast(0) }
                ?: 0
            val distanceKm = (station.distanceMeters ?: 0) / 1000.0
            val score = covered.size * 1000.0 + historyYears.toDouble() - distanceKm
            RankedStation(station, score, covered, historyYears)
        }.sortedByDescending { it.score }
    }

    private fun greedyCover(
        ranked: List<RankedStation>,
        targetElements: List<String>
    ): List<WeatherStation> {
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
}
