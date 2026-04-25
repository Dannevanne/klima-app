package no.uio.ifi.in2000.team45.ui.info

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.Water
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import no.uio.ifi.in2000.team45.R

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
        containerColor = Color(0xFFE3F2FD),
        iconColor = Color(0xFF1565C0),
        icon = Icons.Outlined.Water
    ),
    InfoDatasource(
        title = "Frost API v0",
        url = "https://frost.met.no/index.html",
        descriptionId = R.string.climate_statistics,
        containerColor = Color(0xFFE8F5E9),
        iconColor = Color(0xFF388E3C),
        icon = Icons.Outlined.DeviceThermostat
    ),
    InfoDatasource(
        title = "Mapbox",
        url = "https://www.mapbox.com/",
        descriptionId = R.string.map_and_search,
        containerColor = Color(0xFFEDE7F6),
        iconColor = Color(0xFF6A1B9A),
        icon = Icons.Outlined.TravelExplore
    ),
    InfoDatasource(
        title = "NGU / RadonWMS",
        url = "https://www.ngu.no/",
        descriptionId = R.string.radon_gas,
        containerColor = Color(0xFFFFF3E0),
        iconColor = Color(0xFFE65100),
        icon = Icons.Outlined.Cloud
    )
)