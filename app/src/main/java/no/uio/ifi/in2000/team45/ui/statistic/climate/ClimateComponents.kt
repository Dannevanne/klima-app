package no.uio.ifi.in2000.team45.ui.statistic.climate

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.statistic.AnnualObservation
import no.uio.ifi.in2000.team45.data.statistic.MonthlyClimateStats
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation

@Composable
fun OverviewCards(
    annualData: List<AnnualObservation>,
    stats: List<MonthlyClimateStats>,
    selectedPeriod: Int
) {
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val fromYear = currentYear - selectedPeriod

    val avgPrecip = annualData.mapNotNull { it.totalPrecip }.average()
        .takeIf { annualData.any { a -> a.totalPrecip != null } }
    val maxWindGust = annualData.mapNotNull { it.maxWindGust }.maxOrNull()
    val maxSnowDepth = annualData.mapNotNull { it.maxSnowDepth }.maxOrNull()
    val extremePrecipDays = annualData.mapNotNull { it.totalPrecip }
        .count { it > (avgPrecip ?: Double.MAX_VALUE) * 1.3 }

    // Climate normals from monthly stats. Annual precip = sum of 12 monthly
    // normals; gust/snow normals are sparse so often null.
    val normalAnnualPrecip = stats.mapNotNull { it.precipitation }
        .takeIf { it.size == 12 }?.sum()
    val normalMaxGust = stats.mapNotNull { it.windGust }.maxOrNull()
    val normalMaxSnow = stats.mapNotNull { it.snowDepth }.maxOrNull()
    val expectedExtreme = selectedPeriod * 0.10

    val precipTrend = vsNormal(avgPrecip, normalAnnualPrecip)
    val windTrend = vsNormal(maxWindGust, normalMaxGust)
    val snowTrend = vsNormal(maxSnowDepth, normalMaxSnow)
    val extremeTrend = if (annualData.any { it.totalPrecip != null }) {
        vsNormal(extremePrecipDays.toDouble(), expectedExtreme)
    } else TrendInfo(TrendDirection.STABLE, null)

    Text(
        text = stringResource(R.string.overview_period, fromYear, currentYear),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OverviewStatCard(
                label = stringResource(R.string.precipitation_avg_year),
                value = if (avgPrecip != null) "${avgPrecip.toInt()} mm" else "-",
                trend = precipTrend,
                modifier = Modifier.weight(1f)
            )
            OverviewStatCard(
                label = stringResource(R.string.max_wind_gust),
                value = if (maxWindGust != null) "${String.format("%.1f", maxWindGust)} m/s" else "-",
                trend = windTrend,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OverviewStatCard(
                label = stringResource(R.string.max_snow_depth),
                value = if (maxSnowDepth != null) "${maxSnowDepth.toInt()} cm" else "-",
                trend = snowTrend,
                modifier = Modifier.weight(1f)
            )
            OverviewStatCard(
                label = stringResource(R.string.extreme_precipitation),
                value = if (extremePrecipDays > 0) stringResource(R.string.years_count, extremePrecipDays) else "-",
                trend = extremeTrend,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun OverviewStatCard(
    label: String,
    value: String,
    trend: TrendInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            if (trend.percentChange != null) {
                Spacer(modifier = Modifier.height(2.dp))
                val trendColor = when (trend.direction) {
                    TrendDirection.UP -> Color(0xFFEF4444)
                    TrendDirection.DOWN -> Color(0xFF22C55E)
                    TrendDirection.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val arrow = when (trend.direction) {
                    TrendDirection.UP -> "↑"
                    TrendDirection.DOWN -> "↓"
                    TrendDirection.STABLE -> "→"
                }
                val vsNorm = stringResource(R.string.trend_vs_norm)
                Text(
                    text = "$arrow ${String.format("%+.0f", trend.percentChange)}% $vsNorm",
                    color = trendColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun TrendChart(
    title: String,
    data: List<Pair<Int, Double>>,
    lineColor: Color,
    fillColor: Color? = null
) {
    if (data.size < 2) return

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val minVal = data.minOf { it.second }
            val maxVal = data.maxOf { it.second }
            val range = (maxVal - minVal).coerceAtLeast(1.0)
            val midVal = (minVal + maxVal) / 2.0

            Row {
                // Y-axis labels
                Column(
                    modifier = Modifier
                        .height(150.dp)
                        .width(40.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format("%.0f", maxVal),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = String.format("%.0f", midVal),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = String.format("%.0f", minVal),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                Canvas(
                    modifier = Modifier
                        .weight(1f)
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

@Composable
fun StationsUsedCard(
    station: WeatherStation,
    annualData: List<AnnualObservation>
) {
    val obsCount = annualData.size

    Text(
        text = stringResource(R.string.weather_stations_used),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = station.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.frost_id_prefix, station.id),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (station.municipality.isNotBlank()) {
                    Text(
                        text = "${station.municipality}, ${station.county}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.precipitation_plus_temperature),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$obsCount obs.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


// If selected place dont have data for all years in chosen periode,
// a warning-message appear telling this
@Composable
fun MissingData(
    annualData: List<AnnualObservation>,
    selectedPeriod: Int
){
    val years = annualData.map { it.year }.distinct().count()
    val missingData = years < selectedPeriod
    if (missingData){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = stringResource(R.string.this_place_is_missing_some_data),
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }
    }
}