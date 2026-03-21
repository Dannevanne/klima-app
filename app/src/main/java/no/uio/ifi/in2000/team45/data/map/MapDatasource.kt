package no.uio.ifi.in2000.team45.data.map

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import no.uio.ifi.in2000.team45.network.KtorClient

class MapDatasource {
    fun buildFloodExportURL(bbox: List<Double>,
                            width: Int,
                            height: Int,
                            layersIDs: String = "17"): String{
        val minLon = bbox[0]
        val minLat = bbox[1]
        val maxLon = bbox[2]
        val maxLat = bbox[3]

        return buildString{
            append("https://kart.nve.no/enterprise/rest/services/Flomsoner2/MapServer/export")
            append("?bbox=$minLon,$minLat,$maxLon,$maxLat")
            append("&bboxSR=4326")
            append("&imageSR=4326")
            append("&size=$width,$height")
            append("&format=png32")
            append("&transparent=false")
            append("&layers=show:$layersIDs")
            append("&f=image")
        }
    }
}