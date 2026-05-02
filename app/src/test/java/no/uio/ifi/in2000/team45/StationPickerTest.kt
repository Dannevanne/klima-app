package no.uio.ifi.in2000.team45

import no.uio.ifi.in2000.team45.data.statistic.StationAssignment
import no.uio.ifi.in2000.team45.data.statistic.StationPicker
import no.uio.ifi.in2000.team45.data.statistic.TimeSeriesAvailability
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StationPickerTest {

    private val temp = "mean(air_temperature P1Y)"
    private val precip = "sum(precipitation_amount P1Y)"
    private val gust = "max(wind_speed_of_gust P1M)"
    private val snow = "max(surface_snow_thickness P1M)"
    private val all = listOf(temp, precip, gust, snow)
    private val currentYear = 2026

    private fun station(id: String, distanceMeters: Int) = WeatherStation(
        id = id,
        name = id,
        latitude = 0.0,
        longitude = 0.0,
        municipality = "",
        county = "",
        distanceMeters = distanceMeters
    )

    private fun avail(stationId: String, vararg pairs: Pair<String, Int>) =
        pairs.map { (element, fromYear) ->
            TimeSeriesAvailability(
                stationId = stationId,
                elementId = element,
                validFromYear = fromYear,
                validToYear = null
            )
        }

    private fun stationsOf(result: List<StationAssignment>) = result.map { it.station }

    @Test
    fun emptyCandidatesReturnsEmpty() {
        val result = StationPicker.pickStationsForPlace(
            candidates = emptyList(),
            availability = emptyMap(),
            targetElements = all,
            currentYear = currentYear
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun emptyTargetElementsReturnsEmpty() {
        val result = StationPicker.pickStationsForPlace(
            candidates = listOf(station("SN1", 100)),
            availability = mapOf("SN1" to avail("SN1", temp to 1990)),
            targetElements = emptyList(),
            currentYear = currentYear
        )
        assertTrue(result.isEmpty())
    }

    // Oslo: 4 stations, Blindern has all four with 1937 start (89 yr).
    // Closer sensors have partial coverage. Algorithm should return Blindern
    // alone -- the spec example.
    @Test
    fun osloPicksBlindernAlone() {
        val sensorA = station("SN-A", 200)   // closest
        val blindern = station("SN-BLINDERN", 2_100)
        val sensorC = station("SN-C", 4_500)
        val sensorD = station("SN-D", 8_900)

        val candidates = listOf(sensorA, blindern, sensorC, sensorD)
        val availability = mapOf(
            "SN-A" to avail("SN-A", temp to 2010),                // 16 yr temp only
            "SN-BLINDERN" to avail(
                "SN-BLINDERN",
                temp to 1937, precip to 1937, gust to 1955, snow to 1955
            ),
            "SN-C" to avail("SN-C", precip to 1980, gust to 1990),
            "SN-D" to avail("SN-D", snow to 1970)
        )

        val result = StationPicker.pickStationsForPlace(
            candidates, availability, all, currentYear
        )
        assertEquals(listOf(blindern), stationsOf(result))
        assertEquals(all.toSet(), result.first().elements)
    }

    // The user said: if the nearest already covers everything with 50 yr, end
    // there. So even if a farther station has more history, the closer
    // "perfect" station wins.
    @Test
    fun nearestPerfectStationEndsAlgorithm() {
        val nearby = station("SN-NEAR", 800)
        val historic = station("SN-HISTORIC", 4_000)

        val candidates = listOf(nearby, historic)
        val availability = mapOf(
            "SN-NEAR" to avail(
                "SN-NEAR",
                temp to 1960, precip to 1960, gust to 1960, snow to 1960
            ),
            "SN-HISTORIC" to avail(
                "SN-HISTORIC",
                temp to 1900, precip to 1900, gust to 1900, snow to 1900
            )
        )

        val result = StationPicker.pickStationsForPlace(
            candidates, availability, all, currentYear
        )
        assertEquals(listOf(nearby), stationsOf(result))
        assertEquals(all.toSet(), result.first().elements)
    }

    // Bergen-like: a station 4 km away has all four with a long history. The
    // truly closest is a single-purpose precip sensor.
    @Test
    fun bergenPicksFloridaOverPrecipOnlySensor() {
        val precipOnly = station("SN-PRECIP", 300)
        val florida = station("SN-FLORIDA", 4_000)

        val candidates = listOf(precipOnly, florida)
        val availability = mapOf(
            "SN-PRECIP" to avail("SN-PRECIP", precip to 1990),
            "SN-FLORIDA" to avail(
                "SN-FLORIDA",
                temp to 1949, precip to 1949, gust to 1957, snow to 1957
            )
        )

        val result = StationPicker.pickStationsForPlace(
            candidates, availability, all, currentYear
        )
        assertEquals(listOf(florida), stationsOf(result))
        assertEquals(all.toSet(), result.first().elements)
    }

    // No single station covers all four with 50 yr, so we drop to phase 2:
    // pick per element the station with the longest history. Three stations
    // contribute, in distance order, each tagged with just its own elements.
    @Test
    fun noPerfectStationFallsToPerElementCoverage() {
        val near = station("SN-NEAR", 1_000)    // temp + precip, 30 yr
        val mid = station("SN-MID", 5_000)      // gust, 60 yr
        val far = station("SN-FAR", 12_000)     // snow, 70 yr

        val candidates = listOf(near, mid, far)
        val availability = mapOf(
            "SN-NEAR" to avail("SN-NEAR", temp to 1996, precip to 1996),
            "SN-MID" to avail("SN-MID", gust to 1966),
            "SN-FAR" to avail("SN-FAR", snow to 1956)
        )

        val result = StationPicker.pickStationsForPlace(
            candidates, availability, all, currentYear
        )
        assertEquals(listOf(near, mid, far), stationsOf(result))
        assertEquals(setOf(temp, precip), result[0].elements)
        assertEquals(setOf(gust), result[1].elements)
        assertEquals(setOf(snow), result[2].elements)
    }

    // A station has full coverage but only 40 yr of history -- below the
    // perfect threshold. Phase 1 skips it. Phase 2 still picks it for every
    // element (no other candidate has anything).
    @Test
    fun fullCoverageButShortHistoryFallsToPhase2() {
        val onlyOne = station("SN-ONLY", 2_000)
        val candidates = listOf(onlyOne)
        val availability = mapOf(
            "SN-ONLY" to avail(
                "SN-ONLY",
                temp to 1990, precip to 1990, gust to 1990, snow to 1990
            )
        )

        val result = StationPicker.pickStationsForPlace(
            candidates, availability, all, currentYear
        )
        assertEquals(listOf(onlyOne), stationsOf(result))
        assertEquals(all.toSet(), result.first().elements)
    }

    // Tiny village with a single short-history station. Algorithm covers
    // whatever it can; phase 2 fills the elements the station does have, and
    // omits the rest.
    @Test
    fun smallVillageWithSingleLimitedStation() {
        val onlyOne = station("SN-VILLAGE", 1_500)
        val candidates = listOf(onlyOne)
        val availability = mapOf(
            "SN-VILLAGE" to avail("SN-VILLAGE", temp to 2018)  // 8 yr, temp only
        )

        val result = StationPicker.pickStationsForPlace(
            candidates, availability, all, currentYear
        )
        assertEquals(listOf(onlyOne), stationsOf(result))
        assertEquals(setOf(temp), result.first().elements)
    }

    // A reasonably sized town: closer station has temp 30 yr; a farther one
    // has temp 60 yr. Phase 2 prefers the longer history (within distance
    // limit, both are reasonable).
    @Test
    fun longerHistoryWinsOverCloserShortHistory() {
        val close = station("SN-CLOSE", 500)
        val historic = station("SN-HISTORIC", 6_000)

        val candidates = listOf(close, historic)
        val availability = mapOf(
            "SN-CLOSE" to avail("SN-CLOSE", temp to 1996),
            "SN-HISTORIC" to avail("SN-HISTORIC", temp to 1966)
        )

        val result = StationPicker.pickStationsForPlace(
            candidates, availability, listOf(temp), currentYear
        )
        assertEquals(listOf(historic), stationsOf(result))
        assertEquals(setOf(temp), result.first().elements)
    }

    // Two stations have the exact same history for the only target element.
    // The closer one wins because the input is nearest-first.
    @Test
    fun tieOnHistoryClosestStationWins() {
        val close = station("SN-CLOSE", 800)
        val far = station("SN-FAR", 4_000)

        val candidates = listOf(close, far)
        val availability = mapOf(
            "SN-CLOSE" to avail("SN-CLOSE", temp to 1980),
            "SN-FAR" to avail("SN-FAR", temp to 1980)
        )

        val result = StationPicker.pickStationsForPlace(
            candidates, availability, listOf(temp), currentYear
        )
        assertEquals(listOf(close), stationsOf(result))
        assertEquals(setOf(temp), result.first().elements)
    }

    // Candidates without availability entries are skipped silently. Models
    // the case where Frost knows the station exists but has nothing to serve
    // for our four target elements.
    @Test
    fun candidatesWithoutAvailabilityAreIgnored() {
        val ghost = station("SN-GHOST", 500)
        val real = station("SN-REAL", 2_000)

        val candidates = listOf(ghost, real)
        val availability = mapOf(
            "SN-REAL" to avail(
                "SN-REAL",
                temp to 1937, precip to 1937, gust to 1937, snow to 1937
            )
        )

        val result = StationPicker.pickStationsForPlace(
            candidates, availability, all, currentYear
        )
        assertEquals(listOf(real), stationsOf(result))
        assertEquals(all.toSet(), result.first().elements)
    }

    // When availability is empty for everyone, we get nothing back. The VM
    // falls back to the closest candidate blind in that case (caller's job).
    @Test
    fun noAvailabilityForAnyoneReturnsEmpty() {
        val candidates = listOf(station("SN-1", 500), station("SN-2", 1_500))
        val result = StationPicker.pickStationsForPlace(
            candidates, emptyMap(), all, currentYear
        )
        assertTrue(result.isEmpty())
    }

    // Bigger Oslo-style scenario: 8 stations with mixed coverage. Blindern
    // sits at index 4 in distance order. Phase 1 must scan past 0-3 (none
    // perfect) and stop at Blindern.
    @Test
    fun manyStationsAlgorithmStopsAtFirstPerfect() {
        val sensors = listOf(
            station("SN-1", 100) to avail("SN-1", temp to 2015),
            station("SN-2", 300) to avail("SN-2", precip to 2010),
            station("SN-3", 700) to avail("SN-3", temp to 1990, precip to 1990),
            station("SN-4", 1_500) to avail("SN-4", gust to 2000),
            station("SN-BLINDERN", 2_100) to avail(
                "SN-BLINDERN",
                temp to 1937, precip to 1937, gust to 1955, snow to 1955
            ),
            station("SN-6", 3_000) to avail(
                "SN-6",
                temp to 1900, precip to 1900, gust to 1900, snow to 1900
            )  // even older but past Blindern; should not be reached
        )

        val candidates = sensors.map { it.first }
        val availability = sensors.associate { it.first.id to it.second }

        val result = StationPicker.pickStationsForPlace(
            candidates, availability, all, currentYear
        )
        assertEquals(listOf(candidates[4]), stationsOf(result))  // Blindern
        assertEquals(all.toSet(), result.first().elements)
    }

    // Result preserves distance order when phase 2 picks multiple stations.
    @Test
    fun phase2ResultsAreInDistanceOrder() {
        val nearTemp = station("SN-NEAR", 500)
        val farPrecip = station("SN-FAR", 8_000)
        val midGust = station("SN-MID", 3_000)

        // candidates as given to the picker, already nearest-first
        val candidates = listOf(nearTemp, midGust, farPrecip)
        val availability = mapOf(
            "SN-NEAR" to avail("SN-NEAR", temp to 1990),
            "SN-MID" to avail("SN-MID", gust to 1990),
            "SN-FAR" to avail("SN-FAR", precip to 1990, snow to 1990)
        )

        val result = StationPicker.pickStationsForPlace(
            candidates, availability, all, currentYear
        )
        assertEquals(listOf(nearTemp, midGust, farPrecip), stationsOf(result))
        assertEquals(setOf(temp), result[0].elements)
        assertEquals(setOf(gust), result[1].elements)
        assertEquals(setOf(precip, snow), result[2].elements)
    }

    // Only a subset of elements is requested. Phase 1 succeeds as soon as a
    // station covers that subset with enough history.
    @Test
    fun subsetOfTargetsStillObeysPhase1() {
        val nearby = station("SN-NEAR", 600)
        val candidates = listOf(nearby)
        val availability = mapOf(
            "SN-NEAR" to avail("SN-NEAR", temp to 1970, precip to 1970)
        )

        val result = StationPicker.pickStationsForPlace(
            candidates, availability, listOf(temp, precip), currentYear
        )
        assertEquals(listOf(nearby), stationsOf(result))
        assertEquals(setOf(temp, precip), result.first().elements)
    }

    // Stavanger-like: Vaaland has 3 of 4 metrics with deep history; gust
    // lives only at Sola airport. Vaaland gets temp+precip+snow, Sola gets
    // gust alone.
    @Test
    fun stavangerSplitsVaalandAndSolaByElement() {
        val vaaland = station("SN44640", 0)
        val sola = station("SN44560", 9_500)

        val candidates = listOf(vaaland, sola)
        val availability = mapOf(
            "SN44640" to avail(
                "SN44640",
                temp to 1927, precip to 1895, snow to 1902
            ),
            "SN44560" to avail(
                "SN44560",
                temp to 1935, precip to 1935, gust to 1982, snow to 1935
            )
        )

        val result = StationPicker.pickStationsForPlace(
            candidates, availability, all, currentYear
        )
        assertEquals(listOf(vaaland, sola), stationsOf(result))
        assertEquals(setOf(temp, precip, snow), result[0].elements)
        assertEquals(setOf(gust), result[1].elements)
    }

    // Real Frost availability has multiple time-series entries per element
    // when sensors get reconfigured (Vaaland has 5 temp entries from 1927,
    // 1937, 1953, 2008, 2019). Phase 1 must use the oldest start per element,
    // not the latest, otherwise it sees Vaaland as a 7-yr station.
    @Test
    fun phase1HandlesMultipleTimeSeriesPerElement() {
        val vaaland = station("SN44640", 0)
        val candidates = listOf(vaaland)
        // Sensor reconfigurations give Vaaland 5 separate temp entries
        val availability = mapOf(
            "SN44640" to listOf(
                TimeSeriesAvailability("SN44640", temp, 1927, null),
                TimeSeriesAvailability("SN44640", temp, 1937, null),
                TimeSeriesAvailability("SN44640", temp, 1953, null),
                TimeSeriesAvailability("SN44640", temp, 2008, null),
                TimeSeriesAvailability("SN44640", temp, 2019, null),
                TimeSeriesAvailability("SN44640", precip, 1895, null),
                TimeSeriesAvailability("SN44640", gust, 1960, null),
                TimeSeriesAvailability("SN44640", snow, 1902, null)
            )
        )

        val result = StationPicker.pickStationsForPlace(
            candidates, availability, all, currentYear
        )
        assertEquals(listOf(vaaland), stationsOf(result))
    }

    // Real-world Stavanger probe: 20 candidates, multi-series per element on
    // Vaaland and Sola. Only Sola covers all 4; gust there starts in 1982
    // (44 yr) so Phase 1 fails, Phase 2 should split Vaaland (temp/precip/
    // snow) and Sola (gust).
    @Test
    fun stavangerRealAvailabilityAssignsSolaToGustAlone() {
        val vaaland = station("SN44640", 5_100)
        val sola = station("SN44560", 12_677)
        val tasta = station("SN44620", 935)       // closest, precip-only
        val hundvag = station("SN44660", 2_120)   // precip-only
        val madla = station("SN44580", 5_349)     // precip-only
        val kvitsoy = station("SN44610", 17_561)  // all 4 but very young

        val candidates = listOf(tasta, hundvag, vaaland, madla, sola, kvitsoy)
        val availability = mapOf(
            "SN44640" to listOf(
                TimeSeriesAvailability("SN44640", temp, 1927, null),
                TimeSeriesAvailability("SN44640", temp, 1937, null),
                TimeSeriesAvailability("SN44640", temp, 1953, null),
                TimeSeriesAvailability("SN44640", temp, 2008, null),
                TimeSeriesAvailability("SN44640", temp, 2019, null),
                TimeSeriesAvailability("SN44640", precip, 1895, null),
                TimeSeriesAvailability("SN44640", snow, 1902, null)
            ),
            "SN44560" to listOf(
                TimeSeriesAvailability("SN44560", temp, 1935, null),
                TimeSeriesAvailability("SN44560", temp, 1952, null),
                TimeSeriesAvailability("SN44560", precip, 1935, null),
                TimeSeriesAvailability("SN44560", gust, 1982, null),
                TimeSeriesAvailability("SN44560", snow, 1935, null)
            ),
            "SN44620" to listOf(TimeSeriesAvailability("SN44620", precip, 1999, null)),
            "SN44660" to listOf(TimeSeriesAvailability("SN44660", precip, 1999, null)),
            "SN44580" to listOf(TimeSeriesAvailability("SN44580", precip, 1999, null)),
            "SN44610" to listOf(
                TimeSeriesAvailability("SN44610", temp, 2005, null),
                TimeSeriesAvailability("SN44610", precip, 2006, null),
                TimeSeriesAvailability("SN44610", gust, 2005, null),
                TimeSeriesAvailability("SN44610", snow, 2008, null)
            )
        )

        val result = StationPicker.pickStationsForPlace(
            candidates, availability, all, currentYear
        )
        // Vaaland gets temp/precip/snow, Sola gets gust. Order by distance
        // among picked: Vaaland (5.1km) before Sola (12.7km).
        val vaalandAssignment = result.firstOrNull { it.station == vaaland }
        val solaAssignment = result.firstOrNull { it.station == sola }
        assertEquals(setOf(temp, precip, snow), vaalandAssignment?.elements)
        assertEquals(setOf(gust), solaAssignment?.elements)
    }
}
