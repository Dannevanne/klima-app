package no.uio.ifi.in2000.team45.ui.info

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Storm
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Water
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.theme.IconBlue
import no.uio.ifi.in2000.team45.ui.theme.IconGreen
import no.uio.ifi.in2000.team45.ui.theme.IconLightBlue
import no.uio.ifi.in2000.team45.ui.theme.IconLightGreen
import no.uio.ifi.in2000.team45.ui.theme.IconLightOrange
import no.uio.ifi.in2000.team45.ui.theme.IconLightPurple
import no.uio.ifi.in2000.team45.ui.theme.IconOrange
import no.uio.ifi.in2000.team45.ui.theme.IconPurple

data class InfoDatasource(
    val title: String,
    val url: String,
    val descriptionId: Int,
    val containerColor: Color,
    val iconColor: Color,
    val icon: ImageVector
)

val infoDatasources = listOf(
    InfoDatasource(
        title = "NVE",
        url = "https://www.nve.no/",
        descriptionId = R.string.flood_landslides_quick_clay_wind,
        containerColor = IconLightBlue,
        iconColor = IconBlue,
        icon = Icons.Outlined.Water
    ),
    InfoDatasource(
        title = "Frost API v0",
        url = "https://frost.met.no/index.html",
        descriptionId = R.string.climate_statistics,
        containerColor = IconLightGreen,
        iconColor = IconGreen,
        icon = Icons.Outlined.DeviceThermostat
    ),
    InfoDatasource(
        title = "Mapbox",
        url = "https://www.mapbox.com/",
        descriptionId = R.string.map_and_search,
        containerColor = IconLightPurple,
        iconColor = IconPurple,
        icon = Icons.Outlined.TravelExplore
    ),
    InfoDatasource(
        title = "NGU / RadonWMS",
        url = "https://www.ngu.no/",
        descriptionId = R.string.radon_gas,
        containerColor = IconLightOrange,
        iconColor = IconOrange,
        icon = Icons.Outlined.Cloud
    ),
    InfoDatasource(
        title = "DiBK",
        url = "https://www.dibk.no/saksbehandling-tilsyn-og-kontroll/temaveiledning-utbygging-i-fareomrader/",
        descriptionId = R.string.building_in_hazard_areas,
        containerColor = IconLightBlue,
        iconColor = IconBlue,
        icon = Icons.Outlined.Engineering
    ),
    InfoDatasource(
        title = "Varsom",
        url = "https://www.varsom.no/snoskred/",
        descriptionId = R.string.avalanche_warnings,
        containerColor = IconLightOrange,
        iconColor = IconOrange,
        icon = Icons.Outlined.Warning
    ),
    InfoDatasource(
        title = "DSA",
        url = "https://www.dsa.no/radon/",
        descriptionId = R.string.radon_and_radiation_safety,
        containerColor = IconLightGreen,
        iconColor = IconGreen,
        icon = Icons.Outlined.HealthAndSafety
    ),
    InfoDatasource(
        title = "DSB",
        url = "https://www.dsb.no/sikkerhverdag/produkter-utstyr-og-fritid/slik-forbereder-du-deg-pa-storm/",
        descriptionId = R.string.storm_preparedness,
        containerColor = IconLightPurple,
        iconColor = IconPurple,
        icon = Icons.Outlined.Storm
    )
)