package no.uio.ifi.in2000.team45.data.statistic

data class WeatherStation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val municipality: String = "",
    val county: String = "",
    val distanceMeters: Int? = null
)

data class ClimateNormal(
    val elementId: String,
    val month: Int,
    val normal: Double
)

data class MonthlyObservation(
    val elementId: String,
    val month: Int,
    val value: Double
)

data class AnnualObservation(
    val year: Int,
    val meanTemp: Double?,
    val totalPrecip: Double?,
    // Max single-month value within the year. Frost doesn't have P1Y
    // aggregations for these elements, so the repository computes the yearly
    // max from monthly data client-side.
    val maxWindGust: Double? = null,
    val maxWindSpeed: Double? = null,
    val maxSnowDepth: Double? = null
)

data class MonthlyClimateStats(
    val month: Int,
    val meanTemp: Double?,
    val maxTemp: Double?,
    val minTemp: Double?,
    val precipitation: Double?,
    val windSpeed: Double?,
    val recordHigh: Double? = null,
    val recordLow: Double? = null,
    val snowDepth: Double? = null,
    val windGust: Double? = null,
    val cloudCover: Double? = null
)