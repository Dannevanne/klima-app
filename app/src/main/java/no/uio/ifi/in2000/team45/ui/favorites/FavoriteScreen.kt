package no.uio.ifi.in2000.team45.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.NavBarItem
import no.uio.ifi.in2000.team45.data.favorites.FavoriteLocation
import no.uio.ifi.in2000.team45.data.favorites.FavoriteStation
import no.uio.ifi.in2000.team45.ui.common.AppBottomBar
import no.uio.ifi.in2000.team45.ui.common.AppTopBar
import no.uio.ifi.in2000.team45.ui.common.BottomBarItemData
import no.uio.ifi.in2000.team45.ui.common.OverflowMenu
import no.uio.ifi.in2000.team45.ui.common.OverflowMenuButton
import no.uio.ifi.in2000.team45.ui.favorites.components.FavoriteContent
import no.uio.ifi.in2000.team45.ui.map.MapScreen
import no.uio.ifi.in2000.team45.ui.map.MapTheme
import no.uio.ifi.in2000.team45.ui.map.useDarkThemeFrom
import no.uio.ifi.in2000.team45.ui.theme.StarGold
import no.uio.ifi.in2000.team45.ui.favorites.components.FavoriteLocationCard

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
                navigationIcon = {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 8.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    OverflowMenuButton(onClick = { expanded = true } )

                    OverflowMenu(
                        expanded =  expanded,
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
                        label = stringResource(R.string.nav_statistics),
                        onClick = onStatisticClick,
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.outline_analytics),
                                contentDescription = null
                            )
                        }
                    ),
                    BottomBarItemData(
                        label = stringResource(R.string.saved_places),
                        onClick = {},
                        icon = { Icon(Icons.Filled.Favorite, contentDescription = null) }
                    )
                )
            )
        }
    ) { innerPadding ->
        FavoriteContent(
            stations = stations,
            locations = locations,
            favoriteViewModel = favoriteViewModel,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        )
    }
}
// Color based on temperature
fun tempColor(temp: Double): Color = when {
    temp <= -10 -> Color(0xFFBBDEFB)
    temp <= 0 -> Color(0xFFE3F2FD)
    temp <= 10 -> Color(0xFFC8E6C9)
    temp <= 20 -> Color(0xFFFFF9C4)
    else -> Color(0xFFFFCCBC)
}

// Color based on wind speed
fun windColor(speed: Double): Color = when {
    speed <= 3 -> Color(0xFFE8F5E9)
    speed <= 8 -> Color(0xFFFFF9C4)
    speed <= 15 -> Color(0xFFFFCCBC)
    else -> Color(0xFFEF9A9A)
}