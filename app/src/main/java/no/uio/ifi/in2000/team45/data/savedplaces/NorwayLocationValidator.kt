package no.uio.ifi.in2000.team45.data.savedplaces

object NorwayLocationValidator {
    fun isInsideNorway(location: SavedPlacesLocation): Boolean {
        return isInsideNorway(location.latitude, location.longitude)
    }

    fun isInsideNorway(latitude: Double, longitude: Double): Boolean {
        val norwayPolygon = listOf(
            57.8 to 4.5,
            58.0 to 7.2,
            58.6 to 8.3,
            59.0 to 10.8,
            59.7 to 11.8,
            61.0 to 12.6,
            62.2 to 12.3,
            63.5 to 13.8,
            64.2 to 14.2,
            65.2 to 14.0,
            66.2 to 15.4,
            67.2 to 16.1,
            68.0 to 17.9,
            68.7 to 18.8,
            69.5 to 20.8,
            69.9 to 23.5,
            70.4 to 28.5,
            70.1 to 31.2,
            69.3 to 30.8,
            68.5 to 29.2,
            67.6 to 29.0,
            66.5 to 25.0,
            65.7 to 23.0,
            64.7 to 21.0,
            63.8 to 18.5,
            62.8 to 15.0,
            61.5 to 12.5,
            60.3 to 11.2,
            59.2 to 10.9,
            58.2 to 8.2,
            57.8 to 4.5
        )

        var inside = false
        var j = norwayPolygon.lastIndex

        for (i in norwayPolygon.indices) {
            val latI = norwayPolygon[i].first
            val lonI = norwayPolygon[i].second
            val latJ = norwayPolygon[j].first
            val lonJ = norwayPolygon[j].second

            val intersects = ((lonI > longitude) != (lonJ > longitude)) &&
            (latitude < (latJ - latI) * (longitude - lonI) / (lonJ - lonI) + latI)

            if (intersects) inside = !inside
            j = i
        }
        return inside

    }
}