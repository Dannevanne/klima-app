package no.uio.ifi.in2000.team45.data.statistic

object PlaceSelector {

    // Mapbox Geocoding can return loosely matching candidates ahead of the
    // actual queried place ("Åbestad" before "Ibestad" for query "Ibestad").
    // Prefer exact match, then a candidate whose name starts with the query,
    // then one that contains it, falling back to Mapbox's first result.
    fun pickBest(query: String, candidates: List<Place>): Place? {
        if (candidates.isEmpty()) return null
        val q = query.trim().lowercase()
        if (q.isEmpty()) return candidates.first()
        return candidates.firstOrNull { it.name.lowercase() == q }
            ?: candidates.firstOrNull { it.name.lowercase().startsWith(q) }
            ?: candidates.firstOrNull { it.name.lowercase().contains(q) }
            ?: candidates.first()
    }
}
