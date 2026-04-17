package no.uio.ifi.in2000.team45.ui.statistic.climate

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.statistic.AnnualObservation
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation
import no.uio.ifi.in2000.team45.ui.statistic.OverviewStat
import no.uio.ifi.in2000.team45.ui.statistic.TrendDirection
import no.uio.ifi.in2000.team45.ui.statistic.TrendInfo
import no.uio.ifi.in2000.team45.ui.statistic.computeTrend
import no.uio.ifi.in2000.team45.ui.statistic.estimateUvIndex
import no.uio.ifi.in2000.team45.ui.statistic.uvLabel

@Composable
fun OverviewCards(
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
fun OverviewStat(
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

@Composable
fun StationInfoCard(station: WeatherStation) {
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
fun TrendChart(
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

@Composable
fun UvIndexCard(latitude: Double) {
    val uvIndex = estimateUvIndex(latitude)
    val maxUv = 11.0
    val indicatorColor = MaterialTheme.colorScheme.onSurface
    val scaleLabelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(12.dp)) {
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
                        color = indicatorColor,
                        radius = 8f,
                        center = Offset(x, 6f)
                    )
                    // Line down to bar
                    drawLine(
                        color = indicatorColor,
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
                Text("0", fontSize = 10.sp, color = scaleLabelColor)
                Text("3", fontSize = 10.sp, color = scaleLabelColor)
                Text("6", fontSize = 10.sp, color = scaleLabelColor)
                Text("8", fontSize = 10.sp, color = scaleLabelColor)
                Text("11+", fontSize = 10.sp, color = scaleLabelColor)
            }
        }
    }
}
// If selected place dont have data for all years in chosen periode,
// a warning-message appear telling this
@Composable
fun missingData(
    annualData: List<AnnualObservation>,
    selectedPeriod: Int
){
    val years = annualData.map { it.year }.distinct().count()
    val missingData = years < selectedPeriod
    if (missingData){
        Text(
            text = stringResource(R.string.this_place_is_missing_some_data),
            color = MaterialTheme.colorScheme.error,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}