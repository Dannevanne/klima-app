package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import no.uio.ifi.in2000.team45.ui.map.util.zoomMap
import com.mapbox.maps.MapView
import no.uio.ifi.in2000.team45.data.map.OverlayType

@Composable
fun MapZoomControlsSection(
    mapView: MapView?,
    viewportState: MapViewportState,
    mapViewModel: MapViewModel,
    activeOverlay: OverlayType,
    modifier: Modifier = Modifier
) {
    MapZoomControls(
        onZoomIn = {
            zoomMap(mapView, viewportState, 1.0, activeOverlay)
        },
        onZoomOut = {
            zoomMap(mapView, viewportState, -1.0, activeOverlay)
        },
        modifier = modifier
    )
}