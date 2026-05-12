package no.uio.ifi.in2000.team45.data.map

import android.util.Log
import androidx.annotation.StringRes
import io.ktor.client.call.body
import io.ktor.client.request.get
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.network.KtorClient

/**
 * Represents the available map overlay types and their supported zoom ranges.
 *
 * @property labelRes String resource ID used as the overlay label.
 * @property minZoom Minimum zoom level required for the overlay to be visible.
 * @property maxZoom Maximum zoom level supported for the overlay.
 */
enum class OverlayType(@field:StringRes val labelRes: Int, val minZoom: Double, val maxZoom: Double) {
    NONE(R.string.no_overlay, 3.25, 20.0),
    FLOOD(R.string.flood, 11.5, 20.0),
    QUICK_CLAY(R.string.quick_clay, 12.5, 20.0),
    RADON(R.string.radon, 10.9, 20.0),
    WIND(R.string.wind, 5.0, 20.0),
    LANDSLIDE(R.string.landslide, 10.0, 20.0),
    AVALANCHE(R.string.avalanche, 10.0, 20.0)
}

/**
 * Datasource responsible for fetching hazard and overlay map data
 * from external GIS services.
 */
class MapDatasource {

    /**
     * Fetches raw identify data from an NVE ArcGIS service for a specific map point.
     *
     * @param service The ArcGIS service name.
     * @param layers The visible layer IDs to query.
     * @param lat The latitude of the selected point.
     * @param lon The longitude of the selected point.
     * @return The raw JSON response body, or null if the request fails.
     */
    suspend fun fetchNveIdentifyBody(
        service: String, layers: String, lat: Double, lon: Double
    ): String? {
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
            response.body<String>()
        } catch (e: Exception) {
            Log.e("MapDatasource", "$service identify failed", e)
            null
        }
    }

    /**
     * Fetches raw quick clay hazard data for a specific map point.
     *
     * @param lat The latitude of the selected point.
     * @param lon The longitude of the selected point.
     * @return The raw JSON response body, or null if the request fails.
     */
    suspend fun fetchQuickClayHazardBody(lat: Double, lon: Double): String? {
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
            response.body<String>()
        } catch (e: Exception) {
            Log.e("MapDatasource", "Quick clay hazard fetch failed", e)
            null
        }
    }

    /**
     * Fetches raw wind speed data for a specific map point.
     *
     * @param lat The latitude of the selected point.
     * @param lon The longitude of the selected point.
     * @return The raw JSON response body, or null if the request fails.
     */
    suspend fun fetchWindSpeedBody(lat: Double, lon: Double): String? {
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
            response.body<String>()
        } catch (e: Exception) {
            Log.e("MapDatasource", "Wind speed fetch failed", e)
            null
        }
    }

    /**
     * Fetches raw radon hazard data for a specific map point.
     *
     * @param lat The latitude of the selected point.
     * @param lon The longitude of the selected point.
     * @return The raw GML response body, or null if the request fails.
     */
    suspend fun fetchRadonHazardBody(lat: Double, lon: Double): String? {
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
            append("&INFO_FORMAT=application/vnd.ogc.gml")
        }
        return try {
            val response = KtorClient.client.get(url)
            response.body<String>()
        } catch (e: Exception) {
            Log.e("MapDatasource", "Radon hazard fetch failed", e)
            null
        }
    }

    /**
     * Builds the overlay image URL for the selected overlay type.
     *
     * @param type The overlay type to generate a URL for.
     * @param bbox The bounding box coordinates.
     * @param width The image width in pixels.
     * @param height The image height in pixels.
     * @return The generated overlay URL.
     */
    fun buildOverlayURL(
        type: OverlayType,
        bbox: List<Double>,
        width: Int,
        height: Int
    ): String {
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
            OverlayType.AVALANCHE -> buildNveExportURL(
                service = "SnoskredAktsomhet",
                // 0 is the raster shown at low zoom; 1,2,3 are the vector
                // polygon variants (S3, S2 without/with forest effect).
                layers = "0,1,2,3",
                bbox = bbox, width = width, height = height
            )
        }
    }

    /**
     * Builds an NVE ArcGIS export URL for map overlays.
     *
     * @param service The ArcGIS service name.
     * @param layers The layer IDs to include.
     * @param bbox The bounding box coordinates.
     * @param width The image width in pixels.
     * @param height The image height in pixels.
     * @return The generated export URL.
     */
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

    /**
     * Builds a WMS URL for the radon overlay service.
     *
     * @param bbox The bounding box coordinates.
     * @param width The image width in pixels.
     * @param height The image height in pixels.
     * @return The generated WMS URL.
     */
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
