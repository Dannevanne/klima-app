package no.uio.ifi.in2000.team45.ui.info

data class InfoDatasource(
    val title: String,
    val url: String
)

val infoDatasources = listOf(
    InfoDatasource(
        title = "NVE",
        url = "https://www.nve.no/"
    ),
    InfoDatasource(
        title = "Frost API v0",
        url = "https://frost.met.no/index.html"
    ),
    InfoDatasource(
        title = "Mapbox",
        url = "https://www.mapbox.com/"
    ),
    InfoDatasource(
        title = "NGU / RadonWMS",
        url = "https://www.ngu.no/"
    )
)