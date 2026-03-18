package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.data.map.MapDatasource

sealed interface MapUiState {
    data class Success(val overlay: String): MapUiState
    object Error : MapUiState
    object Loading : MapUiState
}

class MapViewModel(
    private val mapDatasource: MapDatasource = MapDatasource()
) : ViewModel() {
    var img: String by mutableStateOf("")
        private set

    init {
        getMap()
    }

    fun getMap() {
        viewModelScope.launch {
            try {
                val imgTekst = mapDatasource.getMap()
                img = imgTekst
            }
            catch (e: Exception) {
                throw e
            }
        }
    }
}