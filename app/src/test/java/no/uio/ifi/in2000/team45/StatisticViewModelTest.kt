package no.uio.ifi.in2000.team45

import android.app.Application
import io.mockk.every
import no.uio.ifi.in2000.team45.ui.statistic.StatisticUiState
import no.uio.ifi.in2000.team45.ui.statistic.StatisticViewModel
import org.junit.Test
import org.junit.Assert.*
import io.mockk.mockk

class StatisticViewModelTest {

    @Test
    fun testSearchStationsShouldSetLoadingState() {
        val mockApplication = mockk<Application>()
        every { mockApplication.getString(any())} returns "mock_client_id"

        val viewModel = StatisticViewModel(mockApplication)
        viewModel.updateSearchQuery("Oslo")
        viewModel.searchStations()

        // Checks that searchStations sets the uiState to Loading
        assertEquals(StatisticUiState.Loading, viewModel.uiState)
    }
}