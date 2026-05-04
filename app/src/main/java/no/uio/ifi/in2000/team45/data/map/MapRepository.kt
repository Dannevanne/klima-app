package no.uio.ifi.in2000.team45.data.map

import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val RADON_HAZARD_CLASSES = listOf(
    "Særlig høy aktsomhet",
    "Høy aktsomhet",
    "Moderat til lav aktsomhet",
    "Usikker aktsomhet"
)

private val QUICK_CLAY_FAREGRAD_RANK = mapOf(
    "Lav" to 1, "Middels" to 2, "Høy" to 3
)

private val lenientJson = Json { ignoreUnknownKeys = true }

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
        return identifyHasResults("Flomaktsomhet", "1", lat, lon)
    }

    suspend fun isInRadonZone(lat: Double, lon: Double): Boolean? {
        return getRadonHazard(lat, lon)?.let { true }
    }

    suspend fun isInQuickClayZone(lat: Double, lon: Double): Boolean? {
        return identifyHasResults("SkredKvikkleire2", "0,1", lat, lon)
    }

    // Highest `faregrad` ("Lav" / "Middels" / "Hoy") at the point, or null
    // if it isn't in any mapped polygon. Picks the highest if a point hits
    // multiple polygons.
    suspend fun getQuickClayHazard(lat: Double, lon: Double): String? {
        val body = mapDatasource.fetchQuickClayHazardBody(lat, lon) ?: return null
        return try {
            val root = lenientJson.parseToJsonElement(body).jsonObject
            val results = root["results"]?.jsonArray ?: return null
            var best: String? = null
            for (r in results) {
                val attrs = r.jsonObject["attributes"]?.jsonObject ?: continue
                val fg = attrs["faregrad"]?.jsonPrimitive?.contentOrNull
                    ?: attrs["Faregrad"]?.jsonPrimitive?.contentOrNull
                    ?: continue
                if (fg in QUICK_CLAY_FAREGRAD_RANK
                    && (best == null || QUICK_CLAY_FAREGRAD_RANK[fg]!! > QUICK_CLAY_FAREGRAD_RANK[best]!!)
                ) {
                    best = fg
                }
            }
            best
        } catch (e: Exception) {
            Log.e("MapRepository", "Quick clay hazard parse failed", e)
            null
        }
    }

    // Layer 1 is the vector polygon dataset (real aktsomhet zones); layer 0
    // is a coarse raster whose pixel can be ~100m, so identifying against it
    // false-positives in cities near slopes. We hit only the vector layer for
    // a cleaner in/out answer.
    suspend fun isInLandslideZone(lat: Double, lon: Double): Boolean? {
        return identifyHasResults("JordFlomskredAktsomhet", "1", lat, lon)
    }

    // Layer 1 is the S3 snowslide aktsomhet polygons (the main vector layer
    // visible at usable zoom levels). Layer 0 is a coarse raster meant for
    // overviews; we skip it for identify to avoid false positives.
    suspend fun isInAvalancheZone(lat: Double, lon: Double): Boolean? {
        return identifyHasResults("SnoskredAktsomhet", "1", lat, lon)
    }

    // True/false if the NVE identify response had any features at the point,
    // null when the call itself failed so callers can render "unknown" instead
    // of a misleading "Low".
    private suspend fun identifyHasResults(
        service: String, layers: String, lat: Double, lon: Double
    ): Boolean? {
        val body = mapDatasource.fetchNveIdentifyBody(service, layers, lat, lon) ?: return null
        return body.contains("\"results\"") &&
                !body.contains("\"results\":[]") &&
                !body.contains("\"results\": []")
    }

    // Pulls the radon "aktsomhet" class out of NGU's WMS GetFeatureInfo body.
    // Returns one of "Saerlig hoey aktsomhet", "Hoey aktsomhet",
    // "Moderat til lav aktsomhet", "Usikker aktsomhet", or null when the
    // point isn't in any mapped pixel.
    suspend fun getRadonHazard(lat: Double, lon: Double): String? {
        val body = mapDatasource.fetchRadonHazardBody(lat, lon) ?: return null
        return RADON_HAZARD_CLASSES.firstOrNull { body.contains(it, ignoreCase = true) }
    }

    // Average wind speed at 50m height (m/s), parsed out of the NVE identify
    // response. Returns null if no data at this point or the call failed.
    suspend fun getWindSpeed(lat: Double, lon: Double): Double? {
        val body = mapDatasource.fetchWindSpeedBody(lat, lon) ?: return null
        return try {
            val root = lenientJson.parseToJsonElement(body).jsonObject
            val results = root["results"]?.jsonArray ?: return null
            for (result in results) {
                val attrs = result.jsonObject["attributes"]?.jsonObject ?: continue
                val pixelVal = attrs["Classify.Pixel Value"]?.jsonPrimitive?.content
                pixelVal?.toDoubleOrNull()?.let { return it }
            }
            null
        } catch (e: Exception) {
            Log.e("MapRepository", "Wind speed parse failed", e)
            null
        }
    }
}
