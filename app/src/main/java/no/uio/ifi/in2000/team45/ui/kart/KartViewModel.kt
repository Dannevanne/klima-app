package no.uio.ifi.in2000.team45.ui.kart

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.data.kart.KartDatasource

class KartViewModel(
    private val kartDatasource: KartDatasource = KartDatasource()
) : ViewModel() {
    var img: String by mutableStateOf("")
        private set

    init {
        getKart()
    }

    fun getKart() {
        viewModelScope.launch {
            try {
                val imgTekst = kartDatasource.getKart()
                img = imgTekst
            }
            catch (e: Exception) {
                throw e
            }
        }
    }
}