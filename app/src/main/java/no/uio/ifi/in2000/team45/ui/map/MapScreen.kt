package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomAppBar
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.request.ImageRequest
import com.mapbox.geojson.BoundingBox
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.generated.imageSource

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
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,

                    ) {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Outlined.Settings, contentDescription = "to settings")
                        }
                        IconButton({}) {
                            Icon(Icons.Filled.Place, contentDescription = "")
                        }
                        IconButton(onClick = onStatisticClick) {
                            Icon(Icons.Outlined.Info, contentDescription = "to statistics")
                        }
                    }
                    }
            }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ){
            DataMap(mapViewModel = mapViewModel, modifier = Modifier.fillMaxSize())
            if(uiState is MapUiState.Error){
                Text("Error", color = Color.Red)
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
                center(Point.fromLngLat(15.0, 65.0))
                zoom(3.75)
                pitch(0.0)
                bearing(0.0)
            }
        }){}
}

@Composable
fun MapOverlay(
    overlay: String
) {
    val southwest = doubleArrayOf(59.93895,10.69977)
    val northeast = doubleArrayOf(59.94470,10.73090)

        AsyncImage(
            model = ImageRequest.Builder(context = LocalContext.current).data(overlay)
                .crossfade(true).build(),
            contentDescription = ""
        )
}

@Preview
@Composable
fun MapScreenPreview() {
    MapScreen(
        mapViewModel = viewModel(),
        {},
        {}
    )
}