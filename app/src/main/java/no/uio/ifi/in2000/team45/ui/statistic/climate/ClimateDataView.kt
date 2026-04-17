package no.uio.ifi.in2000.team45.ui.statistic.climate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.statistic.AnnualObservation
import no.uio.ifi.in2000.team45.data.statistic.MonthlyClimateStats
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation
import no.uio.ifi.in2000.team45.ui.statistic.ClimateTable
import no.uio.ifi.in2000.team45.ui.statistic.OverviewCards
import no.uio.ifi.in2000.team45.ui.statistic.PERIOD_OPTIONS
import no.uio.ifi.in2000.team45.ui.statistic.StationInfoCard
import no.uio.ifi.in2000.team45.ui.statistic.TrendChart
import no.uio.ifi.in2000.team45.ui.statistic.UvIndexCard
import no.uio.ifi.in2000.team45.ui.statistic.buildCloudRows
import no.uio.ifi.in2000.team45.ui.statistic.buildPrecipitationRows
import no.uio.ifi.in2000.team45.ui.statistic.buildSnowRows
import no.uio.ifi.in2000.team45.ui.statistic.buildTemperatureRows
import no.uio.ifi.in2000.team45.ui.statistic.buildWindRows
import no.uio.ifi.in2000.team45.ui.statistic.missingData
import no.uio.ifi.in2000.team45.ui.theme.StarGold

private val PERIOD_OPTIONS = listOf(10, 30, 50)

@Composable
fun ClimateDataView(
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
            missingData(annualData, selectedPeriod)
        }
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
                        tint = if (isFavorite) StarGold
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