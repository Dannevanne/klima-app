package no.uio.ifi.in2000.team45.ui.favorites

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.common.AppBottomBar
import no.uio.ifi.in2000.team45.ui.common.AppTopBar
import no.uio.ifi.in2000.team45.ui.common.BottomBarItemData
import no.uio.ifi.in2000.team45.ui.common.OverflowMenu
import no.uio.ifi.in2000.team45.ui.common.OverflowMenuButton
import no.uio.ifi.in2000.team45.ui.favorites.components.FavoriteContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    favoriteViewModel: FavoriteViewModel,
    onSettingsClick: () -> Unit,
    onInfoClick: () -> Unit,
    onHelpClick: () -> Unit,
    onMapClick: () -> Unit,
    onStatisticClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stations by favoriteViewModel.stations.collectAsState()
    val locations by favoriteViewModel.locations.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.saved_places),
                titleFontSize = 24.sp,
                navigationIcon = {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    OverflowMenuButton(onClick = { expanded = true })

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
            AppBottomBar(
                items = listOf(
                    BottomBarItemData(
                        label = stringResource(R.string.nav_map),
                        onClick = onMapClick,
                        icon = { Icon(Icons.Outlined.Place, contentDescription = null) }
                    ),
                    BottomBarItemData(
                        label = stringResource(R.string.saved_places),
                        onClick = {},
                        icon = { Icon(Icons.Filled.Favorite, contentDescription = null) }
                    ),
                    BottomBarItemData(
                        label = stringResource(R.string.nav_statistics),
                        onClick = onStatisticClick,
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.outline_analytics),
                                contentDescription = null
                            )
                        }
                    )
                )
            )
        }
    ) { innerPadding ->
        FavoriteContent(
            stations = stations,
            locations = locations,
            favoriteViewModel = favoriteViewModel,
            onMapClick = onMapClick,
            modifier = modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(16.dp)
        )
    }
}
