package no.uio.ifi.in2000.team45

import no.uio.ifi.in2000.team45.data.map.MapDatasource
import no.uio.ifi.in2000.team45.data.map.OverlayType
import org.junit.Test
import org.junit.Assert.*

class MapDatasourceTest {

    private val datasource = MapDatasource()

    @Test
    fun testBuildOverlayUrlFloodContainsFlomaktsomhet() {
        val url = datasource.buildOverlayURL(
            type = OverlayType.FLOOD,
            bbox = listOf(10.0, 59.0, 11.0, 60.0),
            width = 800,
            height = 600
        )
        assertTrue(url.contains("Flomaktsomhet"))
        assertTrue(url.contains("format=png32"))
    }

    @Test
    fun testBuildOverlayUrlRadonUsesNguWms() {
        val url = datasource.buildOverlayURL(
            type = OverlayType.RADON,
            bbox = listOf(10.0, 59.0, 11.0, 60.0),
            width = 800,
            height = 600
        )
        assertTrue(url.contains("geo.ngu.no"))
        assertTrue(url.contains("Radon_aktsomhet"))
    }

    @Test
    fun testBuildOverlayUrlContainsBboxValues() {
        val bbox = listOf(10.5, 59.5, 11.5, 60.5)
        val url = datasource.buildOverlayURL(
            type = OverlayType.QUICK_CLAY,
            bbox = bbox,
            width = 400,
            height = 300
        )
        assertTrue(url.contains("10.5"))
        assertTrue(url.contains("59.5"))
        assertTrue(url.contains("400"))
    }

    @Test
    fun testBuildOverlayUrlLandslideUsesCorrectService() {
        val url = datasource.buildOverlayURL(
            type = OverlayType.LANDSLIDE,
            bbox = listOf(10.0, 59.0, 11.0, 60.0),
            width = 800,
            height = 600
        )
        assertTrue(url.contains("JordFlomskredAktsomhet"))
    }

    @Test
    fun testBuildOverlayUrlWindUsesVindressurser() {
        val url = datasource.buildOverlayURL(
            type = OverlayType.WIND,
            bbox = listOf(10.0, 59.0, 11.0, 60.0),
            width = 800,
            height = 600
        )
        assertTrue(url.contains("Vindressurser"))
    }

    @Test
    fun testBuildOverlayUrlAvalancheUsesSnoskredAktsomhet() {
        val url = datasource.buildOverlayURL(
            type = OverlayType.AVALANCHE,
            bbox = listOf(10.0, 59.0, 11.0, 60.0),
            width = 800,
            height = 600
        )
        assertTrue(url.contains("SnoskredAktsomhet"))
    }
}
