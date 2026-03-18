package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.data.map.MapDatasource

sealed interface MapUiState {
    object Success : MapUiState
    object Error : MapUiState
    object Loading : MapUiState
}

class MapViewModel : ViewModel(){
    var mapUiState: MapUiState by mutableStateOf<MapUiState>(MapUiState.Success)
}