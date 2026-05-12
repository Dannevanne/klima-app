package no.uio.ifi.in2000.team45.data.statistic.utils

import no.uio.ifi.in2000.team45.data.statistic.models.Place

/**
 * Utility responsible for selecting the best matching Place from a list of candidates
 * based on a user search query.
 */
object PlaceSelector {

    /**
     * Selects the most relevant Place match for a given query using some matching rules.
     *
     * @param query The user search query.
     * @param candidates List of candidate places returned from search.
     * @return The best matching Place, or null if the list is empty.
     */
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