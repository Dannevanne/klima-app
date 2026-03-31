package no.uio.ifi.in2000.team45.data.statistic

data class MonthlyClimateStats(
    val month: Int,
    val meanTemp: Double?,
    val maxTemp: Double?,
    val minTemp: Double?,
    val precipitation: Double?,
    val windSpeed: Double?
)

class StatisticRepository(
    private val datasource: StatisticDatasource = StatisticDatasource()
) {

    suspend fun searchStations(query: String, clientId: String): List<WeatherStation> {
        return datasource.searchStations(query, clientId)
    }

    suspend fun getClimateStats(
        stationId: String,
        clientId: String
    ): List<MonthlyClimateStats> {
        return List(12) {
            MonthlyClimateStats(it + 1, null, null, null, null, null)
        }
    }

    private fun buildStatsFromNormals(normals: List<ClimateNormal>): List<MonthlyClimateStats> {
        val byMonth = normals.groupBy { it.month }

        return (1..12).map { month ->
            val monthData = byMonth[month] ?: emptyList()
            MonthlyClimateStats(
                month = month,
                meanTemp = monthData.find {
                    it.elementId.contains("mean") && it.elementId.contains("air_temperature")
                }?.normal,
                maxTemp = monthData.find {
                    it.elementId.contains("max") && it.elementId.contains("air_temperature")
                }?.normal,
                minTemp = monthData.find {
                    it.elementId.contains("min") && it.elementId.contains("air_temperature")
                }?.normal,
                precipitation = monthData.find {
                    it.elementId.contains("precipitation")
                }?.normal,
                windSpeed = monthData.find {
                    it.elementId.contains("wind_speed")
                }?.normal
            )
        }
    }

    private fun buildStatsFromObservations(
        observations: List<MonthlyObservation>
    ): List<MonthlyClimateStats> {
        val byMonth = observations.groupBy { it.month }

        return (1..12).map { month ->
            val monthData = byMonth[month] ?: emptyList()

            val temps = monthData.filter {
                it.elementId.contains("air_temperature")
            }
            val precip = monthData.filter {
                it.elementId.contains("precipitation")
            }

            MonthlyClimateStats(
                month = month,
                meanTemp = temps.map { it.value }.average().takeIf { temps.isNotEmpty() },
                maxTemp = null,
                minTemp = null,
                precipitation = precip.map { it.value }.average().takeIf { precip.isNotEmpty() },
                windSpeed = null
            )
        }
    }
}
