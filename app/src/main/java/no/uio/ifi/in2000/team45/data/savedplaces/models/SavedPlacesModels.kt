package no.uio.ifi.in2000.team45.data.savedplaces.models

data class FavoriteStation(
    val stationId: String,
    val name: String,
    val municipality: String = "",
    val county: String = ""
)

data class SavedPlacesLocation(
    val latitude: Double,
    val longitude: Double,
    val label: String = ""
)