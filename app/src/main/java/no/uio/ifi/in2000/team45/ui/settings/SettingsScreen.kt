package no.uio.ifi.in2000.team45.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onMapClick: () -> Unit,
    onStatisticClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        bottomBar = {
            BottomAppBar{
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton({}) {
                        Icon(Icons.Filled.Settings, contentDescription = "")
                    }
                    IconButton(onClick = onMapClick) {
                        Icon(Icons.Outlined.Place, contentDescription = "to map")
                    }
                    IconButton(onClick = onStatisticClick) {
                        Icon(Icons.Outlined.Info, contentDescription = "to statistics")
                    }
                }
            }
        }
    ) {
        innerPadding ->
        Column(modifier = modifier.padding(innerPadding)) { }
    }
}