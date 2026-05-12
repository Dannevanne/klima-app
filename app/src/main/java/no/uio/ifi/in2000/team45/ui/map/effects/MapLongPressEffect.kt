package no.uio.ifi.in2000.team45.ui.map.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapLongClickListener
import com.mapbox.maps.MapView
import com.mapbox.geojson.Point

/**
 * Handles long-press events on the Mapbox map.
 *
 * @param mapView Mapbox MapView to attach the long-press listener to.
 * @param onMapLongClick Called when the user long-presses the map with the
 * geographic point of the interaction.
 */
@Composable
fun MapLongPressEffect(
    mapView: MapView?,
    onMapLongClick: (Point) -> Unit
) {
    if (mapView == null) return
    DisposableEffect(mapView) {
        val longClickListener = OnMapLongClickListener { point ->
            onMapLongClick(point)
            true
        }

        mapView.mapboxMap.addOnMapLongClickListener(longClickListener)

        onDispose {
            mapView.mapboxMap.removeOnMapLongClickListener(longClickListener)
        }
    }
}