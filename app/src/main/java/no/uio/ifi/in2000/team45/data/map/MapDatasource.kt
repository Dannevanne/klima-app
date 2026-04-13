package no.uio.ifi.in2000.team45.data.map

import android.util.Log
import io.ktor.client.call.body
import io.ktor.client.request.get
import no.uio.ifi.in2000.team45.network.KtorClient

enum class OverlayType(val label: String) {
    FLOOD("Flom"),
    QUICK_CLAY("Kvikkleire"),
    RADON("Radon"),
    WIND("Vind")
}

class MapDatasource {

    // Check if coordinates fall inside an NVE flood zone
    suspend fun isInFloodZone(lat: Double, lon: Double): Boolean {
        val tolerance = 0.001
        val url = buildString {
            append("https://kart.nve.no/enterprise/rest/services/Flomsoner2/MapServer/identify")
            append("?geometry=$lon,$lat")
            append("&geometryType=esriGeometryPoint")
            append("&sr=4326")
            append("&layers=all")
            append("&tolerance=5")
            append("&mapExtent=${lon - tolerance},${lat - tolerance},${lon + tolerance},${lat + tolerance}")
            append("&imageDisplay=100,100,96")
            append("&returnGeometry=false")
            append("&f=json")
        }
        return try {
            val response = KtorClient.client.get(url)
            val body: String = response.body()
            // If "results" array is non-empty, point is in a flood zone
            body.contains("\"results\"") && !body.contains("\"results\":[]")
                    && !body.contains("\"results\": []")
        } catch (e: Exception) {
            Log.e("MapDatasource", "Flood zone check failed", e)
            false
        }
    }

    // Check if coordinates fall inside NGU radon aktsomhet zone
    suspend fun isInRadonZone(lat: Double, lon: Double): Boolean {
        val tolerance = 0.001
        val url = buildString {
            append("https://geo.ngu.no/mapserver/RadonWMS2")
            append("?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetFeatureInfo")
            append("&LAYERS=Radon_aktsomhet")
            append("&QUERY_LAYERS=Radon_aktsomhet")
            append("&SRS=EPSG:4326")
            append("&BBOX=${lon - tolerance},${lat - tolerance},${lon + tolerance},${lat + tolerance}")
            append("&WIDTH=101&HEIGHT=101")
            append("&X=50&Y=50")
            append("&INFO_FORMAT=application/json")
        }
        return try {
            val response = KtorClient.client.get(url)
            val body: String = response.body()
            body.contains("\"features\"") && !body.contains("\"features\":[]")
                    && !body.contains("\"features\": []")
        } catch (e: Exception) {
            Log.e("MapDatasource", "Radon zone check failed", e)
            false
        }
    }

    fun buildOverlayURL(
        type: OverlayType,
        bbox: List<Double>,
        width: Int,
        height: Int
    ): String {
        val minLon = bbox[0]
        val minLat = bbox[1]
        val maxLon = bbox[2]
        val maxLat = bbox[3]

        return when (type) {
            OverlayType.FLOOD -> buildNveExportURL(
                service = "Flomsoner2",
                layers = "17",
                bbox = bbox, width = width, height = height
            )
            OverlayType.QUICK_CLAY -> buildNveExportURL(
                service = "SkredKvikkleire2",
                layers = "0,1",
                bbox = bbox, width = width, height = height
            )
            OverlayType.WIND -> buildNveExportURL(
                service = "Vindressurser",
                layers = "3",
                bbox = bbox, width = width, height = height
            )
            OverlayType.RADON -> buildRadonWmsURL(
                bbox = bbox, width = width, height = height
            )
        }
    }

    // NVE ArcGIS REST export (flood, quick clay, wind)
    private fun buildNveExportURL(
        service: String,
        layers: String,
        bbox: List<Double>,
        width: Int,
        height: Int
    ): String {
        return buildString {
            append("https://kart.nve.no/enterprise/rest/services/$service/MapServer/export")
            append("?bbox=${bbox[0]},${bbox[1]},${bbox[2]},${bbox[3]}")
            append("&bboxSR=4326")
            append("&imageSR=3857")
            append("&size=$width,$height")
            append("&format=png32")
            append("&transparent=true")
            append("&layers=show:$layers")
            append("&f=image")
        }
    }

    // NGU WMS (radon)
    private fun buildRadonWmsURL(
        bbox: List<Double>,
        width: Int,
        height: Int
    ): String {
        return buildString {
            append("https://geo.ngu.no/mapserver/RadonWMS2")
            append("?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap")
            append("&LAYERS=Radon_aktsomhet")
            append("&STYLES=default")
            append("&SRS=EPSG:4326")
            append("&BBOX=${bbox[0]},${bbox[1]},${bbox[2]},${bbox[3]}")
            append("&WIDTH=$width&HEIGHT=$height")
            append("&FORMAT=image/png")
            append("&TRANSPARENT=TRUE")
        }
    }
}