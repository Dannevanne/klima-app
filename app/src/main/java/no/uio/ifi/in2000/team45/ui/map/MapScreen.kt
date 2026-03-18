package no.uio.ifi.in2000.team45.ui.map

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
import androidx.compose.foundation.layout.fillMaxSize
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.animation.viewport.MapViewportState
import com.mapbox.maps.extension.compose.style.MapStyle
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.BottomAppBar
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator

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
            modifier = modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ){
            when (uiState) {
                is MapUiState.Error -> Text("Error", color = Color.Red)
                is MapUiState.Loading -> CircularProgressIndicator()
                is MapUiState.Success -> DataMap(
                    modifier = Modifier.fillMaxSize(),
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
    MapboxMap(
        modifier = modifier.fillMaxSize(),
        mapViewportState = rememberMapViewportState {
            setCameraOptions {
                center(Point.fromLngLat(10.75, 59.91))
                zoom(9.0)
            }
        }
    ) {
    }
}