package no.uio.ifi.in2000.team45

import no.uio.ifi.in2000.team45.data.statistic.StatisticDatasource
import org.junit.Test
import org.junit.Assert.*

class StatisticDatasourceTest {

    @Test
    fun testParseStationsShouldReturnListOfWeatherStation() {
        val datasource = StatisticDatasource()

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
}