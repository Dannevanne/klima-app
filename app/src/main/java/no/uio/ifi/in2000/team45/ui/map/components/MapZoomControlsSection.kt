package no.uio.ifi.in2000.team45.ui.map.components

import androidx.compose.runtime.Composable
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import no.uio.ifi.in2000.team45.ui.map.utils.zoomMap
import com.mapbox.maps.MapView
import no.uio.ifi.in2000.team45.data.map.OverlayType

/**
 * Wrapper around [MapZoomControls] that connects zoom actions to the Mapbox map state.
 * And notifies user if zoom-out is blocked by overlay constraints.
 *
 * @param mapView Mapbox MapView instance used to perform zoom operations.
 * @param viewportState Current map viewport state used for camera updates.
 * @param activeOverlay Currently active overlay affecting zoom behavior.
 * @param onZoomOutBlocked Callback invoked when a zoom-out action is prevented.
 */
@Composable
fun MapZoomControlsSection(
    mapView: MapView?,
    viewportState: MapViewportState,
    activeOverlay: OverlayType,
    onZoomOutBlocked: () -> Unit = {},
) {
    MapZoomControls(
        onZoomIn = {
            zoomMap(mapView, viewportState, 1.0, activeOverlay)
        },
        onZoomOut = {
            val blocked = zoomMap(mapView, viewportState, -1.0, activeOverlay)
            if (blocked) onZoomOutBlocked()
        },
    )
}