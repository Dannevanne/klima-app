package no.uio.ifi.in2000.team45.ui.statistic

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.favorites.FavoriteStation
import no.uio.ifi.in2000.team45.data.statistic.AnnualObservation
import no.uio.ifi.in2000.team45.data.statistic.MonthlyClimateStats
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation
import no.uio.ifi.in2000.team45.ui.favorites.FavoriteViewModel
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource

private val MONTH_NAMES = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticScreen(
    statisticViewModel: StatisticViewModel,
    onSettingsClick: () -> Unit,
    onInfoClick: () -> Unit,
    onHelpClick: () -> Unit,
    onMapClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    favoriteViewModel: FavoriteViewModel,
    modifier: Modifier = Modifier
) {
    val favoriteStations by favoriteViewModel.stations.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            statisticViewModel.cancelStatisticScreenWork()
        }
    }

    var expanded by remember { mutableStateOf(false)}
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.fillMaxWidth(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    Icon(
                        painterResource(R.drawable.bar_chart),
                        contentDescription = "")
                },
                title = {Text(
                    text = stringResource(R.string.climate_statistics),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )},
                actions = {
                    IconButton(onClick = {expanded = true}) {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = stringResource(R.string.menu)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {expanded = false}
                    ) {
                        DropdownMenuItem(
                            onClick = onSettingsClick,
                            leadingIcon = { Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = ""
                            ) },
                            text = { Text(text = stringResource(R.string.settings)) }
                        )
                        DropdownMenuItem(
                            onClick = onInfoClick,
                            leadingIcon = { Icon(
                                    imageVector = Icons.Outlined.Info,
                                contentDescription = ""
                            ) },
                            text = { Text(text = stringResource(R.string.info)) }
                        )
                        DropdownMenuItem(
                            onClick = onHelpClick,
                            leadingIcon = { Icon(
                                painter = painterResource(R.drawable.outline_help),
                                contentDescription = ""
                            ) },
                            text = { Text(text = stringResource(R.string.help)) }
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onMapClick) {
                        Icon(Icons.Outlined.Place,
                            contentDescription = stringResource(R.string.map))
                    }
                    IconButton({}) {
                        Icon(painterResource(R.drawable.filled_analytics),
                            contentDescription = "")
                    }
                    IconButton(onClick = onFavoritesClick) {
                        Icon(Icons.Outlined.FavoriteBorder,
                            contentDescription = stringResource(R.string.favorites)
                        )
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

            SearchBar(
                query = statisticViewModel.searchQuery,
                onQueryChange = { statisticViewModel.updateSearchQuery(it) },
                onSearch = { statisticViewModel.searchStations() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (val state = statisticViewModel.uiState) {
                is StatisticUiState.Idle -> {
                    val history = statisticViewModel.searchHistory
                    val hasContent = favoriteStations.isNotEmpty() || history.isNotEmpty()

                    if (hasContent) {
                        LazyColumn {
                            if (history.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Tidligere sok",
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                items(history) { query ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .clickable {
                                                statisticViewModel.updateSearchQuery(query)
                                                statisticViewModel.searchStations()
                                            },
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Filled.Search,
                                                contentDescription = null,
                                                modifier = Modifier.padding(end = 8.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(query, fontSize = 15.sp)
                                        }
                                    }
                                }
                                item { Spacer(modifier = Modifier.height(12.dp)) }
                            }

                            if (favoriteStations.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Lagrede stasjoner",
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                items(favoriteStations) { fav ->
                                    val station = WeatherStation(
                                        id = fav.stationId,
                                        name = fav.name,
                                        latitude = 0.0, longitude = 0.0,
                                        municipality = fav.municipality,
                                        county = fav.county
                                    )
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { statisticViewModel.selectStation(station) },
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
                                                Text(fav.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                                if (fav.municipality.isNotBlank()) {
                                                    Text(
                                                        "${fav.municipality}, ${fav.county}",
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        IdleContent()
                    }
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
                    val favIds = favoriteStations.map { it.stationId }.toSet()
                    // Pinned stations first, then the rest
                    val sorted = state.stations.sortedByDescending { it.id in favIds }
                    StationList(
                        stations = sorted,
                        favoriteIds = favIds,
                        onStationClick = { statisticViewModel.selectStation(it) },
                        onToggleFavorite = { station ->
                            val isFav = station.id in favIds
                            if (isFav) {
                                favoriteViewModel.removeStation(station.id)
                            } else {
                                favoriteViewModel.addStation(station)
                            }
                        }
                    )
                }
                is StatisticUiState.ClimateData -> {
                    val isFav = favoriteStations.any { it.stationId == state.station.id }
                    ClimateDataView(
                        station = state.station,
                        stats = state.stats,
                        annualData = state.annualData,
                        selectedPeriod = statisticViewModel.selectedPeriod,
                        onPeriodChange = { statisticViewModel.updatePeriod(it) },
                        isFavorite = isFav,
                        onToggleFavorite = {
                            if (isFav) {
                                favoriteViewModel.removeStation(state.station.id)
                            } else {
                                favoriteViewModel.addStation(state.station)
                            }
                        },
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
        label = { Text(stringResource(R.string.place_or_station)) },
        trailingIcon = {
            IconButton(onClick = onSearch) {
                Icon(Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search))
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
                text = stringResource(R.string.search_to_see_stats),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.ex_oslo_bergen_troms),
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
    favoriteIds: Set<String>,
    onStationClick: (WeatherStation) -> Unit,
    onToggleFavorite: (WeatherStation) -> Unit
) {
    Text(
        text = stringResource(R.string.choose_station),
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    LazyColumn {
        items(stations.take(20)) { station ->
            val isFav = station.id in favoriteIds
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onStationClick(station) },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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
                        station.distanceMeters?.let { dist ->
                            val distText = if (dist >= 1000) {
                                "%.1f km unna".format(dist / 1000.0)
                            } else {
                                "$dist m unna"
                            }
                            Text(
                                text = distText,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { onToggleFavorite(station) }) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = if (isFav) "Fjern favoritt" else "Legg til favoritt",
                            tint = if (isFav) androidx.compose.ui.graphics.Color(0xFFFFC107)
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private val PERIOD_OPTIONS = listOf(10, 30, 50)

@Composable
private fun ClimateDataView(
    station: WeatherStation,
    stats: List<MonthlyClimateStats>,
    annualData: List<AnnualObservation>,
    selectedPeriod: Int,
    onPeriodChange: (Int) -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn {
        item {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.back_to_search))
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                PERIOD_OPTIONS.forEach { years ->
                    FilterChip(
                        selected = selectedPeriod == years,
                        onClick = { onPeriodChange(years) },
                        label = { Text("$years ar") }
                    )
                }
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = station.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 4.dp)
                )
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = if (isFavorite) "Fjern favoritt" else "Legg til favoritt",
                        tint = if (isFavorite) androidx.compose.ui.graphics.Color(0xFFFFC107)
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (station.municipality.isNotBlank()) {
                Text(
                    text = "${station.municipality}, ${station.county}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        if (annualData.isNotEmpty()) {
            item {
                OverviewCards(annualData = annualData, selectedPeriod = selectedPeriod)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (annualData.any { it.totalPrecip != null }) {
            item {
                TrendChart(
                    title = "Arlig nedbor (mm)",
                    data = annualData.mapNotNull { a ->
                        a.totalPrecip?.let { a.year to it }
                    },
                    lineColor = Color(0xFF42A5F5)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (annualData.any { it.meanTemp != null }) {
            item {
                TrendChart(
                    title = "Arlig gjennomsnittstemperatur (C)",
                    data = annualData.mapNotNull { a ->
                        a.meanTemp?.let { a.year to it }
                    },
                    lineColor = Color(0xFFEF5350),
                    fillColor = Color(0x3042A5F5)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (stats.any { it.meanTemp != null }) {
            item {
                ClimateTable(
                    title = stringResource(R.string.temperature),
                    stats = stats,
                    rows = buildTemperatureRows(stats)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (stats.any { it.precipitation != null }) {
            item {
                ClimateTable(
                    title = stringResource(R.string.precipitation),
                    stats = stats,
                    rows = buildPrecipitationRows(stats)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (stats.any { it.windSpeed != null }) {
            item {
                ClimateTable(
                    title = stringResource(R.string.wind),
                    stats = stats,
                    rows = buildWindRows(stats)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (stats.any { it.snowDepth != null }) {
            item {
                ClimateTable(
                    title = "Snodybde",
                    stats = stats,
                    rows = buildSnowRows(stats)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (stats.any { it.cloudCover != null }) {
            item {
                ClimateTable(
                    title = "Skydekke",
                    stats = stats,
                    rows = buildCloudRows(stats)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // UV index estimate based on latitude and month
        item {
            UvIndexCard(latitude = station.latitude)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            StationInfoCard(station = station)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun OverviewCards(
    annualData: List<AnnualObservation>,
    selectedPeriod: Int
) {
    val avgPrecip = annualData.mapNotNull { it.totalPrecip }.average()
        .takeIf { annualData.any { a -> a.totalPrecip != null } }
    val avgTemp = annualData.mapNotNull { it.meanTemp }.average()
        .takeIf { annualData.any { a -> a.meanTemp != null } }

    // Trend: compare first half vs second half of the period
    val precipTrend = computeTrend(annualData.mapNotNull { it.totalPrecip })
    val tempTrend = computeTrend(annualData.mapNotNull { it.meanTemp })

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Oversikt siste $selectedPeriod ar",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (avgPrecip != null) {
                    OverviewStat(
                        label = "Nedbor snitt/ar",
                        value = "${avgPrecip.toInt()} mm",
                        trend = precipTrend,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (avgTemp != null) {
                    OverviewStat(
                        label = "Temperatur snitt",
                        value = "${String.format("%.1f", avgTemp)} C",
                        trend = tempTrend,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewStat(
    label: String,
    value: String,
    trend: TrendInfo,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            val trendColor = when (trend.direction) {
                TrendDirection.UP -> Color(0xFFE53935)
                TrendDirection.DOWN -> Color(0xFF43A047)
                TrendDirection.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            val arrow = when (trend.direction) {
                TrendDirection.UP -> "^"
                TrendDirection.DOWN -> "v"
                TrendDirection.STABLE -> "~"
            }
            Text(
                text = arrow,
                color = trendColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
            if (trend.percentChange != null) {
                Text(
                    text = "${String.format("%+.0f", trend.percentChange)}%",
                    color = trendColor,
                    fontSize = 13.sp
                )
            }
        }
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

data class TrendInfo(
    val direction: TrendDirection,
    val percentChange: Double?
)

enum class TrendDirection { UP, DOWN, STABLE }

private fun computeTrend(values: List<Double>): TrendInfo {
    if (values.size < 4) return TrendInfo(TrendDirection.STABLE, null)
    val mid = values.size / 2
    val firstHalf = values.subList(0, mid).average()
    val secondHalf = values.subList(mid, values.size).average()

    if (firstHalf == 0.0) return TrendInfo(TrendDirection.STABLE, null)

    val change = ((secondHalf - firstHalf) / firstHalf) * 100.0
    val direction = when {
        change > 3.0 -> TrendDirection.UP
        change < -3.0 -> TrendDirection.DOWN
        else -> TrendDirection.STABLE
    }
    return TrendInfo(direction, change)
}

@Composable
private fun StationInfoCard(station: WeatherStation) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Malestasjon",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(text = station.name, fontSize = 14.sp)
            Text(
                text = "Frost-ID: ${station.id}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (station.municipality.isNotBlank()) {
                Text(
                    text = "${station.municipality}, ${station.county}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Pos: ${String.format("%.4f", station.latitude)}, ${String.format("%.4f", station.longitude)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            station.distanceMeters?.let { dist ->
                val distText = if (dist >= 1000) {
                    "%.1f km fra sokestedet".format(dist / 1000.0)
                } else {
                    "$dist m fra sokestedet"
                }
                Text(
                    text = distText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TrendChart(
    title: String,
    data: List<Pair<Int, Double>>,
    lineColor: Color,
    fillColor: Color? = null
) {
    if (data.size < 2) return

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val minVal = data.minOf { it.second }
            val maxVal = data.maxOf { it.second }
            val range = (maxVal - minVal).coerceAtLeast(1.0)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val w = size.width
                val h = size.height
                val padBottom = 20f
                val chartH = h - padBottom
                val step = w / (data.size - 1).coerceAtLeast(1)

                val points = data.mapIndexed { i, (_, v) ->
                    val x = i * step
                    val y = chartH - ((v - minVal) / range * chartH).toFloat()
                    Offset(x, y)
                }

                // Fill area under line
                if (fillColor != null && points.size >= 2) {
                    val path = Path().apply {
                        moveTo(points.first().x, chartH)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, chartH)
                        close()
                    }
                    drawPath(path, color = Color(fillColor.value))
                }

                // Draw line
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = lineColor,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 3f
                    )
                }

                // Draw dots
                points.forEach { p ->
                    drawCircle(color = lineColor, radius = 4f, center = p)
                }
            }

            // Year labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val firstYear = data.first().first
                val lastYear = data.last().first
                Text(firstYear.toString(), fontSize = 10.sp, color = Color.Gray)
                if (data.size > 2) {
                    val mid = data[data.size / 2].first
                    Text(mid.toString(), fontSize = 10.sp, color = Color.Gray)
                }
                Text(lastYear.toString(), fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

enum class ColorScale { TEMPERATURE, PRECIPITATION, WIND, SNOW, NONE }

data class TableRow(
    val label: String,
    val unit: String,
    val values: List<String>,
    val rawValues: List<Double?> = emptyList(),
    val colorScale: ColorScale = ColorScale.NONE
)

// Blue (cold) -> white (mild) -> red (hot)
private fun temperatureColor(value: Double?): Color {
    if (value == null) return Color.Transparent
    return when {
        value <= -15 -> Color(0xFFBBDEFB)
        value <= -5 -> Color(0xFFE3F2FD)
        value <= 5 -> Color(0xFFF5F5F5)
        value <= 15 -> Color(0xFFFFF9C4)
        value <= 25 -> Color(0xFFFFCCBC)
        else -> Color(0xFFEF9A9A)
    }
}

// Green (dry) -> blue (wet)
private fun precipitationColor(value: Double?): Color {
    if (value == null) return Color.Transparent
    return when {
        value <= 30 -> Color(0xFFE8F5E9)
        value <= 80 -> Color(0xFFC8E6C9)
        value <= 150 -> Color(0xFFBBDEFB)
        value <= 250 -> Color(0xFF90CAF9)
        else -> Color(0xFF64B5F6)
    }
}

// Green (calm) -> red (strong)
private fun windColor(value: Double?): Color {
    if (value == null) return Color.Transparent
    return when {
        value <= 2 -> Color(0xFFE8F5E9)
        value <= 5 -> Color(0xFFC8E6C9)
        value <= 8 -> Color(0xFFFFF9C4)
        value <= 12 -> Color(0xFFFFCCBC)
        else -> Color(0xFFEF9A9A)
    }
}

// White (no snow) -> light blue (deep snow)
private fun snowColor(value: Double?): Color {
    if (value == null) return Color.Transparent
    return when {
        value <= 1 -> Color(0xFFF5F5F5)
        value <= 10 -> Color(0xFFE3F2FD)
        value <= 30 -> Color(0xFFBBDEFB)
        value <= 60 -> Color(0xFF90CAF9)
        else -> Color(0xFF64B5F6)
    }
}

private fun cellColor(value: Double?, scale: ColorScale): Color {
    return when (scale) {
        ColorScale.TEMPERATURE -> temperatureColor(value)
        ColorScale.PRECIPITATION -> precipitationColor(value)
        ColorScale.WIND -> windColor(value)
        ColorScale.SNOW -> snowColor(value)
        ColorScale.NONE -> Color.Transparent
    }
}

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
                            modifier = Modifier
                                .width(80.dp)
                                .padding(4.dp),
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
                                modifier = Modifier
                                    .width(48.dp)
                                    .padding(4.dp),
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
                                modifier = Modifier
                                    .width(80.dp)
                                    .padding(4.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "${row.label} (${row.unit})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            row.values.forEachIndexed { index, value ->
                                val rawVal = row.rawValues.getOrNull(index)
                                val bgColor = cellColor(rawVal, row.colorScale)
                                Box(
                                    modifier = Modifier
                                        .width(48.dp)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(bgColor)
                                        .padding(2.dp),
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
                Text(stringResource(R.string.try_again))
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
                values = stats.map { formatValue(it.meanTemp) },
                rawValues = stats.map { it.meanTemp },
                colorScale = ColorScale.TEMPERATURE
            )
        )
    }
    if (stats.any { it.maxTemp != null }) {
        rows.add(
            TableRow(
                label = "Maks",
                unit = "C",
                values = stats.map { formatValue(it.maxTemp) },
                rawValues = stats.map { it.maxTemp },
                colorScale = ColorScale.TEMPERATURE
            )
        )
    }
    if (stats.any { it.minTemp != null }) {
        rows.add(
            TableRow(
                label = "Min",
                unit = "C",
                values = stats.map { formatValue(it.minTemp) },
                rawValues = stats.map { it.minTemp },
                colorScale = ColorScale.TEMPERATURE
            )
        )
    }
    if (stats.any { it.recordHigh != null }) {
        rows.add(
            TableRow(
                label = "Rekord H",
                unit = "C",
                values = stats.map { formatValue(it.recordHigh) },
                rawValues = stats.map { it.recordHigh },
                colorScale = ColorScale.TEMPERATURE
            )
        )
    }
    if (stats.any { it.recordLow != null }) {
        rows.add(
            TableRow(
                label = "Rekord L",
                unit = "C",
                values = stats.map { formatValue(it.recordLow) },
                rawValues = stats.map { it.recordLow },
                colorScale = ColorScale.TEMPERATURE
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
            values = stats.map { formatValue(it.precipitation) },
            rawValues = stats.map { it.precipitation },
            colorScale = ColorScale.PRECIPITATION
        )
    )
}

private fun buildWindRows(stats: List<MonthlyClimateStats>): List<TableRow> {
    val rows = mutableListOf<TableRow>()

    if (stats.any { it.windSpeed != null }) {
        rows.add(
            TableRow(
                label = "Snitt",
                unit = "m/s",
                values = stats.map { formatValue(it.windSpeed) },
                rawValues = stats.map { it.windSpeed },
                colorScale = ColorScale.WIND
            )
        )
    }
    if (stats.any { it.windGust != null }) {
        rows.add(
            TableRow(
                label = "Kast",
                unit = "m/s",
                values = stats.map { formatValue(it.windGust) },
                rawValues = stats.map { it.windGust },
                colorScale = ColorScale.WIND
            )
        )
    }
    return rows
}

private fun buildSnowRows(stats: List<MonthlyClimateStats>): List<TableRow> {
    return listOf(
        TableRow(
            label = "Snitt",
            unit = "cm",
            values = stats.map { formatValue(it.snowDepth) },
            rawValues = stats.map { it.snowDepth },
            colorScale = ColorScale.SNOW
        )
    )
}

private fun buildCloudRows(stats: List<MonthlyClimateStats>): List<TableRow> {
    return listOf(
        TableRow(
            label = "Snitt",
            unit = "%",
            values = stats.map { formatValue(it.cloudCover) },
            rawValues = stats.map { it.cloudCover },
            colorScale = ColorScale.NONE
        )
    )
}

// Estimate peak summer UV index based on latitude.
// Southern Norway (~58N) gets UV ~6-7, northern (~70N) gets ~3-4 in summer.
// Current month scales this down (winter = near zero).
private fun estimateUvIndex(latitude: Double): Double {
    val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
    // Peak UV for this latitude (summer solstice)
    val peakUv = (10.0 - (latitude - 50.0) * 0.15).coerceIn(1.0, 11.0)
    // Monthly scaling factor (peaks in June/July)
    val monthFactor = when (month) {
        1, 12 -> 0.05
        2, 11 -> 0.10
        3, 10 -> 0.25
        4, 9 -> 0.50
        5, 8 -> 0.75
        6, 7 -> 1.0
        else -> 0.3
    }
    return (peakUv * monthFactor).coerceIn(0.0, 11.0)
}

private fun uvLabel(uv: Double): String = when {
    uv < 3 -> "Lav"
    uv < 6 -> "Moderat"
    uv < 8 -> "Hoy"
    uv < 11 -> "Veldig hoy"
    else -> "Ekstrem"
}

@Composable
private fun UvIndexCard(latitude: Double) {
    val uvIndex = estimateUvIndex(latitude)
    val maxUv = 11.0

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "UV-indeks (estimat for denne maneden)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "${String.format("%.1f", uvIndex)} - ${uvLabel(uvIndex)}",
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Gradient bar with indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                // Gradient background
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 8.dp)
                ) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF4CAF50), // green - low
                                Color(0xFFCDDC39), // lime
                                Color(0xFFFFEB3B), // yellow - moderate
                                Color(0xFFFF9800), // orange - high
                                Color(0xFFF44336), // red - very high
                                Color(0xFF9C27B0)  // purple - extreme
                            )
                        )
                    )
                }

                // Indicator dot
                val fraction = (uvIndex / maxUv).toFloat().coerceIn(0f, 1f)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    val x = size.width * fraction
                    // Triangle pointer at top
                    drawCircle(
                        color = Color.Black,
                        radius = 8f,
                        center = Offset(x, 6f)
                    )
                    // Line down to bar
                    drawLine(
                        color = Color.Black,
                        start = Offset(x, 10f),
                        end = Offset(x, size.height),
                        strokeWidth = 2f
                    )
                }
            }

            // Scale labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0", fontSize = 10.sp, color = Color.Gray)
                Text("3", fontSize = 10.sp, color = Color.Gray)
                Text("6", fontSize = 10.sp, color = Color.Gray)
                Text("8", fontSize = 10.sp, color = Color.Gray)
                Text("11+", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}
