package no.uio.ifi.in2000.team45.ui.statistic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.NavBarItem
import no.uio.ifi.in2000.team45.ui.savedplaces.SavedPlacesViewModel
import androidx.compose.material.icons.outlined.FavoriteBorder
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticScreen(
    modifier: Modifier = Modifier,
    statisticViewModel: StatisticViewModel,
    onSettingsClick: () -> Unit,
    onInfoClick: () -> Unit,
    onHelpClick: () -> Unit,
    onMapClick: () -> Unit,
    onSavedPlacesClick: () -> Unit,
    onShowOnMap: ((lat: Double, lon: Double) -> Unit)? = null,
    savedPlacesViewModel: SavedPlacesViewModel,
) {
    val favoriteStations by savedPlacesViewModel.stations.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            statisticViewModel.cancelStatisticScreenWork()
        }
    }

    var menuExpanded by remember { mutableStateOf(false) }
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
                    OverflowMenuButton(onClick = { menuExpanded = true } )

                    OverflowMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        onSettingsClick = {
                            menuExpanded = false
                            onSettingsClick()
                        },
                        onInfoClick = {
                            menuExpanded = false
                            onInfoClick()
                        },
                        onHelpClick = {
                            menuExpanded = false
                            onHelpClick()
                        }
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
                        onClick = onSavedPlacesClick,
                        icon = { Icon(Icons.Outlined.FavoriteBorder, contentDescription = null) },
                        label = stringResource(R.string.saved_places),
                        modifier = Modifier.weight(1f)
                    )
                    NavBarItem(
                        onClick = { statisticViewModel.resetAllStatisticData() },
                        icon = { Icon(painterResource(R.drawable.filled_analytics), contentDescription = null) },
                        label = stringResource(R.string.nav_statistics),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { innerPadding ->
        StatisticContent(
            state = statisticViewModel.uiState,
            favoriteStations = favoriteStations,
            statisticViewModel = statisticViewModel,
            savedPlacesViewModel = savedPlacesViewModel,
            topInset = innerPadding.calculateTopPadding(),
            bottomInset = innerPadding.calculateBottomPadding(),
            onShowOnMap = onShowOnMap,
            modifier = modifier.fillMaxSize()
        )
    }
}
