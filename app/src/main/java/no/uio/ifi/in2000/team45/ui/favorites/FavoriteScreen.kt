package no.uio.ifi.in2000.team45.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.NavBarItem
import no.uio.ifi.in2000.team45.data.favorites.FavoriteLocation
import no.uio.ifi.in2000.team45.data.favorites.FavoriteStation
import no.uio.ifi.in2000.team45.ui.common.AppBottomBar
import no.uio.ifi.in2000.team45.ui.common.AppTopBar
import no.uio.ifi.in2000.team45.ui.common.BottomBarItemData
import no.uio.ifi.in2000.team45.ui.common.OverflowMenu
import no.uio.ifi.in2000.team45.ui.common.OverflowMenuButton
import no.uio.ifi.in2000.team45.ui.map.MapScreen
import no.uio.ifi.in2000.team45.ui.map.MapTheme
import no.uio.ifi.in2000.team45.ui.map.useDarkThemeFrom
import no.uio.ifi.in2000.team45.ui.theme.StarGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    favoriteViewModel: FavoriteViewModel,
    onSettingsClick: () -> Unit,
    onInfoClick: () -> Unit,
    onHelpClick: () -> Unit,
    onMapClick: () -> Unit,
    onStatisticClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stations by favoriteViewModel.stations.collectAsState()
    val locations by favoriteViewModel.locations.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.saved_places),
                navigationIcon = {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 8.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    OverflowMenuButton(onClick = { expanded = true } )

                    OverflowMenu(
                        expanded =  expanded,
                        onDismissRequest = { expanded = false },
                        onSettingsClick = onSettingsClick,
                        onInfoClick = onInfoClick,
                        onHelpClick = onHelpClick
                    )
                }
            )
        },
        bottomBar = {
            AppBottomBar(
                items = listOf(
                    BottomBarItemData(
                        label = stringResource(R.string.nav_map),
                        onClick = onMapClick,
                        icon = { Icon(Icons.Outlined.Place, contentDescription = null) }
                    ),
                    BottomBarItemData(
                        label = stringResource(R.string.nav_statistics),
                        onClick = onStatisticClick,
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.outline_analytics),
                                contentDescription = null
                            )
                        }
                    ),
                    BottomBarItemData(
                        label = stringResource(R.string.saved_places),
                        onClick = {},
                        icon = { Icon(Icons.Filled.Favorite, contentDescription = null) }
                    )
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Saved map locations section
            item {
                Text(
                    text = "Lagrede kartposisjoner",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (locations.isEmpty()) {
                item {
                    Text(
                        text = "Ingen lagrede posisjoner enda. Bruk lagre-knappen i kartet.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }

            items(locations) { location ->
                LaunchedEffect(location) {
                    favoriteViewModel.fetchStatusForLocation(location)
                }
                val key = "${location.latitude},${location.longitude}"
                val status = favoriteViewModel.locationStatuses[key]
                LocationCard(
                    location = location,
                    status = status,
                    onRemove = { favoriteViewModel.removeLocation(location) }
                )
                val name = location.label.ifBlank { "Kartposisjon" }
                if (status?.inFloodZone == true) {
                    FloodZoneTips(name)
                }
                if (status?.inRadonZone == true) {
                    RadonZoneTips(name)
                }
                if (status?.inQuickClayZone == true) {
                    QuickClayZoneTips(name)
                }
                status?.avgWindSpeed?.let { speed ->
                    WindZoneTips(name, speed)
                }
            }

            // Advice section (mocked)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Rad og tips",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item { AdviceCard("Sjekk flomsonekart for eiendommen din.", Color(0xFFE3F2FD)) }
            item { AdviceCard("Hold deg oppdatert pa NVE sine varsler.", Color(0xFFFFF3E0)) }
            item { AdviceCard("Sjekk radonnivaer for din adresse hos NGU.", Color(0xFFE8F5E9)) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StationCard(station: FavoriteStation, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = StarGold,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(station.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                if (station.municipality.isNotBlank()) {
                    Text(
                        "${station.municipality}, ${station.county}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "ID: ${station.stationId}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "Fjern", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// Color based on temperature
private fun tempColor(temp: Double): Color = when {
    temp <= -10 -> Color(0xFFBBDEFB)
    temp <= 0 -> Color(0xFFE3F2FD)
    temp <= 10 -> Color(0xFFC8E6C9)
    temp <= 20 -> Color(0xFFFFF9C4)
    else -> Color(0xFFFFCCBC)
}

// Color based on wind speed
private fun windColor(speed: Double): Color = when {
    speed <= 3 -> Color(0xFFE8F5E9)
    speed <= 8 -> Color(0xFFFFF9C4)
    speed <= 15 -> Color(0xFFFFCCBC)
    else -> Color(0xFFEF9A9A)
}

@Composable
private fun LocationCard(
    location: FavoriteLocation,
    status: LocationStatus?,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(12.dp)) {
            // Header: name + coordinates + delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = location.label.ifBlank { "Kartposisjon" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "%.4f, %.4f".format(location.latitude, location.longitude),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, contentDescription = "Fjern", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Current conditions from nearest station
            if (status != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Stasjon: ${status.stationName}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Temperature chip
                    status.temperature?.let { temp ->
                        StatusChip(
                            label = "Temperatur",
                            value = "${String.format("%.1f", temp)} C",
                            color = tempColor(temp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Wind chip
                    status.windSpeed?.let { wind ->
                        StatusChip(
                            label = "Vind",
                            value = "${String.format("%.1f", wind)} m/s",
                            color = windColor(wind),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Precipitation chip
                    status.precipitation?.let { precip ->
                        StatusChip(
                            label = "Nedbor",
                            value = "${String.format("%.1f", precip)} mm/t",
                            color = Color(0xFFE3F2FD),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Henter vaerdata...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AdviceCard(text: String,
                       backgroundColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun FloodZoneTips(locationName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0B2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Flomsone-varsel for $locationName",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Denne posisjonen ligger i en flomsone. Her er noen tips:",
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("- Sjekk NVE sine flomvarsler jevnlig", fontSize = 13.sp)
            Text("- Ha en beredskapsplan for flom", fontSize = 13.sp)
            Text("- Flytt verdisaker bort fra kjeller/underetasje", fontSize = 13.sp)
            Text("- Sjekk forsikringsdekningen din", fontSize = 13.sp)
        }
    }
}

@Composable
private fun RadonZoneTips(locationName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Radon-aktsomhet for $locationName",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF283593)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Denne posisjonen ligger i en radon-aktsomhetssone. Her er noen tips:",
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("- Maal radonnivaaet i boligen din", fontSize = 13.sp)
            Text("- Tiltaksgrensen er 200 Bq/m3 (Statens straalevern)", fontSize = 13.sp)
            Text("- Installer radonbroenner eller ventilasjon ved hoye verdier", fontSize = 13.sp)
            Text("- Sjekk NGU sin radonkartlegging for mer info", fontSize = 13.sp)
        }
    }
}

@Composable
private fun QuickClayZoneTips(locationName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Kvikkleireomraade for $locationName",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFFB71C1C)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Denne posisjonen ligger i et kvikkleireomraade. Her er noen tips:",
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("- Unngaa graving og terrenginngrep uten geoteknisk vurdering", fontSize = 13.sp)
            Text("- Kontakt kommunen foer du gjor endringer paa tomten", fontSize = 13.sp)
            Text("- Foelg med paa NVE sine varslinger for jordskred", fontSize = 13.sp)
            Text("- Vurder grunnundersokelse ved bygging", fontSize = 13.sp)
        }
    }
}

@Composable
private fun WindZoneTips(locationName: String, avgSpeed: Double) {
    val level = when {
        avgSpeed < 4.0 -> "lav"
        avgSpeed < 6.0 -> "moderat"
        avgSpeed < 8.0 -> "hoey"
        else -> "svart hoey"
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Vindforhold for $locationName",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF006064)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Gjennomsnittlig vindstyrke: %.1f m/s (%s)".format(avgSpeed, level),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (avgSpeed >= 6.0) {
                Text("- Sikre lause gjenstander utendoors ved sterk vind", fontSize = 13.sp)
                Text("- Sjekk at tak og kledning taaler vindbelastningen", fontSize = 13.sp)
                Text("- Planlegg levegg eller beplantning som vindskjerm", fontSize = 13.sp)
            }
            Text("- Foelg med paa yr.no og varsom.no for vindvarsler", fontSize = 13.sp)
        }
    }
}
