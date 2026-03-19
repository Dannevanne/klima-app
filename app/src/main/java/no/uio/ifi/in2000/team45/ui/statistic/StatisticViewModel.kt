package no.uio.ifi.in2000.team45.ui.statistic

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.data.statistic.StatisticRepository

class StatisticViewModel(
    private val statisticRepository: StatisticRepository = StatisticRepository()
) : ViewModel(){

    var statistic: String by mutableStateOf("")
        private set

    fun getStatistic() {
        viewModelScope.launch {
            try {
                statistic = statisticRepository.getStatistic()
            }
            catch (e: Exception) {
                Log.e("StatisticVM", "Couldn't get statistics", e)
                throw e
            }
        }
    }
}