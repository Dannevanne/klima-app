package no.uio.ifi.in2000.team45.ui.statistic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.NavBarItem
import no.uio.ifi.in2000.team45.ui.favorites.FavoriteViewModel
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
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
                        onClick = onFavoritesClick,
                        icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null) },
                        label = stringResource(R.string.saved_places),
                        modifier = Modifier.weight(1f)
                    )
                    NavBarItem(
                        onClick = {},
                        icon = { Icon(painterResource(R.drawable.filled_analytics), contentDescription = null) },
                        label = stringResource(R.string.nav_statistics),
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
            val currentState = statisticViewModel.uiState

            if (currentState is StatisticUiState.ClimateData) {
                val isFav = favoriteStations.any { it.stationId == currentState.station.id }
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
                            text = currentState.station.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentState.station.municipality.isNotBlank()) {
                            Text(
                                text = "${currentState.station.municipality}, ${currentState.station.county}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = {
                        if (isFav) favoriteViewModel.removeStation(currentState.station.id)
                        else favoriteViewModel.addStation(currentState.station)
                    }) {
                        Icon(
                            imageVector = if (isFav) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = if (isFav) "Remove favorite" else "Add favorite",
                            tint = if (isFav) StarGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                SearchBar(
                    query = statisticViewModel.searchQuery,
                    onQueryChange = { statisticViewModel.updateSearchQuery(it) },
                    onSearch = { statisticViewModel.searchStations() }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            StatisticContent(
                state = statisticViewModel.uiState,
                favoriteStations = favoriteStations,
                statisticViewModel = statisticViewModel,
                favoriteViewModel = favoriteViewModel
            )
        }
    }
}
