package no.uio.ifi.in2000.team45.data.map

import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// list of all the different radon hazard danger levels
private val RADON_HAZARD_CLASSES = listOf(
    "Særlig høy aktsomhet",
    "Høy aktsomhet",
    "Moderat til lav aktsomhet",
    "Usikker aktsomhet"
)

// map that connects different quick clay danger level to numbers
private val QUICK_CLAY_FAREGRAD_RANK = mapOf(
    "Lav" to 1, "Middels" to 2, "Høy" to 3
)

private val lenientJson = Json { ignoreUnknownKeys = true }

/**
 * Repository responsible for retrieving and processing hazard and overlay data
 * from the map datasource.
 *
 * @property mapDatasource Datasource used for map and hazard data requests.
 */
class MapRepository(private val mapDatasource: MapDatasource = MapDatasource()) {

    /**
     * Returns the overlay image URL for the selected overlay type.
     *
     * @param type The selected overlay type.
     * @param bbox The bounding box coordinates.
     * @param width The image width in pixels.
     * @param height The image height in pixels.
     * @return The generated overlay URL.
     */
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

    /**
     * Checks whether the specified location is inside a flood zone.
     *
     * @param lat The latitude of the selected point.
     * @param lon The longitude of the selected point.
     * @return True if the location is inside a flood zone, false if not,
     * or null if the request fails.
     */
    suspend fun isInFloodZone(lat: Double, lon: Double): Boolean? {
        return identifyHasResults("Flomaktsomhet", "1", lat, lon)
    }

    /**
     * Retrieves the highest quick clay hazard level for the specified location.
     *
     * @param lat The latitude of the selected point.
     * @param lon The longitude of the selected point.
     * @return The highest hazard level, or null if no hazard data exists.
     */
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

    /**
     * Checks whether the specified location is inside a landslide hazard zone.
     *
     * @param lat The latitude of the selected point.
     * @param lon The longitude of the selected point.
     * @return True if the location is inside a landslide zone, false if not,
     * or null if the request fails.
     */
    suspend fun isInLandslideZone(lat: Double, lon: Double): Boolean? {
        return identifyHasResults("JordFlomskredAktsomhet", "1", lat, lon)
    }

    /**
     * Checks whether the specified location is inside an avalanche hazard zone.
     *
     * @param lat The latitude of the selected point.
     * @param lon The longitude of the selected point.
     * @return True if the location is inside an avalanche zone, false if not,
     * or null if the request fails.
     */
    suspend fun isInAvalancheZone(lat: Double, lon: Double): Boolean? {
        return identifyHasResults("SnoskredAktsomhet", "1", lat, lon)
    }

    /**
     * Checks whether an NVE identify request returned any matching results.
     *
     * @param service The ArcGIS service name.
     * @param layers The visible layer IDs to query.
     * @param lat The latitude of the selected point.
     * @param lon The longitude of the selected point.
     * @return True if results were found, false if not, or null if the request fails.
     */
    private suspend fun identifyHasResults(
        service: String, layers: String, lat: Double, lon: Double
    ): Boolean? {
        val body = mapDatasource.fetchNveIdentifyBody(service, layers, lat, lon) ?: return null
        return body.contains("\"results\"") &&
                !body.contains("\"results\":[]") &&
                !body.contains("\"results\": []")
    }

    /**
     * Retrieves the radon hazard level for the specified location.
     *
     * @param lat The latitude of the selected point.
     * @param lon The longitude of the selected point.
     * @return The radon hazard classification, or null if no data exists.
     */
    suspend fun getRadonHazard(lat: Double, lon: Double): String? {
        val body = mapDatasource.fetchRadonHazardBody(lat, lon) ?: return null
        return RADON_HAZARD_CLASSES.firstOrNull { body.contains(it, ignoreCase = true) }
    }

    /**
     * Retrieves the average wind speed for the specified location.
     *
     * @param lat The latitude of the selected point.
     * @param lon The longitude of the selected point.
     * @return The average wind speed in m/s, or null if no data exists.
     */
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
