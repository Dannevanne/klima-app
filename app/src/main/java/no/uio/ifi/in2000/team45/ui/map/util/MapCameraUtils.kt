package no.uio.ifi.in2000.team45.ui.map.util

import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import no.uio.ifi.in2000.team45.data.map.OverlayType

// Function for zoom change. Limits duplication of code, and is used twice in the
// parameter for MapZoomControls to decide what the zoom in and zoom out buttons do.
fun zoomMap(
    mapView: MapView?,
    viewPortState: MapViewportState,
    zoomChange: Double,
    activeOverlay: OverlayType
    ) {
    val currentZoom = mapView?.mapboxMap?.cameraState?.zoom ?: 3.75

    val camera = CameraOptions.Builder()
        .zoom((currentZoom + zoomChange).coerceIn(activeOverlay.minZoom, activeOverlay.maxZoom))
        .build()

    viewPortState.easeTo(camera)
}

// Builds new camera configuration for the map
fun moveToPoint(
    point: Point,
    viewPortState: MapViewportState
){
    val camera = CameraOptions.Builder()
        .center(point)
        .zoom(13.5)
        .build()

    viewPortState.easeTo(camera)
}