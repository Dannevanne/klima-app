package no.uio.ifi.in2000.team45.data.map

import android.util.Log
import androidx.annotation.StringRes
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.network.KtorClient

// Every overlay-type with its own max- and minZoom
enum class OverlayType(@StringRes val labelRes: Int, val minZoom: Double, val maxZoom: Double) {
    NONE(R.string.no_overlay, 3.25, 20.0),
    FLOOD(R.string.flood, 11.5, 20.0),
    QUICK_CLAY(R.string.quick_clay, 12.5, 20.0),
    RADON(R.string.radon, 10.9, 20.0),
    WIND(R.string.wind, 5.0, 20.0),
    LANDSLIDE(R.string.landslide, 5.0, 20.0)
}

class MapDatasource {

    // NVE ArcGIS identify for a point against specific layers of a service.
    // The layer IDs must match what the corresponding overlay renders, or the
    // check returns true for background/mask layers that the user never sees.
    private suspend fun isInNveZone(
        service: String, layers: String, lat: Double, lon: Double
    ): Boolean {
        val tolerance = 0.001
        val url = buildString {
            append("https://kart.nve.no/enterprise/rest/services/$service/MapServer/identify")
            append("?geometry=$lon,$lat")
            append("&geometryType=esriGeometryPoint")
            append("&sr=4326")
            append("&layers=visible:$layers")
            append("&tolerance=1")
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
        return isInNveZone("Flomaktsomhet", "1", lat, lon)
    }

    suspend fun isInQuickClayZone(lat: Double, lon: Double): Boolean {
        return isInNveZone("SkredKvikkleire2", "0,1", lat, lon)
    }

    // Reads the actual `faregrad` attribute from the SkredKvikkleire2 layer.
    // Returns "Lav" / "Middels" / "Hoy" (mapping from "Hoey" without the
    // diacritic for stable comparison) or null if the point isn't in any
    // mapped polygon. Picks the highest faregrad if a point hits multiple.
    suspend fun getQuickClayHazard(lat: Double, lon: Double): String? {
        val tolerance = 0.001
        val url = buildString {
            append("https://kart.nve.no/enterprise/rest/services/SkredKvikkleire2/MapServer/identify")
            append("?geometry=$lon,$lat")
            append("&geometryType=esriGeometryPoint")
            append("&sr=4326")
            append("&layers=visible:0,1")
            append("&tolerance=1")
            append("&mapExtent=${lon - tolerance},${lat - tolerance},${lon + tolerance},${lat + tolerance}")
            append("&imageDisplay=100,100,96")
            append("&returnGeometry=false")
            append("&f=json")
        }
        return try {
            val response = KtorClient.client.get(url)
            if (response.status.value != 200) return null
            val body: String = response.body()
            val parsed = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            val root = parsed.parseToJsonElement(body).jsonObject
            val results = root["results"]?.jsonArray ?: return null
            val rank = mapOf("Lav" to 1, "Middels" to 2, "Høy" to 3)
            var best: String? = null
            for (r in results) {
                val attrs = r.jsonObject["attributes"]?.jsonObject ?: continue
                val fg = attrs["faregrad"]?.jsonPrimitive?.contentOrNull
                    ?: attrs["Faregrad"]?.jsonPrimitive?.contentOrNull
                    ?: continue
                if (fg in rank && (best == null || rank[fg]!! > rank[best]!!)) {
                    best = fg
                }
            }
            best
        } catch (e: Exception) {
            Log.e("MapDatasource", "Quick clay hazard lookup failed", e)
            null
        }
    }

    // Generic "skred" check. Layer 1 is the vector polygon dataset (real
    // aktsomhet zones); layer 0 is a coarse raster whose pixel can be ~100m,
    // so identify against it false-positives in cities near slopes. We hit
    // only the vector layer for a cleaner in/out answer.
    suspend fun isInLandslideZone(lat: Double, lon: Double): Boolean {
        return isInNveZone("JordFlomskredAktsomhet", "1", lat, lon)
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
        return getRadonHazard(lat, lon) != null
    }

    // Pulls the radon "aktsomhet" class from NGU's WMS GetFeatureInfo. Returns
    // one of: "Saerlig hoey aktsomhet", "Hoey aktsomhet",
    // "Moderat til lav aktsomhet", "Usikker aktsomhet", or null when the
    // point isn't in any mapped pixel.
    suspend fun getRadonHazard(lat: Double, lon: Double): String? {
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
            // The text/plain response contains lines like "Aktsomhet: Hoey"
            // or "value: <class>"; just match any of the documented classes.
            val candidates = listOf(
                "Særlig høy aktsomhet",
                "Høy aktsomhet",
                "Moderat til lav aktsomhet",
                "Usikker aktsomhet"
            )
            candidates.firstOrNull { body.contains(it, ignoreCase = true) }
        } catch (e: Exception) {
            Log.e("MapDatasource", "Radon zone check failed", e)
            null
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
            OverlayType.NONE -> error("buildOverlayURL should not be called for NONE")
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