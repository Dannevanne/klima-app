package no.uio.ifi.in2000.team45.ui.map.utils

import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import no.uio.ifi.in2000.team45.data.map.OverlayType

/**
 * Zooms the map within overlay-defined limits.
 *
 * Returns true if the user attempted to zoom out past the minimum zoom
 * for the active overlay, allowing the UI to show a hint or fallback action.
 *
 * @param mapView Mapbox MapView used to read current zoom level.
 * @param viewPortState Map viewport controller used to animate zoom changes.
 * @param zoomChange Zoom change to apply.
 * @param activeOverlay Currently active overlay defining zoom bounds.
 * @return True if zoom-out was blocked by overlay min zoom.
 */
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

/**
 * Moves the map camera to a specific geographic point.
 * Centers the camera on the given point with a fixed zoom level.
 *
 * @param point Target geographic coordinate.
 * @param viewPortState Map viewport controller used for camera animation.
 */
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