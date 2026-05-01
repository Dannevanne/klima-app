package no.uio.ifi.in2000.team45

import android.app.Application
import com.mapbox.search.autocomplete.PlaceAutocomplete
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import no.uio.ifi.in2000.team45.data.statistic.RecentSearchRepository
import no.uio.ifi.in2000.team45.data.statistic.StatisticRepository
import no.uio.ifi.in2000.team45.data.statistic.WeatherStation
import no.uio.ifi.in2000.team45.ui.statistic.StatisticUiState
import no.uio.ifi.in2000.team45.ui.statistic.StatisticViewModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticViewModelTest {

    @Before
    fun setUp() {
        // viewModelScope uses Dispatchers.Main; without this, launch throws.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testSearchStationsShouldSetLoadingState() = runTest {
        val mockApp = mockk<Application>(relaxed = true)
        val mockRepo = mockk<StatisticRepository>()
        val mockRecent = mockk<RecentSearchRepository>()
        val mockPlace = mockk<PlaceAutocomplete>(relaxed = true)

        every { mockRecent.recentSearchesFlow } returns flowOf(emptyList())
        // Hang on the network call so the launched coroutine stays at
        // performSearch's first line (uiState = Loading) when we assert.
        coEvery { mockRepo.searchStations(any(), any()) } coAnswers {
            CompletableDeferred<List<WeatherStation>>().await()
        }

        val vm = StatisticViewModel(mockApp, mockRepo, mockRecent, mockPlace, "id")
        vm.updateSearchQuery("Oslo")
        vm.searchStations()

        assertEquals(StatisticUiState.Loading, vm.uiState)
    }
}