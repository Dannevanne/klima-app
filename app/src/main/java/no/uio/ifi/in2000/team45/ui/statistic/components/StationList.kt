package no.uio.ifi.in2000.team45.ui.statistic.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.statistic.models.WeatherStation

private fun String.toDisplayCase(): String {
    if (isBlank()) return this
    return split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}

@Composable
fun StationList(
    modifier: Modifier = Modifier,
    stations: List<WeatherStation>,
    favoriteIds: Set<String>,
    onStationClick: (WeatherStation) -> Unit,
    onToggleFavorite: (WeatherStation) -> Unit,
    header: @Composable () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyColumn(modifier = modifier, contentPadding = contentPadding) {
        item { header() }
        item {
            Text(
                text = stringResource(R.string.choose_station),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(stations.take(20)) { station ->
            val isFav = station.id in favoriteIds
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onStationClick(station) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val title = station.municipality.toDisplayCase()
                            .ifBlank { station.name }
                        val subtitle = station.county.toDisplayCase()
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        if (subtitle.isNotBlank()) {
                            Text(
                                text = subtitle,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        station.distanceMeters?.let { dist ->
                            val distText = if (dist >= 1000) {
                                stringResource(R.string.distance_km_away, "%.1f".format(dist / 1000.0))
                            } else {
                                stringResource(R.string.distance_m_away, dist)
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
                            imageVector = if (isFav) Icons.Outlined.Delete else Icons.Outlined.Star,
                            contentDescription = if (isFav) stringResource(R.string.remove_saved_place) else stringResource(R.string.add_place_to_saved_places),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}