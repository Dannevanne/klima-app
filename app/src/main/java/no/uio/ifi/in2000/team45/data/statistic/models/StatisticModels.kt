package no.uio.ifi.in2000.team45.data.statistic.models

data class WeatherStation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val municipality: String = "",
    val county: String = "",
    val distanceMeters: Int? = null
)

data class Place(
    val name: String,
    val subtitle: String,
    val latitude: Double,
    val longitude: Double,
    // Stations already known to belong to this place (e.g., grouped from a
    // Frost name match like "OSLO - BLINDERN", "OSLO - VINDERN"). Empty for
    // places that come from geocoding only.
    val stations: List<WeatherStation> = emptyList()
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
    val maxSnowDepth: Double? = null,
    // Count of days with daily precipitation >= the heavy-rain threshold.
    // Derived in the repository from sum(precipitation_amount P1D) data.
    val heavyPrecipDays: Int? = null
)

data class TimeSeriesAvailability(
    val stationId: String,
    val elementId: String,
    val validFromYear: Int,
    val validToYear: Int?
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