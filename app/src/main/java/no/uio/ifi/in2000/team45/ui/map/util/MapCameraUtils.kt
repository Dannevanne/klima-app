package no.uio.ifi.in2000.team45.ui.map.util

import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState

// Function for zoom change. Limits duplication of code, and is used twice in the
// parameter for MapZoomControls to decide what the zoom in and zoom out buttons do.
private fun zoomMap(mapView: MapView?,
                    viewPortState: MapViewportState,
                    zoomChange: Double) {
    val currentZoom = mapView?.mapboxMap?.cameraState?.zoom ?: 11.5

    val camera = CameraOptions.Builder().zoom(
        (currentZoom + zoomChange).coerceIn(1.0, 20.0)
    )
        .build()

    viewPortState.easeTo(camera)
}

// Builds new camera configuration for the map
private fun moveToPoint(
    point: Point,
    viewPortState: MapViewportState
){
    val camera = CameraOptions.Builder()
        .center(point)
        .zoom(13.5)
        .build()

    viewPortState.easeTo(camera)
}