package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage


@Composable
fun MapScreen(
    mapViewModel: MapViewModel,
    onSettingsClick: () -> Unit,
    onStatisticClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val uiState = mapViewModel.mapUiState
    Scaffold(
        bottomBar = {
                BottomAppBar{
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Outlined.Settings, contentDescription = "to settings")
                        }
                        Icon(Icons.Filled.Place, contentDescription = "")
                        IconButton(onClick = onStatisticClick) {
                            Icon(Icons.Outlined.Info, contentDescription = "to statistics")
                        }
                    }
            }
    ) { innerPadding ->
        Box(
            modifier = modifier.padding(innerPadding),
            contentAlignment = Alignment.Center
        ){
            when (uiState) {
                is MapUiState.Error -> Text("Error", color = Color.Red)
                is MapUiState.Loading -> CircularProgressIndicator()
                is MapUiState.Success -> DataMap(
                    modifier = modifier,
                    mapViewModel = mapViewModel
                )
            }
        }
    }
}

@Composable
fun DataMap(
    mapViewModel: MapViewModel,
    modifier: Modifier = Modifier
) {

}