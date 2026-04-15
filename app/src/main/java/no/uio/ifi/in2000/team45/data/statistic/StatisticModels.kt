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
    val totalPrecip: Double?
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