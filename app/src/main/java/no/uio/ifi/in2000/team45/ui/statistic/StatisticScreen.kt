package no.uio.ifi.in2000.team45.ui.statistic

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import no.uio.ifi.in2000.team45.ui.map.MapViewModel


@Composable
fun StatisticScreen(
    statisticViewModel: StatisticViewModel,
    onSettingsClick: () -> Unit,
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        bottomBar = {
            BottomAppBar{
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Outlined.Settings, contentDescription = "to settings")
                }
                IconButton(onClick = onMapClick) {
                    Icon(Icons.Outlined.Place, contentDescription = "")
                }
                Icon(Icons.Filled.Info, contentDescription = "to statistics")
            }
        }
    ) { innerPadding ->

    }

}