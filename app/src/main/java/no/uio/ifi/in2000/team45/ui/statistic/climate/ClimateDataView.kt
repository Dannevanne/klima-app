package no.uio.ifi.in2000.team45.ui.statistic.climate

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.team45.data.statistic.AnnualObservation
import no.uio.ifi.in2000.team45.data.statistic.MonthlyClimateStats
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation

private val PERIOD_OPTIONS = listOf(10, 20, 30, 50)

@Composable
fun ClimateDataView(
    station: WeatherStation,
    stats: List<MonthlyClimateStats>,
    annualData: List<AnnualObservation>,
    selectedPeriod: Int,
    onPeriodChange: (Int) -> Unit
) {
    LazyColumn {
        item {
            missingData(annualData, selectedPeriod)
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
                        label = { Text("$years years") }
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
                    title = "ANNUAL PRECIPITATION - TREND (MM)",
                    data = annualData.mapNotNull { a ->
                        a.totalPrecip?.let { a.year to it }
                    },
                    lineColor = Color(0xFF42A5F5),
                    fillColor = Color(0x3042A5F5)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (annualData.any { it.meanTemp != null }) {
            item {
                TrendChart(
                    title = "AVERAGE ANNUAL TEMPERATURE (C)",
                    data = annualData.mapNotNull { a ->
                        a.meanTemp?.let { a.year to it }
                    },
                    lineColor = Color(0xFFFF9800),
                    fillColor = Color(0x30FF9800)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            StationsUsedCard(station = station, annualData = annualData)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}