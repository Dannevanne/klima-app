package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.runtime.Composable
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraBounds
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.extension.compose.MapEffect

private val NORWAY_AREA_BOUNDS = CoordinateBounds(
    Point.fromLngLat(-5.0, 53.0),
    Point.fromLngLat(40.0, 75.0)
)

private const val MIN_MAP_ZOOM = 3.25
private const val MAX_MAP_ZOOM = 18.0

@Composable
fun MapBoundsEffect() {
    MapEffect(Unit) { mapView ->
        mapView.mapboxMap.setBounds(
            CameraBoundsOptions.Builder()
                .bounds(NORWAY_AREA_BOUNDS)
                .minZoom(MIN_MAP_ZOOM)
                .maxZoom(MAX_MAP_ZOOM)
                .build()
        )
    }
}