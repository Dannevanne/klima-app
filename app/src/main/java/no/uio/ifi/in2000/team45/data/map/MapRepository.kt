package no.uio.ifi.in2000.team45.data.map

class MapRepository(
    val mapDatasource: MapDatasource = MapDatasource()
) {
    suspend fun getOverlay(): String {
        return mapDatasource.getOverlay()
    }
}