package no.uio.ifi.in2000.team45.ui.map.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener

/**
 * Handles map click events from the Mapbox map.

 * @param mapView Mapbox MapView to attach the click listener to.
 * @param onMapClick Called when the user taps on the map.
 */
@Composable
fun MapClickEffect(
    mapView: MapView?,
    onMapClick: () -> Unit
) {
    if (mapView == null) return
    DisposableEffect(mapView) {
        val listener = OnMapClickListener {
            onMapClick()
            false
        }
        mapView.mapboxMap.addOnMapClickListener(listener)
        onDispose {
            mapView.mapboxMap.removeOnMapClickListener(listener)
        }
    }
}
