package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.data.map.MapDatasource
import no.uio.ifi.in2000.team45.data.map.MapRepository

sealed interface MapUiState {
    data class Success(val overlay: String): MapUiState
    object Error : MapUiState
    object Loading : MapUiState
}

class MapViewModel(
    private val mapRepository: MapRepository = MapRepository()
) : ViewModel() {
    var mapUiState: MapUiState by mutableStateOf(MapUiState.Loading)
        private set

    init {
        getMap()
    }

    fun getMap() {
        viewModelScope.launch {
            try {
                val imgTekst = mapRepository.getOverlay()
                mapUiState = MapUiState.Success(imgTekst)
            }
            catch (e: Exception) {
                mapUiState = MapUiState.Error
            }
        }
    }
}