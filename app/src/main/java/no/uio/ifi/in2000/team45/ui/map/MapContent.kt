package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import android.graphics.Bitmap
import com.mapbox.maps.MapView
import com.mapbox.geojson.Point

@Composable
fun MapContent(
    viewportState: MapViewportState,
    mapViewRef: MapView?,
    onMapViewReady: (MapView) -> Unit,
    onMapSizeChanged: (width: Int, height: Int) -> Unit,
    pendingBitmap: Bitmap?,
    overlayBBox: List<Double>?,
    useDarkMap: Boolean,
    currentMapWidth: Int,
    currentMapHeight: Int,
    locationGranted: Boolean,
    mapViewModel: MapViewModel,
    onMapLongClick: (Point) -> Unit,
    modifier: Modifier = Modifier
) {
    MapboxMap(
        modifier = modifier
            .onSizeChanged {
                onMapSizeChanged(it.width, it.height)
                           // The maps size is needed when the
                           // when the overlayURL is being built
                           // because the backend service returns
                           // a photo depending on the shown map window
            },
        mapViewportState = viewportState,
        // Overrides the default MapboxMap scalebar slot so nothing renders there.
        scaleBar = {}
    ){
        MapEffect(Unit) { mapView ->
            onMapViewReady(mapView)
        }

        MapLongPressEffect(
            mapView = mapViewRef,
            onMapLongClick = onMapLongClick
        )

        MapUserLocationEffect(
            mapView = mapViewRef,
            locationGranted = locationGranted,
            onUserLocationChanged = { point ->
                mapViewModel.updateUserLocation(point)
            }
        )

        MapSelectedLocationEffect(
            selectedLocation = mapViewModel.selectedLocation,
            viewportState = viewportState,
            onFlyToComplete = { mapViewModel.clearSelectedLocation() }
        )

        MapThemeEffect(useDarkMap)

        MapCameraOverlayEffect(
            mapView = mapViewRef,
            mapWidth = currentMapWidth,
            mapHeight = currentMapHeight,
            mapViewModel = mapViewModel
        )

        MapOverlayEffect(
            bitmap = pendingBitmap,
            overlayBBox = overlayBBox,
            useDarkMap = useDarkMap
        )
    }
}