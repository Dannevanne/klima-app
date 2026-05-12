package no.uio.ifi.in2000.team45.ui.map.effects

import androidx.compose.runtime.Composable
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraBoundsOptions
import com.mapbox.maps.CoordinateBounds
import com.mapbox.maps.extension.compose.MapEffect

// coordinate bound of norway
private val NORWAY_AREA_BOUNDS = CoordinateBounds(
    Point.fromLngLat(-10.0, 53.0),
    Point.fromLngLat(45.0, 82.0)
)

// minimum and maximum zoom levels
private const val MIN_MAP_ZOOM = 3.25
private const val MAX_MAP_ZOOM = 18.0

/**
 * Restricts the map camera to predefined bounds and zoom limits.
 *
 * Applies Norway map bounds and enforces min/max zoom levels once
 * when the map is first composed.
 */
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