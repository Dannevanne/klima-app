package no.uio.ifi.in2000.team45.ui.map

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.rasterLayer
import com.mapbox.maps.extension.style.layers.getLayer
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

        // changed to so that the expanded box is rendered
        val bbox = overlayBBox ?: return@MapEffect
        val corners = cornersFromBBox(bbox)

        if (style.getLayer("overlaylayer") != null) {
            style.removeStyleLayer("overlaylayer")
        }

        if (style.getSource("overlaysource") != null) {
            style.removeStyleSource("overlaysource")
        }

        val source = ImageSource.Builder("overlaysource")
            .coordinates(corners)
            .build()

        style.addSource(source)
        style.getSourceAs<ImageSource>("overlaysource")?.updateImage(currentBitmap)


        if (style.getLayer("overlaylayer") == null) {
            style.addLayer(rasterLayer("overlaylayer", "overlaysource") {
                rasterOpacity(0.5)
            })
        }
    }
}