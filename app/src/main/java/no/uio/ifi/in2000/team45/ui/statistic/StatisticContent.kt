package no.uio.ifi.in2000.team45.ui.statistic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.data.favorites.FavoriteStation
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation
import no.uio.ifi.in2000.team45.ui.favorites.FavoriteViewModel
import no.uio.ifi.in2000.team45.ui.statistic.climate.ClimateDataView
import no.uio.ifi.in2000.team45.ui.statistic.components.ErrorContent
import no.uio.ifi.in2000.team45.ui.statistic.components.IdleContent
import no.uio.ifi.in2000.team45.ui.statistic.components.StationList
import no.uio.ifi.in2000.team45.ui.theme.StarGold

@Composable
fun StatisticContent(
    state: StatisticUiState,
    favoriteStations: List<FavoriteStation>,
    statisticViewModel: StatisticViewModel,
    favoriteViewModel: FavoriteViewModel
) {
    when (state) {
        is StatisticUiState.Idle -> {
            val history = statisticViewModel.searchHistory
            val hasContent = favoriteStations.isNotEmpty() || history.isNotEmpty()

            if (hasContent) {
                LazyColumn {
                    if (history.isNotEmpty()) {
                        item {
                            Text(
                                text = "Tidligere søk",
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
                                        .background(MaterialTheme.colorScheme.surfaceContainer)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = StarGold,
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
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
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