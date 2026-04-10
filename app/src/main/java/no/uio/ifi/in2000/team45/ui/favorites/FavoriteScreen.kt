package no.uio.ifi.in2000.team45.ui.favorites

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.data.favorites.FavoriteLocation
import no.uio.ifi.in2000.team45.data.favorites.FavoriteStation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    favoriteViewModel: FavoriteViewModel,
    onSettingsClick: () -> Unit,
    onMapClick: () -> Unit,
    onStatisticClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stations by favoriteViewModel.stations.collectAsState()
    val locations by favoriteViewModel.locations.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                title = {
                    Text(
                        text = "Favoritter",
                        modifier = Modifier.padding(8.dp),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Innstillinger")
                    }
                    IconButton(onClick = onMapClick) {
                        Icon(Icons.Outlined.Place, contentDescription = "Kart")
                    }
                    IconButton(onClick = onStatisticClick) {
                        Icon(Icons.Outlined.Info, contentDescription = "Statistikk")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.Favorite, contentDescription = "Favoritter")
                    }
                }
            }
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
                if (status?.inFloodZone == true) {
                    FloodZoneTips(location.label.ifBlank { "Kartposisjon" })
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
                tint = Color(0xFFFFC107),
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
                Icon(Icons.Filled.Delete, contentDescription = "Fjern", tint = Color.Gray)
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
        Column(modifier = Modifier.padding(12.dp)) {
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
                    Icon(Icons.Filled.Delete, contentDescription = "Fjern", tint = Color.Gray)
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
            Text(label, fontSize = 10.sp, color = Color.DarkGray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AdviceCard(text: String, backgroundColor: Color) {
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
            fontSize = 14.sp
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
                color = Color(0xFFE65100)
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
