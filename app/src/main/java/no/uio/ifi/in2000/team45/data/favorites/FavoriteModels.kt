package no.uio.ifi.in2000.team45.data.favorites

data class FavoriteStation(
    val stationId: String,
    val name: String,
    val municipality: String = "",
    val county: String = ""
)

data class FavoriteLocation(
    val latitude: Double,
    val longitude: Double,
    val label: String = ""
)