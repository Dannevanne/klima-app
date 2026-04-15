package no.uio.ifi.in2000.team45.ui.map.util

// Function that should just expand the current bbox on the map
// factor is the expanding number, 1.5 might be good
fun expandBBox(bbox: List<Double>, factor: Double = 1.5): List<Double> {
    val minLon = bbox[0]
    val minLat = bbox[1]
    val maxLon = bbox[2]
    val maxLat = bbox[3]

    // expanding the bbox
    val lonExpand = (maxLon - minLon) * factor
    val latExpand = (maxLat - minLat) * factor

    return listOf(
        minLon - lonExpand,
        minLat - latExpand,
        maxLon + lonExpand,
        maxLat + latExpand
    )
}

// MapBox ImageSource needs the corners for the picture in the correct order,
// not just the bbox values. This function does this conversion for us.
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