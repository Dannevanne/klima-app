package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun MapScreen(
    mapViewModel: MapViewModel,
    onSettingsClick: () -> Unit,
    onStatisticsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        bottomBar = {
                BottomAppBar{
                        Icon(Icons.Filled.Place, contentDescription = "")
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Outlined.Settings, contentDescription = "to settings")
                        }
                        IconButton(onClick = onStatisticsClick) {
                            Icon(Icons.Outlined.Info, contentDescription = "to statistics")
                        }
                    }
            }
    ) {

    }
}