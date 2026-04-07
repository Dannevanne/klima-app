package no.uio.ifi.in2000.team45

import no.uio.ifi.in2000.team45.data.statistic.StatisticDatasource
import org.junit.Test
import org.junit.Assert.*

class StatisticDatasourceTest {

    // You can notice that the naming convention for the tests is different than
    // the one they use from "ukesoppgaver", because in the lecture they named liked this.
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