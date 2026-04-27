package no.uio.ifi.in2000.team45.ui.map.util

import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import no.uio.ifi.in2000.team45.data.map.OverlayType

// Returns true if the user tried to zoom out past the overlay's minZoom.
// Callers use it to surface a "switch overlay to zoom out further" hint.
fun zoomMap(
    mapView: MapView?,
    viewPortState: MapViewportState,
    zoomChange: Double,
    activeOverlay: OverlayType
): Boolean {
    val currentZoom = mapView?.mapboxMap?.cameraState?.zoom ?: 3.75
    val requested = currentZoom + zoomChange
    val clamped = requested.coerceIn(activeOverlay.minZoom, activeOverlay.maxZoom)

    val camera = CameraOptions.Builder()
        .zoom(clamped)
        .build()

    viewPortState.easeTo(camera)

    // NONE has no overlay to swap to, so the "change layer" hint would be
    // misleading. Just clamp silently in that case.
    return zoomChange < 0 &&
            requested < activeOverlay.minZoom &&
            activeOverlay != OverlayType.NONE
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