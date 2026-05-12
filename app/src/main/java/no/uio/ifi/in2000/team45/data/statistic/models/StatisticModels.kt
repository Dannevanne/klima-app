package no.uio.ifi.in2000.team45.data.statistic.models

/**
 * Represents a weather station with geographic location and metadata.
 *
 * @property id Unique identifier of the station.
 * @property name Name of the station.
 * @property latitude Latitude coordinate of the station.
 * @property longitude Longitude coordinate of the station.
 * @property municipality Municipality where the station is located.
 * @property county County where the station is located.
 * @property distanceMeters Optional distance to a reference point in meters.
 */
data class WeatherStation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val municipality: String = "",
    val county: String = "",
    val distanceMeters: Int? = null
)

/**
 * Represents a geographic place that can contain one or more weather stations.
 *
 * @property name Name of the place.
 * @property subtitle Additional descriptive text for the place.
 * @property latitude Latitude coordinate of the place.
 * @property longitude Longitude coordinate of the place.
 * @property stations List of weather stations associated with the place.
 */
data class Place(
    val name: String,
    val subtitle: String,
    val latitude: Double,
    val longitude: Double,
    val stations: List<WeatherStation> = emptyList()
)

/**
 * Represents a monthly climate normal value for a specific climate element.
 *
 * @property elementId Identifier of the climate element.
 * @property month Month of the year (1–12).
 * @property normal The long-term average value for the element.
 */
data class ClimateNormal(
    val elementId: String,
    val month: Int,
    val normal: Double
)

/**
 * Represents a monthly observed climate value for a specific element.
 *
 * @property elementId Identifier of the climate element.
 * @property month Month of the year (1–12).
 * @property value Observed value for the given month.
 */
data class MonthlyObservation(
    val elementId: String,
    val month: Int,
    val value: Double
)

/**
 * Represents aggregated annual climate observations for a station.
 *
 * @property year The observation year.
 * @property meanTemp Average temperature for the year.
 * @property totalPrecip Total precipitation for the year.
 * @property maxWindGust Maximum wind gust recorded in the year.
 * @property maxWindSpeed Maximum wind speed recorded in the year.
 * @property maxSnowDepth Maximum snow depth recorded in the year.
 * @property heavyPrecipDays Number of days with heavy precipitation.
 */
data class AnnualObservation(
    val year: Int,
    val meanTemp: Double?,
    val totalPrecip: Double?,
    val maxWindGust: Double? = null,
    val maxWindSpeed: Double? = null,
    val maxSnowDepth: Double? = null,
    val heavyPrecipDays: Int? = null
)

/**
 * Represents data availability for a specific climate element at a station.
 *
 * @property stationId Identifier of the station.
 * @property elementId Identifier of the climate element.
 * @property validFromYear First year with available data.
 * @property validToYear Last year with available data, or null if ongoing.
 */
data class TimeSeriesAvailability(
    val stationId: String,
    val elementId: String,
    val validFromYear: Int,
    val validToYear: Int?
)

/**
 * Represents monthly aggregated climate statistics for a station.
 *
 * @property month Month of the year (1–12).
 * @property meanTemp Average temperature for the month.
 * @property maxTemp Maximum temperature for the month.
 * @property minTemp Minimum temperature for the month.
 * @property precipitation Total precipitation for the month.
 * @property windSpeed Average wind speed for the month.
 * @property recordHigh Highest recorded temperature.
 * @property recordLow Lowest recorded temperature.
 * @property snowDepth Average or maximum snow depth.
 * @property windGust Maximum wind gust.
 * @property cloudCover Average cloud coverage.
 */
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