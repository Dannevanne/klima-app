package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import no.uio.ifi.in2000.team45.data.savedplaces.SavedPlacesLocation

// Renders one filled circle per saved place and reports clicks via a callback.
// Recreates the annotations whenever the list changes; cheap because the list
// is small (a handful of saved places at most).
@Composable
fun MapSavedPlacesEffect(
    mapView: MapView?,
    savedPlaces: List<SavedPlacesLocation>,
    onPlaceClick: (SavedPlacesLocation) -> Unit
) {
    DisposableEffect(mapView, savedPlaces) {
        val mv = mapView
        if (mv == null) {
            onDispose { }
        } else {
            val annotationApi = mv.annotations
            val manager = annotationApi.createCircleAnnotationManager()

            val byAnnotationId = HashMap<String, SavedPlacesLocation>()
            savedPlaces.forEach { place ->
                val options = CircleAnnotationOptions()
                    .withPoint(Point.fromLngLat(place.longitude, place.latitude))
                    .withCircleRadius(7.0)
                    .withCircleColor("#1976D2")
                    .withCircleStrokeColor("#FFFFFF")
                    .withCircleStrokeWidth(2.0)
                val annotation = manager.create(options)
                byAnnotationId[annotation.id] = place
            }

            manager.addClickListener { annotation ->
                byAnnotationId[annotation.id]?.let(onPlaceClick)
                true
            }

            onDispose {
                annotationApi.removeAnnotationManager(manager)
            }
        }
    }
}
