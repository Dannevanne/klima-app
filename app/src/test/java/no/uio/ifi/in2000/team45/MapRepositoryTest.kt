package no.uio.ifi.in2000.team45

import no.uio.ifi.in2000.team45.data.map.MapDatasource
import no.uio.ifi.in2000.team45.data.map.MapRepository
import no.uio.ifi.in2000.team45.data.map.OverlayType
import org.junit.Test
import org.junit.Assert.*

class MapRepositoryTest {

    @Test
    fun testGetOverlayUrlDelegatesToDatasource() {
        val repository = MapRepository()
        val url = repository.getOverlayURL(
            type = OverlayType.FLOOD,
            bbox = listOf(10.0, 59.0, 11.0, 60.0),
            width = 800,
            height = 600
        )
        assertTrue(url.contains("Flomaktsomhet"))
        assertTrue(url.contains("800,600"))
    }

    @Test
    fun testGetOverlayUrlAllTypesReturnNonEmpty() {
        val repository = MapRepository()
        val bbox = listOf(10.0, 59.0, 11.0, 60.0)
        // NONE is excluded -- buildOverlayURL throws for it on purpose since
        // the map skips overlay rendering when no overlay is selected.
        for (type in OverlayType.entries.filter { it != OverlayType.NONE }) {
            val url = repository.getOverlayURL(type, bbox, 500, 400)
            assertTrue("URL for $type should not be empty", url.isNotEmpty())
        }
    }
}
