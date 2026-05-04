package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener

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
