package no.uio.ifi.in2000.team45.ui.map.utils

/**
 * Converts a bbox into Mapbox image source corner coordinates.
 * Reorders values into the format required by ImageSource.
 */
fun cornersFromBBox(bbox: List<Double>): List<List<Double>>{
    val minLon = bbox[0]
    val minLat = bbox[1]
    val maxLon = bbox[2]
    val maxLat = bbox[3]

    return listOf(
        listOf(minLon, maxLat),
        listOf(maxLon, maxLat),
        listOf(maxLon, minLat),
        listOf(minLon, minLat)
    )
}