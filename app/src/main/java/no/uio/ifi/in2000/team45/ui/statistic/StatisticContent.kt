package no.uio.ifi.in2000.team45.ui.statistic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.savedplaces.FavoriteStation
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation
import no.uio.ifi.in2000.team45.ui.savedplaces.SavedPlacesViewModel
import no.uio.ifi.in2000.team45.ui.statistic.climate.ClimateDataView
import no.uio.ifi.in2000.team45.ui.statistic.components.ErrorContent
import no.uio.ifi.in2000.team45.ui.statistic.components.IdleContent
import no.uio.ifi.in2000.team45.ui.statistic.components.SearchBar
import no.uio.ifi.in2000.team45.ui.statistic.components.StationList
import no.uio.ifi.in2000.team45.ui.theme.StarGold
import androidx.compose.runtime.getValue

@Composable
fun StatisticContent(
    state: StatisticUiState,
    favoriteStations: List<FavoriteStation>,
    statisticViewModel: StatisticViewModel,
    savedPlacesViewModel: SavedPlacesViewModel,
    topInset: Dp,
    bottomInset: Dp,
    modifier: Modifier = Modifier
) {
    val listPadding = PaddingValues(
        top = topInset + 16.dp,
        bottom = bottomInset + 16.dp,
        start = 16.dp,
        end = 16.dp
    )

    when (state) {
        is StatisticUiState.Idle -> {
            val history by statisticViewModel.searchHistory.collectAsState()
            val hasContent = favoriteStations.isNotEmpty() || history.isNotEmpty()

            if (hasContent) {
                LazyColumn(modifier = modifier, contentPadding = listPadding) {
                    item { SearchBarHeader(statisticViewModel) }

                    if (favoriteStations.isNotEmpty()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 4.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(end = 4.dp)
                                )
                                Text(
                                    text = stringResource(R.string.favorites),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                                    .padding(vertical = 3.dp)
                                    .clickable { statisticViewModel.selectStation(station) },
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
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = StarGold,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Text(
                                        fav.name,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(12.dp)) }
                    }

                    if (history.isNotEmpty()) {
                        items(history) { query ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        statisticViewModel.updateSearchQuery(query)
                                        statisticViewModel.searchStations()
                                    },
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
                                    Icon(
                                        Icons.Filled.Search,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 12.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = query,
                                        fontSize = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = { statisticViewModel.removeRecentSearch(query) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = stringResource(R.string.remove_recent_search),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = modifier
                        .padding(top = topInset, bottom = bottomInset)
                        .padding(16.dp)
                ) {
                    SearchBarHeader(statisticViewModel)
                    Box(modifier = Modifier.weight(1f)) {
                        IdleContent()
                    }
                }
            }
        }
        is StatisticUiState.Loading -> {
            Column(
                modifier = modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = topInset, bottom = bottomInset)
                    .padding(16.dp)
            ) {
                SearchBarHeader(statisticViewModel)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
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
                        savedPlacesViewModel.removeStation(station.id)
                    } else {
                        savedPlacesViewModel.addStation(station)
                    }
                },
                header = { SearchBarHeader(statisticViewModel) },
                contentPadding = listPadding,
                modifier = modifier
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
                contentPadding = listPadding,
                modifier = modifier,
                header = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_to_search),
                            modifier = Modifier
                                .clickable { statisticViewModel.goBack() }
                                .padding(end = 12.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.station.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (state.station.municipality.isNotBlank()) {
                                Text(
                                    text = "${state.station.municipality}, ${state.station.county}",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = {
                            if (isFav) savedPlacesViewModel.removeStation(state.station.id)
                            else savedPlacesViewModel.addStation(state.station)
                        }) {
                            Icon(
                                imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = if (isFav) stringResource(R.string.remove_favorite) else stringResource(R.string.add_to_favorites),
                                tint = if (isFav) StarGold else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
        is StatisticUiState.Error -> {
            Column(
                modifier = modifier
                    .padding(top = topInset, bottom = bottomInset)
                    .padding(16.dp)
            ) {
                SearchBarHeader(statisticViewModel)
                Box(modifier = Modifier.weight(1f)) {
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
private fun SearchBarHeader(statisticViewModel: StatisticViewModel) {
    SearchBar(
        query = statisticViewModel.searchQuery,
        onQueryChange = { statisticViewModel.updateSearchQuery(it) },
        onSearch = { statisticViewModel.searchStations() }
    )
    Spacer(modifier = Modifier.height(12.dp))
}
