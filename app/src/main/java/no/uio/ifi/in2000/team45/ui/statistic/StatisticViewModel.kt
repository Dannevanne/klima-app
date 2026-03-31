package no.uio.ifi.in2000.team45.ui.statistic

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.statistic.MonthlyClimateStats
import no.uio.ifi.in2000.team45.data.statistic.StatisticRepository
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation

sealed interface StatisticUiState {
    object Idle : StatisticUiState
    object Loading : StatisticUiState
    data class StationResults(val stations: List<WeatherStation>) : StatisticUiState
    data class ClimateData(
        val station: WeatherStation,
        val stats: List<MonthlyClimateStats>
    ) : StatisticUiState
    data class Error(val message: String) : StatisticUiState
}

class StatisticViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = StatisticRepository()
    private val clientId: String = application.getString(R.string.frost_client_id)

    var uiState: StatisticUiState by mutableStateOf(StatisticUiState.Idle)
        private set

    var searchQuery: String by mutableStateOf("")
        private set

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun searchStations() {
        // Removed this logic in order to complete TDD for this function
    }

    fun selectStation(station: WeatherStation) {
        uiState = StatisticUiState.Loading

        viewModelScope.launch {
            try {
                val stats = repository.getClimateStats(station.id, clientId)
                uiState = if (stats.all { it.meanTemp == null && it.precipitation == null }) {
                    StatisticUiState.Error("Ingen klimadata tilgjengelig for ${station.name}")
                } else {
                    StatisticUiState.ClimateData(station, stats)
                }
            } catch (e: Exception) {
                Log.e("StatisticVM", "Climate data failed", e)
                uiState = StatisticUiState.Error("Feil ved henting av data: ${e.message}")
            }
        }
    }

    fun goBack() {
        uiState = StatisticUiState.Idle
    }
}
