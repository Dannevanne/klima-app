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

    @Test
    fun testParseGeocodeFeaturesReturnsListOfPlace() {
        val body = """
            {
              "features": [
                {
                  "properties": {
                    "name": "Oslo",
                    "place_formatted": "Oslo, Norge"
                  },
                  "geometry": {
                    "coordinates": [10.75, 59.91]
                  }
                }
              ]
            }
        """.trimIndent()

        val result = datasource.parseGeocodeFeatures(body)

        assertEquals(1, result.size)
        assertEquals("Oslo", result[0].name)
        assertEquals("Oslo, Norge", result[0].subtitle)
        assertEquals(59.91, result[0].latitude, 0.01)
        assertEquals(10.75, result[0].longitude, 0.01)
    }

    @Test
    fun testParseGeocodeFeaturesEmptyArrayReturnsEmpty() {
        val body = """{ "features": [] }"""
        assertTrue(datasource.parseGeocodeFeatures(body).isEmpty())
    }

    @Test
    fun testParseGeocodeFeaturesMissingFeaturesKey() {
        val body = """{ "type": "FeatureCollection" }"""
        assertTrue(datasource.parseGeocodeFeatures(body).isEmpty())
    }

    @Test
    fun testParseGeocodeFeaturesSkipsMalformed() {
        // Real-world: a feature missing geometry, another missing name.
        val body = """
            {
              "features": [
                {
                  "properties": { "name": "Ibestad", "place_formatted": "Troms" },
                  "geometry": { "coordinates": [17.13, 68.78] }
                },
                {
                  "properties": { "name": "NoGeometry" }
                },
                {
                  "geometry": { "coordinates": [10.0, 60.0] }
                }
              ]
            }
        """.trimIndent()

        val result = datasource.parseGeocodeFeatures(body)
        assertEquals(1, result.size)
        assertEquals("Ibestad", result[0].name)
    }

    @Test
    fun testParseGeocodeFeaturesMissingPlaceFormattedDefaultsToBlank() {
        val body = """
            {
              "features": [
                {
                  "properties": { "name": "Oslo" },
                  "geometry": { "coordinates": [10.75, 59.91] }
                }
              ]
            }
        """.trimIndent()

        val result = datasource.parseGeocodeFeatures(body)
        assertEquals(1, result.size)
        assertEquals("", result[0].subtitle)
    }
}