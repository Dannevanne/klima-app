package no.uio.ifi.in2000.team45

import no.uio.ifi.in2000.team45.data.statistic.models.Place
import no.uio.ifi.in2000.team45.data.statistic.utils.PlaceSelector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaceSelectorTest {

    private fun place(name: String) = Place(
        name = name,
        subtitle = "",
        latitude = 0.0,
        longitude = 0.0
    )

    @Test
    fun emptyCandidatesReturnsNull() {
        assertNull(PlaceSelector.pickBest("Oslo", emptyList()))
    }

    @Test
    fun blankQueryReturnsFirstCandidate() {
        val first = place("Oslo")
        val result = PlaceSelector.pickBest("  ", listOf(first, place("Bergen")))
        assertEquals(first, result)
    }

    @Test
    fun prefixMatchBeatsListOrder() {
        // Mapbox returned Abestad first by phonetic ranking; we want Ibestad
        // because the user typed "Ibestad".
        val candidates = listOf(place("Abestad"), place("Ibestad"))
        assertEquals("Ibestad", PlaceSelector.pickBest("Ibestad", candidates)?.name)
    }

    @Test
    fun caseInsensitivePrefix() {
        val candidates = listOf(place("Abestad"), place("IBESTAD"))
        assertEquals("IBESTAD", PlaceSelector.pickBest("ibestad", candidates)?.name)
    }

    @Test
    fun substringMatchWhenNoPrefix() {
        val candidates = listOf(place("Sentrum"), place("Indre Oslo"))
        assertEquals("Indre Oslo", PlaceSelector.pickBest("Oslo", candidates)?.name)
    }

    @Test
    fun fallsBackToFirstWhenNothingMatches() {
        val first = place("Sentrum")
        val candidates = listOf(first, place("Bergen"))
        assertEquals(first, PlaceSelector.pickBest("Trondheim", candidates))
    }

    @Test
    fun prefixWinsOverSubstring() {
        // "Lillestrom" starts with "Lille"; "Storlille" only contains it.
        val candidates = listOf(place("Storlille"), place("Lillestrom"))
        assertEquals("Lillestrom", PlaceSelector.pickBest("Lille", candidates)?.name)
    }

    @Test
    fun trimsQueryWhitespace() {
        val candidates = listOf(place("Abestad"), place("Ibestad"))
        assertEquals("Ibestad", PlaceSelector.pickBest("  Ibestad  ", candidates)?.name)
    }

    @Test
    fun exactMatchBeatsPrefix() {
        // Query "Os" matches "Oslo" by prefix and "Os" exactly. We want "Os".
        val candidates = listOf(place("Oslo"), place("Os"))
        assertEquals("Os", PlaceSelector.pickBest("Os", candidates)?.name)
    }
}
