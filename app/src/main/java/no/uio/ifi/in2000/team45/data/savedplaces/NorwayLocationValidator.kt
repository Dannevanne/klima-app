package no.uio.ifi.in2000.team45.data.savedplaces

import com.mapbox.geojson.FeatureCollection
import android.content.Context
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.turf.TurfJoins
import no.uio.ifi.in2000.team45.R

object NorwayLocationValidator {

    private var norwayBoundary: FeatureCollection? = null

    fun loadBoundary(context: Context) {
        if (norwayBoundary != null) return

        val geoJson = context.resources
            .openRawResource(R.raw.norway_boundary)
            .bufferedReader()
            .use { it.readText() }

        norwayBoundary = FeatureCollection.fromJson(geoJson)
    }

    fun isInsideNorway(location: SavedPlacesLocation): Boolean {
        return isInsideNorway(location.latitude, location.longitude)
    }

    fun isInsideNorway(latitude: Double, longitude: Double): Boolean {
        val boundary = norwayBoundary ?: return false
        val point = Point.fromLngLat(longitude, latitude)

        return boundary.features()?.any { feature ->
            val geometry = feature.geometry()
            geometry is Polygon && TurfJoins.inside(point, geometry)
        } ?: false
    }
}