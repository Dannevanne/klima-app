package no.uio.ifi.in2000.team45.data.map

import android.util.Log
import androidx.annotation.StringRes
import io.ktor.client.call.body
import io.ktor.client.request.get
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
    // Returns the raw JSON body, or null when the call failed; the repository
    // turns that into the in/out Boolean.
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

    // Raw identify JSON for the SkredKvikkleire2 layers at the given point.
    // The repository pulls the highest `faregrad` attribute out of it.
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

    // Raw identify JSON for the NVE wind resource raster at the given point.
    // The repository pulls the pixel value (m/s at 50 m height) out of it.
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

    // Raw GML body from NGU's WMS GetFeatureInfo at the given point. The
    // repository turns the body into an aktsomhet class string. text/plain
    // only returns a feature id with no attributes, which silently mapped
    // every in-zone point to Unknown.
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