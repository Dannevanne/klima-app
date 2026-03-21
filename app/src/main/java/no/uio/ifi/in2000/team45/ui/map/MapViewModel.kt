package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.maps.renderer.widget.Widget
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.data.map.MapDatasource
import no.uio.ifi.in2000.team45.data.map.MapRepository
import okhttp3.internal.http2.Header

sealed interface MapUiState {
    data class Success(val overlayURL: String): MapUiState
    object Error : MapUiState
    object Loading : MapUiState
}

class MapViewModel(private val mapRepository: MapRepository = MapRepository()) : ViewModel(){
    var mapUiState: MapUiState by mutableStateOf(MapUiState.Loading)
        private set

    private var lastOverlayURL: String? = null

    fun updateOverlay(bbox: List<Double>,
                      width: Int,
                      height: Int){
        if(bbox.size != 4 || width <= 0 || height <= 0) return
        try{
            val url = mapRepository.getOverlayURL(bbox = bbox,
                width = width,
                height = height)
            if(url != lastOverlayURL){
                lastOverlayURL = url
                mapUiState = MapUiState.Success(url)
            }
        }
        catch(e: Exception){
            mapUiState = MapUiState.Error
        }
    }
}