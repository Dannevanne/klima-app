package no.uio.ifi.in2000.team45.data.savedplaces.utils

import android.content.Context
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.turf.TurfJoins
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.savedplaces.models.SavedPlacesLocation

/**
 * Utility object for validating whether a location is inside Norway's boundaries.
 */
object NorwayLocationValidator {

    private var norwayBoundary: FeatureCollection? = null

    /**
     * Loads the Norway boundary GeoJSON data into memory.
     *
     * @param context Application context used to access the raw resource file.
     */
    fun loadBoundary(context: Context) {
        if (norwayBoundary != null) return

        val geoJson = context.resources
            .openRawResource(R.raw.norway_boundary)
            .bufferedReader()
            .use { it.readText() }

        norwayBoundary = FeatureCollection.fromJson(geoJson)
    }

    /**
     * Checks whether the specified saved location is inside Norway.
     *
     * @param location The saved location to validate.
     * @return True if the location is inside Norway, false otherwise.
     */
    fun isInsideNorway(location: SavedPlacesLocation): Boolean {
        return isInsideNorway(location.latitude, location.longitude)
    }

    /**
     * Checks whether the specified coordinates are inside Norway.
     *
     * @param latitude The latitude coordinate to validate.
     * @param longitude The longitude coordinate to validate.
     * @return True if the coordinates are inside Norway, false otherwise.
     */
    fun isInsideNorway(latitude: Double, longitude: Double): Boolean {
        val boundary = norwayBoundary ?: return false
        val point = Point.fromLngLat(longitude, latitude)

        return boundary.features()?.any { feature ->
            val geometry = feature.geometry()
            geometry is Polygon && TurfJoins.inside(point, geometry)
        } ?: false
    }
}