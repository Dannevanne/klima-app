package no.uio.ifi.in2000.team45.ui.map

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import no.uio.ifi.in2000.team45.data.savedplaces.models.SavedPlacesLocation
import no.uio.ifi.in2000.team45.ui.map.effects.MapBoundsEffect
import no.uio.ifi.in2000.team45.ui.map.effects.MapCameraOverlayEffect
import no.uio.ifi.in2000.team45.ui.map.effects.MapClickEffect
import no.uio.ifi.in2000.team45.ui.map.effects.MapLongPressEffect
import no.uio.ifi.in2000.team45.ui.map.effects.MapOverlayEffect
import no.uio.ifi.in2000.team45.ui.map.effects.MapSavedPlacesEffect
import no.uio.ifi.in2000.team45.ui.map.effects.MapSelectedLocationEffect
import no.uio.ifi.in2000.team45.ui.map.effects.MapUserLocationEffect

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
    savedPlaces: List<SavedPlacesLocation>,
    onSavedPlaceClick: (SavedPlacesLocation) -> Unit,
    onMapLongClick: (Point) -> Unit,
    onMapClick: () -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    MapboxMap(
        modifier = modifier
            .onSizeChanged {
                onMapSizeChanged(it.width, it.height)
                           // The maps size is needed when the
                           // overlayURL is being built
                           // because the backend service returns
                           // a photo depending on the shown map window
            },
        mapViewportState = viewportState,
        scaleBar = {}
    ){
        MapEffect(Unit) { mapView ->
            onMapViewReady(mapView)
        }

        MapBoundsEffect()

        MapLongPressEffect(
            mapView = mapViewRef,
            onMapLongClick = onMapLongClick
        )

        MapClickEffect(
            mapView = mapViewRef,
            onMapClick = onMapClick
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
            mapViewModel = mapViewModel,
            context = context,
        )

        MapOverlayEffect(
            bitmap = pendingBitmap,
            overlayBBox = overlayBBox,
            useDarkMap = useDarkMap
        )

        MapSavedPlacesEffect(
            savedPlaces = savedPlaces,
            onPlaceClick = onSavedPlaceClick
        )
    }
}