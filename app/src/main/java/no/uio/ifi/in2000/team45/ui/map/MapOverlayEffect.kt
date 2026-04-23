package no.uio.ifi.in2000.team45.ui.map

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.rasterLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.RasterResampling
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.ImageSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.sources.updateImage
import no.uio.ifi.in2000.team45.ui.map.util.cornersFromBBox

// When the bitmap and style is ready,
// a temporary overlay is built over the map.
@Composable
fun MapOverlayEffect(
    bitmap: Bitmap?,
    overlayBBox: List<Double>?,
    useDarkMap: Boolean
) {
    MapEffect(bitmap, overlayBBox, useDarkMap) { mapView ->
        val currentBitmap = bitmap ?: return@MapEffect
        val style = mapView.mapboxMap.getStyle() ?: return@MapEffect
        val bbox = overlayBBox ?: return@MapEffect
        val corners = cornersFromBBox(bbox)

        val sourceId = "overlaysource"
        val layerId = "overlaylayer"

        val source = style.getSourceAs<ImageSource>(sourceId)

        if (source == null) {
            val newSource = ImageSource.Builder(sourceId)
                .coordinates(corners)
                .build()
            style.addSource(newSource)
            style.getSourceAs<ImageSource>(sourceId)?.updateImage(currentBitmap)

            if (style.getLayer(layerId) == null) {
                style.addLayer(rasterLayer(layerId, sourceId) {
                    rasterOpacity(0.6)
                    rasterFadeDuration(0.0)
                    rasterResampling(RasterResampling.LINEAR)
                })
            }
        } else {
            source.coordinates(corners)
            source.updateImage(currentBitmap)
        }
    }
}