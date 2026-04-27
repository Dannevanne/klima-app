package no.uio.ifi.in2000.team45.ui.map

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.mapbox.maps.MapView
import com.mapbox.maps.CameraChangedCallback
import com.mapbox.maps.MapIdleCallback
import com.mapbox.maps.toCameraOptions

@Composable
fun MapCameraOverlayEffect(
    mapView: MapView?,
    mapWidth: Int,
    mapHeight: Int,
    mapViewModel: MapViewModel,
    context: Context
) {
    fun triggerOverlayUpdate() {
        val mv = mapView ?: return
        if (mapWidth <= 0 || mapHeight <= 0) return
        val bounds = mv.mapboxMap.coordinateBoundsForCamera(
            mv.mapboxMap.cameraState.toCameraOptions()
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
            height = mapHeight,
            context = context,
        )
    }

    // Force a fetch when the user picks a different overlay. Otherwise the
    // viewmodel sits in Loading until the next MapIdle event -- which never
    // fires unless the user actually pans, so the overlay appears stuck.
    LaunchedEffect(mapView, mapWidth, mapHeight, mapViewModel.activeOverlay) {
        triggerOverlayUpdate()
    }

    DisposableEffect(mapView, mapWidth, mapHeight) {
        if (mapView == null) {
            onDispose { }
        } else {
            // Camera changes in the map dictates new overlay requests.
            // The UI observes the map, and the ViewModel decides
            // if a request should go ahead.
            val cameraChangedCallback = CameraChangedCallback {
                mapViewModel.saveCamera(
                    center = mapView.mapboxMap.cameraState.center,
                    zoom = mapView.mapboxMap.cameraState.zoom
                )
            }

            val cameraIdleCallback = MapIdleCallback {
                triggerOverlayUpdate()
            }

            val changeCancelable = mapView.mapboxMap.subscribeCameraChanged(cameraChangedCallback)
            val idleCancelable = mapView.mapboxMap.subscribeMapIdle(cameraIdleCallback)

            onDispose {
                changeCancelable.cancel()
                idleCancelable.cancel()
            }
        }
    }
}