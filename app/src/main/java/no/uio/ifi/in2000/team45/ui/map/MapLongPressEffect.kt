package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapLongClickListener
import com.mapbox.maps.MapView
import com.mapbox.geojson.Point

@Composable
fun MapLongPressEffect(
    mapView: MapView?,
    onMapLongClick: (Point) -> Unit
) {
    DisposableEffect(mapView) {
        if (mapView == null) {
            onDispose {  }
        }
        else {
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
}