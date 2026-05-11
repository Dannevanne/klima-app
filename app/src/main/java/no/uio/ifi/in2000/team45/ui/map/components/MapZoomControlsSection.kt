package no.uio.ifi.in2000.team45.ui.map.components

import androidx.compose.runtime.Composable
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import no.uio.ifi.in2000.team45.ui.map.utils.zoomMap
import com.mapbox.maps.MapView
import no.uio.ifi.in2000.team45.data.map.OverlayType

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