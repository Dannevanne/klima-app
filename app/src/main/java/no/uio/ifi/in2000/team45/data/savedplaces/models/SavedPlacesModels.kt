package no.uio.ifi.in2000.team45.data.savedplaces.models

/**
 * Represents a saved favorite weather station.
 *
 * @property stationId Unique identifier of the station.
 * @property name Name of the station.
 * @property municipality Municipality where the station is located.
 * @property county County where the station is located.
 */
data class FavoriteStation(
    val stationId: String,
    val name: String,
    val municipality: String = "",
    val county: String = ""
)

/**
 * Represents a saved location on the map.
 *
 * @property latitude Latitude coordinate of the location.
 * @property longitude Longitude coordinate of the location.
 * @property label User-defined label for the location.
 */
data class SavedPlacesLocation(
    val latitude: Double,
    val longitude: Double,
    val label: String = ""
)