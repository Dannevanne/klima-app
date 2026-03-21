package no.uio.ifi.in2000.team45.data.map

class MapRepository(private val mapDatasource: MapDatasource = MapDatasource()){
    fun getOverlayURL(bbox: List<Double>,
                      width: Int,
                      height: Int): String{
        return mapDatasource.buildFloodExportURL(bbox = bbox,
            width = width,
            height = height)
    }
}