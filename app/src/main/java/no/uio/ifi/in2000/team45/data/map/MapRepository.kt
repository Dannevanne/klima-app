package no.uio.ifi.in2000.team45.data.map

private val RADON_HAZARD_CLASSES = listOf(
    "Særlig høy aktsomhet",
    "Høy aktsomhet",
    "Moderat til lav aktsomhet",
    "Usikker aktsomhet"
)

class MapRepository(private val mapDatasource: MapDatasource = MapDatasource()) {
    fun getOverlayURL(
        type: OverlayType,
        bbox: List<Double>,
        width: Int,
        height: Int
    ): String {
        return mapDatasource.buildOverlayURL(
            type = type, bbox = bbox, width = width, height = height
        )
    }

    suspend fun isInFloodZone(lat: Double, lon: Double): Boolean? {
        return mapDatasource.isInFloodZone(lat, lon)
    }

    suspend fun isInRadonZone(lat: Double, lon: Double): Boolean? {
        return getRadonHazard(lat, lon)?.let { true }
    }

    suspend fun isInQuickClayZone(lat: Double, lon: Double): Boolean? {
        return mapDatasource.isInQuickClayZone(lat, lon)
    }

    suspend fun getQuickClayHazard(lat: Double, lon: Double): String? {
        return mapDatasource.getQuickClayHazard(lat, lon)
    }

    suspend fun isInLandslideZone(lat: Double, lon: Double): Boolean? {
        return mapDatasource.isInLandslideZone(lat, lon)
    }

    // Pulls the radon "aktsomhet" class out of NGU's WMS GetFeatureInfo body.
    // Returns one of "Saerlig hoey aktsomhet", "Hoey aktsomhet",
    // "Moderat til lav aktsomhet", "Usikker aktsomhet", or null when the
    // point isn't in any mapped pixel.
    suspend fun getRadonHazard(lat: Double, lon: Double): String? {
        val body = mapDatasource.fetchRadonHazardBody(lat, lon) ?: return null
        return RADON_HAZARD_CLASSES.firstOrNull { body.contains(it, ignoreCase = true) }
    }

    suspend fun getWindSpeed(lat: Double, lon: Double): Double? {
        return mapDatasource.getWindSpeed(lat, lon)
    }
}