package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.MapView
import com.mapbox.geojson.Point

@Composable
fun MapUserLocationEffect(
    mapView: MapView?,
    locationGranted: Boolean,
    onUserLocationChanged: (Point) -> Unit
) {
    if (mapView == null) return

    // Activates the blue location-dot.
    // Rerun every time locationGranted changes.
    DisposableEffect(mapView, locationGranted) {
        mapView.location.updateSettings {
            locationPuck = createDefault2DPuck(withBearing = false)
            enabled = locationGranted
            pulsingEnabled = true
        }

        // Listens to location changes from MapBox
        // and updates userLocation in ViewModel
        val locationListener = OnIndicatorPositionChangedListener { point ->
           onUserLocationChanged(point)
        }

        if (locationGranted) {
            mapView.location.addOnIndicatorPositionChangedListener(locationListener)
        }

        onDispose {
            mapView.location.removeOnIndicatorPositionChangedListener(locationListener)
        }
    }
}