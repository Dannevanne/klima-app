package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.ViewAnnotationOptions
import com.mapbox.maps.extension.compose.annotation.ViewAnnotation
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.viewannotation.geometry
import no.uio.ifi.in2000.team45.data.savedplaces.SavedPlacesLocation
import no.uio.ifi.in2000.team45.R

// Renders one filled circle per saved place and reports clicks via a callback.
// Recreates the annotations whenever the list changes; cheap because the list
// is small (a handful of saved places at most).
@Composable
fun MapSavedPlacesEffect(
    mapView: MapView?,
    savedPlaces: List<SavedPlacesLocation>,
    onPlaceClick: (SavedPlacesLocation) -> Unit
) {
    savedPlaces.forEach { place ->
        ViewAnnotation(
            options = ViewAnnotationOptions.Builder()
                .geometry(Point.fromLngLat(place.longitude, place.latitude))
                .allowOverlap(true)
                .build()
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = stringResource(R.string.save_place),
                tint = Color.White,
                modifier = Modifier
                    .clickable { onPlaceClick(place) },

            )
        }
    }
}
