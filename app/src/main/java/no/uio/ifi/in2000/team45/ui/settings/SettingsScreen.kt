package no.uio.ifi.in2000.team45.ui.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SettingsScreen(
    onMapClick: () -> Unit,
    onStatisticClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        bottomBar = {
            BottomAppBar{
                Icon(Icons.Filled.Settings, contentDescription = "")
                IconButton(onClick = onMapClick) {
                    Icon(Icons.Outlined.Place, contentDescription = "to map")
                }
                IconButton(onClick = onStatisticClick) {
                    Icon(Icons.Outlined.Info, contentDescription = "to statistics")
                }
            }
        }
    ) {

    }
}