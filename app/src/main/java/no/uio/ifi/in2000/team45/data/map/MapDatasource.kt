package no.uio.ifi.in2000.team45.data.map

import android.util.Log
import androidx.annotation.StringRes
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.network.KtorClient

// Every overlay-type with its own max- and minZoom
enum class OverlayType(@StringRes val labelRes: Int, val minZoom: Double, val maxZoom: Double) {
    FLOOD(R.string.flood, 11.5, 20.0),
    QUICK_CLAY(R.string.quick_clay, 11.4, 20.0),
    RADON(R.string.radon, 10.9, 20.0),
    WIND(R.string.wind, 5.0, 20.0),
    LANDSLIDE(R.string.landslide, 5.0, 20.0)
}

class MapDatasource {

    // NVE ArcGIS identify for a point against a given service
    private suspend fun isInNveZone(
        service: String, lat: Double, lon: Double
    ): Boolean {
        val tolerance = 0.001
        val url = buildString {
            append("https://kart.nve.no/enterprise/rest/services/$service/MapServer/identify")
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
            body.contains("\"results\"") && !body.contains("\"results\":[]")
                    && !body.contains("\"results\": []")
        } catch (e: Exception) {
            Log.e("MapDatasource", "$service zone check failed", e)
            false
        }
    }

    suspend fun isInFloodZone(lat: Double, lon: Double): Boolean {
        return isInNveZone("Flomaktsomhet", lat, lon)
    }

    suspend fun isInQuickClayZone(lat: Double, lon: Double): Boolean {
        return isInNveZone("SkredKvikkleire2", lat, lon)
    }

    // Returns average wind speed at 50m height (m/s), or null if no data
    suspend fun getWindSpeed(lat: Double, lon: Double): Double? {
        val tolerance = 0.001
        val url = buildString {
            append("https://kart.nve.no/enterprise/rest/services/Vindressurser/MapServer/identify")
            append("?geometry=$lon,$lat")
            append("&geometryType=esriGeometryPoint")
            append("&sr=4326")
            append("&layers=all:3")
            append("&tolerance=5")
            append("&mapExtent=${lon - tolerance},${lat - tolerance},${lon + tolerance},${lat + tolerance}")
            append("&imageDisplay=100,100,96")
            append("&returnGeometry=false")
            append("&f=json")
        }
        return try {
            val response = KtorClient.client.get(url)
            val body: String = response.body()
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val root = json.parseToJsonElement(body).jsonObject
            val results = root["results"]?.jsonArray ?: return null
            for (result in results) {
                val attrs = result.jsonObject["attributes"]?.jsonObject ?: continue
                val pixelVal = attrs["Classify.Pixel Value"]?.jsonPrimitive?.content
                pixelVal?.toDoubleOrNull()?.let { return it }
            }
            null
        } catch (e: Exception) {
            Log.e("MapDatasource", "Wind speed check failed", e)
            null
        }
    }

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
            append("&INFO_FORMAT=text/plain")
        }
        return try {
            val response = KtorClient.client.get(url)
            val body: String = response.body()
            body.contains("Feature")
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
                service = "Flomaktsomhet",
                layers = "1",
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
            OverlayType.LANDSLIDE -> buildNveExportURL(
                service = "JordFlomskredAktsomhet",
                layers = "0,1", // getting both layers
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