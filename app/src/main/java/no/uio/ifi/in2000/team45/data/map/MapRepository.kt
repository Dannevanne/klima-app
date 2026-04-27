package no.uio.ifi.in2000.team45.data.map

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

    suspend fun isInFloodZone(lat: Double, lon: Double): Boolean {
        return mapDatasource.isInFloodZone(lat, lon)
    }

    suspend fun isInRadonZone(lat: Double, lon: Double): Boolean {
        return mapDatasource.isInRadonZone(lat, lon)
    }

    suspend fun isInQuickClayZone(lat: Double, lon: Double): Boolean {
        return mapDatasource.isInQuickClayZone(lat, lon)
    }

    suspend fun isInLandslideZone(lat: Double, lon: Double): Boolean {
        return mapDatasource.isInLandslideZone(lat, lon)
    }

    suspend fun getWindSpeed(lat: Double, lon: Double): Double? {
        return mapDatasource.getWindSpeed(lat, lon)
    }
}