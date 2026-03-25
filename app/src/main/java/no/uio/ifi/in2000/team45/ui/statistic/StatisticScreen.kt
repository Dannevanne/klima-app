package no.uio.ifi.in2000.team45.ui.statistic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.data.statistic.MonthlyClimateStats
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation

private val MONTH_NAMES = listOf(
    "Jan", "Feb", "Mar", "Apr", "Mai", "Jun",
    "Jul", "Aug", "Sep", "Okt", "Nov", "Des"
)

@Composable
fun StatisticScreen(
    statisticViewModel: StatisticViewModel,
    onSettingsClick: () -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = "to settings")
                    }
                    IconButton(onClick = onMapClick) {
                        Icon(Icons.Outlined.Place, contentDescription = "to map")
                    }
                    IconButton({}) {
                        Icon(Icons.Filled.Info, contentDescription = "")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Klimastatistikk",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SearchBar(
                query = statisticViewModel.searchQuery,
                onQueryChange = { statisticViewModel.updateSearchQuery(it) },
                onSearch = { statisticViewModel.searchStations() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (val state = statisticViewModel.uiState) {
                is StatisticUiState.Idle -> {
                    IdleContent()
                }
                is StatisticUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is StatisticUiState.StationResults -> {
                    StationList(
                        stations = state.stations,
                        onStationClick = { statisticViewModel.selectStation(it) }
                    )
                }
                is StatisticUiState.ClimateData -> {
                    ClimateDataView(
                        station = state.station,
                        stats = state.stats,
                        onBack = { statisticViewModel.goBack() }
                    )
                }
                is StatisticUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { statisticViewModel.goBack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text("Sok etter sted eller stasjon") },
        trailingIcon = {
            IconButton(onClick = onSearch) {
                Icon(Icons.Filled.Search, contentDescription = "Sok")
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun IdleContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Sok etter et sted for a se klimastatistikk",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "F.eks. \"Oslo\", \"Bergen\" eller \"Tromsoe\"",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StationList(
    stations: List<WeatherStation>,
    onStationClick: (WeatherStation) -> Unit
) {
    Text(
        text = "Velg en stasjon:",
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    LazyColumn {
        items(stations.take(20)) { station ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onStationClick(station) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = station.name,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                    if (station.municipality.isNotBlank()) {
                        Text(
                            text = "${station.municipality}, ${station.county}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "ID: ${station.id}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ClimateDataView(
    station: WeatherStation,
    stats: List<MonthlyClimateStats>,
    onBack: () -> Unit
) {
    LazyColumn {
        item {
            TextButton(onClick = onBack) {
                Text("Tilbake til sok")
            }
        }

        item {
            Text(
                text = station.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            if (station.municipality.isNotBlank()) {
                Text(
                    text = "${station.municipality}, ${station.county}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        if (stats.any { it.meanTemp != null }) {
            item {
                ClimateTable(
                    title = "Temperatur",
                    stats = stats,
                    rows = buildTemperatureRows(stats)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (stats.any { it.precipitation != null }) {
            item {
                ClimateTable(
                    title = "Nedbor",
                    stats = stats,
                    rows = buildPrecipitationRows(stats)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (stats.any { it.windSpeed != null }) {
            item {
                ClimateTable(
                    title = "Vind",
                    stats = stats,
                    rows = buildWindRows(stats)
                )
            }
        }
    }
}

data class TableRow(
    val label: String,
    val unit: String,
    val values: List<String>
)

@Composable
private fun ClimateTable(
    title: String,
    stats: List<MonthlyClimateStats>,
    rows: List<TableRow>
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Column {
                    // Header row with month names
                    Row {
                        Box(
                            modifier = Modifier.width(80.dp).padding(4.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        MONTH_NAMES.forEach { month ->
                            Box(
                                modifier = Modifier.width(48.dp).padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = month,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // Data rows
                    rows.forEach { row ->
                        Row {
                            Box(
                                modifier = Modifier.width(80.dp).padding(4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "${row.label} (${row.unit})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            row.values.forEach { value ->
                                Box(
                                    modifier = Modifier.width(48.dp).padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = value,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onRetry) {
                Text("Prov igjen")
            }
        }
    }
}

private fun formatValue(value: Double?): String {
    if (value == null) return "-"
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format("%.1f", value)
    }
}

private fun buildTemperatureRows(stats: List<MonthlyClimateStats>): List<TableRow> {
    val rows = mutableListOf<TableRow>()

    if (stats.any { it.meanTemp != null }) {
        rows.add(
            TableRow(
                label = "Snitt",
                unit = "C",
                values = stats.map { formatValue(it.meanTemp) }
            )
        )
    }
    if (stats.any { it.maxTemp != null }) {
        rows.add(
            TableRow(
                label = "Maks",
                unit = "C",
                values = stats.map { formatValue(it.maxTemp) }
            )
        )
    }
    if (stats.any { it.minTemp != null }) {
        rows.add(
            TableRow(
                label = "Min",
                unit = "C",
                values = stats.map { formatValue(it.minTemp) }
            )
        )
    }
    return rows
}

private fun buildPrecipitationRows(stats: List<MonthlyClimateStats>): List<TableRow> {
    return listOf(
        TableRow(
            label = "Nedbor",
            unit = "mm",
            values = stats.map { formatValue(it.precipitation) }
        )
    )
}

private fun buildWindRows(stats: List<MonthlyClimateStats>): List<TableRow> {
    return listOf(
        TableRow(
            label = "Snitt",
            unit = "m/s",
            values = stats.map { formatValue(it.windSpeed) }
        )
    )
}
