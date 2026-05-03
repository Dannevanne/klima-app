package no.uio.ifi.in2000.team45.data.map

import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val RADON_HAZARD_CLASSES = listOf(
    "Særlig høy aktsomhet",
    "Høy aktsomhet",
    "Moderat til lav aktsomhet",
    "Usikker aktsomhet"
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