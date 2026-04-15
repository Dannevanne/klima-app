package no.uio.ifi.in2000.team45

import no.uio.ifi.in2000.team45.data.statistic.StatisticDatasource
import org.junit.Test
import org.junit.Assert.*

class StatisticDatasourceTest {

    private val datasource = StatisticDatasource()

    @Test
    fun testParseStationsShouldReturnListOfWeatherStation() {
        val dummyDatasource = StatisticDatasource()

        val dummyData = """
            {
              "data": [
                {
                  "id": "SN1",
                  "name": "Oslo",
                  "geometry": {
                    "coordinates": [10.75, 59.91]
                  },
                  "municipality": "Oslo",
                  "county": "Oslo"
                }
              ]
            }
        """.trimIndent()

        val result = datasource.parseStations(dummyData)

        assertEquals(1, result.size)
        assertEquals("Oslo", result[0].name)
    }

    @Test
    fun testParseStationsMultipleResults() {
        val dummyData = """
            {
              "data": [
                {
                  "id": "SN1",
                  "name": "Oslo",
                  "geometry": { "coordinates": [10.75, 59.91] },
                  "municipality": "Oslo",
                  "county": "Oslo"
                },
                {
                  "id": "SN2",
                  "name": "Bergen",
                  "geometry": { "coordinates": [5.33, 60.39] },
                  "municipality": "Bergen",
                  "county": "Vestland"
                }
              ]
            }
        """.trimIndent()

        val result = datasource.parseStations(dummyData)

        assertEquals(2, result.size)
        assertEquals("SN2", result[1].id)
        assertEquals(60.39, result[1].latitude, 0.01)
    }

    @Test
    fun testParseStationsEmptyDataReturnsEmptyList() {
        val dummyData = """{ "data": [] }"""
        val result = datasource.parseStations(dummyData)
        assertTrue(result.isEmpty())
    }

    @Test
    fun testParseStationsSkipsMalformedEntries() {
        val dummyData = """
            {
              "data": [
                {
                  "id": "SN1",
                  "name": "Oslo",
                  "geometry": { "coordinates": [10.75, 59.91] }
                },
                {
                  "name": "Missing ID"
                }
              ]
            }
        """.trimIndent()

        val result = datasource.parseStations(dummyData)
        assertEquals(1, result.size)
    }
}