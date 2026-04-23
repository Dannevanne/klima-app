package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.mapbox.maps.MapView
import com.mapbox.common.Cancelable
import com.mapbox.maps.CameraChangedCallback
import com.mapbox.maps.toCameraOptions
import no.uio.ifi.in2000.team45.ui.map.util.expandBBox

@Composable
fun MapCameraOverlayEffect(
    mapView: MapView?,
    mapWidth: Int,
    mapHeight: Int,
    mapViewModel: MapViewModel
) {
    DisposableEffect(mapView, mapWidth, mapHeight) {
        if (mapView == null) {
            onDispose { }
        } else {
            // Reads the camera setting from MapBox,
            // calculates the visable area,
            // and asks ViewModel for new overlay-data.
            fun updateFromCamera() {
                if (mapWidth <= 0 || mapHeight <= 0) return
                val bounds = mapView.mapboxMap.coordinateBoundsForCamera(
                    mapView.mapboxMap.cameraState.toCameraOptions()
                )
                val bbox = listOf(
                    bounds.southwest.longitude(),
                    bounds.southwest.latitude(),
                    bounds.northeast.longitude(),
                    bounds.northeast.latitude()
                )

                mapViewModel.updateOverlay(
                    bbox = bbox,
                    width = mapWidth,
                    height = mapHeight
                )

                // Saves where the camera is pointing on the map
                mapViewModel.saveCamera(
                    center = mapView.mapboxMap.cameraState.center,
                    zoom = mapView.mapboxMap.cameraState.zoom
                )
            }

            // Camera changes in the map dictates new overlay requests.
            // The UI observes the map, and the ViewModel decides
            // if a request should go ahead.
            val cameraChangedCallback = CameraChangedCallback {
                updateFromCamera()
            }

            val cancelable: Cancelable =
                mapView.mapboxMap.subscribeCameraChanged(cameraChangedCallback)

            updateFromCamera()

            onDispose {
                cancelable.cancel()
            }
        }
    }
}