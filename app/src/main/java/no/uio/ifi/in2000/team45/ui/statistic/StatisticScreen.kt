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
import no.uio.ifi.in2000.team45.ui.NavBarItem
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import no.uio.ifi.in2000.team45.ui.common.AppTopBar
import no.uio.ifi.in2000.team45.ui.common.OverflowMenu
import no.uio.ifi.in2000.team45.ui.common.OverflowMenuButton
import no.uio.ifi.in2000.team45.ui.statistic.components.SearchBar
import no.uio.ifi.in2000.team45.ui.theme.StarGold

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.climate_statistics),
                titleFontSize = 24.sp,
                navigationIcon = {
                    Icon(
                        painterResource(R.drawable.bar_chart),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    OverflowMenuButton(onClick = { expanded = true } )

                    OverflowMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        onSettingsClick = onSettingsClick,
                        onInfoClick = onInfoClick,
                        onHelpClick = onHelpClick
                    )
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NavBarItem(
                        onClick = onMapClick,
                        icon = { Icon(Icons.Outlined.Place, contentDescription = null) },
                        label = stringResource(R.string.nav_map),
                        modifier = Modifier.weight(1f)
                    )
                    NavBarItem(
                        onClick = {},
                        icon = { Icon(painterResource(R.drawable.filled_analytics), contentDescription = null) },
                        label = stringResource(R.string.nav_statistics),
                        modifier = Modifier.weight(1f)
                    )
                    NavBarItem(
                        onClick = onFavoritesClick,
                        icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null) },
                        label = stringResource(R.string.saved_places),
                        modifier = Modifier.weight(1f)
                    )
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

            StatisticContent(
                state = statisticViewModel.uiState,
                favoriteStations = favoriteStations,
                statisticViewModel = statisticViewModel,
                favoriteViewModel = favoriteViewModel
            )
        }
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
        Column(modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(12.dp)) {
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
