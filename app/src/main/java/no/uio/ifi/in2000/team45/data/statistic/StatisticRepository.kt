package no.uio.ifi.in2000.team45.data.statistic

class StatisticRepository(
    private val datasource: StatisticDatasource = StatisticDatasource()
) {

    suspend fun searchStations(query: String, clientId: String): List<WeatherStation> {
        return datasource.searchStations(query, clientId)
    }

    suspend fun findNearestStation(lat: Double, lon: Double, clientId: String): WeatherStation? {
        return datasource.findNearestStation(lat, lon, clientId)
    }

    suspend fun findNearestStations(
        lat: Double, lon: Double, clientId: String, count: Int = 5
    ): List<WeatherStation> {
        return datasource.findNearestStations(lat, lon, clientId, count)
    }

    suspend fun getLatestObservations(stationId: String, clientId: String): Map<String, Double> {
        return datasource.getLatestObservations(stationId, clientId)
    }

    suspend fun getAnnualObservations(
        stationId: String,
        clientId: String,
        yearFrom: Int,
        yearTo: Int
    ): List<AnnualObservation> {
        val annual = datasource.getAnnualObservations(
            stationId, clientId, yearFrom, yearTo
        )
        // Frost has no P1Y aggregations for wind/snow. Pull monthly maxes
        // and roll them up to per-year maxes here, then merge.
        val monthly = datasource.getMonthlyWindAndSnow(
            stationId, clientId, yearFrom, yearTo
        )
        if (monthly.isEmpty()) return annual

        data class YearAgg(
            var maxGust: Double? = null,
            var maxWind: Double? = null,
            var maxSnow: Double? = null
        )
        val byYear = mutableMapOf<Int, YearAgg>()
        for (m in monthly) {
            val year = m.month / 100
            val agg = byYear.getOrPut(year) { YearAgg() }
            when {
                m.elementId.contains("wind_speed_of_gust") ->
                    agg.maxGust = maxOfNullable(agg.maxGust, m.value)
                m.elementId.contains("wind_speed") ->
                    agg.maxWind = maxOfNullable(agg.maxWind, m.value)
                m.elementId.contains("surface_snow_thickness") ->
                    agg.maxSnow = maxOfNullable(agg.maxSnow, m.value)
            }
        }

        val byYearAnnual = annual.associateBy { it.year }.toMutableMap()
        for ((year, agg) in byYear) {
            val existing = byYearAnnual[year] ?: AnnualObservation(year, null, null)
            byYearAnnual[year] = existing.copy(
                maxWindGust = agg.maxGust,
                maxWindSpeed = agg.maxWind,
                maxSnowDepth = agg.maxSnow
            )
        }
        return byYearAnnual.values.sortedBy { it.year }
    }

    private fun maxOfNullable(a: Double?, b: Double?): Double? {
        return when {
            a == null -> b
            b == null -> a
            else -> maxOf(a, b)
        }
    }

    suspend fun getClimateStats(
        stationId: String,
        clientId: String
    ): List<MonthlyClimateStats> {
        val normals = datasource.getClimateNormals(stationId, clientId)

        if (normals.isNotEmpty()) {
            return buildStatsFromNormals(normals)
        }

        val observations = datasource.getMonthlyObservations(
            stationId, clientId,
            yearFrom = 2010,
            yearTo = 2024
        )
        return buildStatsFromObservations(observations)
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
                }?.normal,
                recordHigh = monthData.find {
                    it.elementId.contains("record_high")
                }?.normal,
                recordLow = monthData.find {
                    it.elementId.contains("record_low")
                }?.normal,
                snowDepth = monthData.find {
                    it.elementId.contains("surface_snow_thickness")
                }?.normal,
                windGust = monthData.find {
                    it.elementId.contains("wind_speed_of_gust")
                }?.normal,
                cloudCover = monthData.find {
                    it.elementId.contains("cloud_area_fraction")
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
