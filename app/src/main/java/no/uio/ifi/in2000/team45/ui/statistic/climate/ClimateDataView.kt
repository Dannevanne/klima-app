package no.uio.ifi.in2000.team45.ui.statistic.climate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.statistic.AnnualObservation
import no.uio.ifi.in2000.team45.data.statistic.MonthlyClimateStats
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation
import no.uio.ifi.in2000.team45.ui.theme.FillColorBlue
import no.uio.ifi.in2000.team45.ui.theme.FillColorOrange
import no.uio.ifi.in2000.team45.ui.theme.FillColorPurple
import no.uio.ifi.in2000.team45.ui.theme.FillColorTurquoise
import no.uio.ifi.in2000.team45.ui.theme.LineColorBlue
import no.uio.ifi.in2000.team45.ui.theme.LineColorOrange
import no.uio.ifi.in2000.team45.ui.theme.LineColorPurple
import no.uio.ifi.in2000.team45.ui.theme.LineColorTurquoise

private val PERIOD_OPTIONS = listOf(5, 10, 20, 30, 50)

@Composable
fun ClimateDataView(
    station: WeatherStation,
    stats: List<MonthlyClimateStats>,
    annualData: List<AnnualObservation>,
    selectedPeriod: Int,
    onPeriodChange: (Int) -> Unit,
    header: @Composable () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier, contentPadding = contentPadding) {
        item { header() }

        item {
            MissingData(annualData, selectedPeriod)
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
                        label = { Text(stringResource(R.string.years_count, years)) }
                    )
                }
            }
        }


        if (annualData.isNotEmpty()) {
            item {
                OverviewCards(annualData = annualData, stats = stats, selectedPeriod = selectedPeriod)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (annualData.any { it.totalPrecip != null }) {
            item {
                TrendChart(
                    title = stringResource(R.string.annual_precipitation_trend),
                    data = annualData.mapNotNull { a ->
                        a.totalPrecip?.let { a.year to it }
                    },
                    lineColor = LineColorBlue,
                    fillColor = FillColorBlue
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (annualData.any { it.meanTemp != null }) {
            item {
                TrendChart(
                    title = stringResource(R.string.average_annual_temperature),
                    data = annualData.mapNotNull { a ->
                        a.meanTemp?.let { a.year to it }
                    },
                    lineColor = LineColorOrange,
                    fillColor = FillColorOrange
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (annualData.any { it.maxWindGust != null }) {
            item {
                TrendChart(
                    title = stringResource(R.string.annual_max_gust_trend),
                    data = annualData.mapNotNull { a ->
                        a.maxWindGust?.let { a.year to it }
                    },
                    lineColor = LineColorPurple,
                    fillColor = FillColorPurple
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (annualData.any { it.maxSnowDepth != null }) {
            item {
                TrendChart(
                    title = stringResource(R.string.annual_max_snow_trend),
                    data = annualData.mapNotNull { a ->
                        a.maxSnowDepth?.let { a.year to it }
                    },
                    lineColor = LineColorTurquoise,
                    fillColor = FillColorTurquoise
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            StationsUsedCard(station = station, annualData = annualData)
        }
    }
}