package no.uio.ifi.in2000.team45.ui.map.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.mapbox.maps.CameraOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState

/**
 * Moves the map camera to a selected geographic location and animates the camera.
 *
 * @param selectedLocation Target map position to fly to.
 * @param viewportState Map viewport controller used for camera movement.
 * @param onFlyToComplete Called after the camera animation is started.
 */
@Composable
fun MapSelectedLocationEffect(
    selectedLocation: Point?,
    viewportState: MapViewportState,
    onFlyToComplete: () -> Unit
) {
    // Listens to selectedLocation from ViewModel and
    // reruns when moveToUserLocation() is called.
    // The UI changes the camera when selectedLocation is set in ViewModel.
    // The ViewModel decides what the selected posistion is,
    // and the UI decides how to move there.
    LaunchedEffect(selectedLocation) {
        selectedLocation?.let {
            viewportState.easeTo(
                CameraOptions.Builder()
                    .center(it)
                    .zoom(13.0)
                    .build()
            )
            onFlyToComplete()
        }
    }
}