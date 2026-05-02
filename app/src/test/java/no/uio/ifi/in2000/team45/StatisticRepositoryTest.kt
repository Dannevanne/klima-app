package no.uio.ifi.in2000.team45

import io.mockk.coEvery
import org.junit.Test
import org.junit.Assert.*
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.uio.ifi.in2000.team45.data.statistic.ClimateNormal
import no.uio.ifi.in2000.team45.data.statistic.MonthlyObservation
import no.uio.ifi.in2000.team45.data.statistic.StatisticDatasource
import no.uio.ifi.in2000.team45.data.statistic.StatisticRepository

class StatisticRepositoryTest {

    @Test
    fun testGetClimateStatsShouldUseNormalsIfAreThere() = runTest {
        val mockDatasource = mockk<StatisticDatasource>()
        val mockNormals = listOf(ClimateNormal(elementId = "mean_air_temperature", month = 1, normal = 5.0))

        // Since it testing suspend function, we need coEvery (every for normal funcs),
        // also why we need runTest here, I am pretty sure.
        coEvery { mockDatasource.getClimateNormals(any(), any())} returns mockNormals
        coEvery { mockDatasource.getMonthlyObservations(any(), any(), any(), any())} returns emptyList()

        val repository = StatisticRepository(mockDatasource)
        val result = repository.getClimateStats("id", "client")

        // Checks if normals is used, since we have set with coEvery
        // that if observations is called upon, it would return an emptylist()
        assertEquals(12, result.size)
    }

    // Reproduces what the VM expects when Sola is assigned only gust for
    // Stavanger. Repository should return per-year AnnualObservations with
    // maxWindGust populated, no other fields, no daily fetch attempted.
    @Test
    fun gustOnlyAssignmentReturnsAnnualWithGustFilled() = runTest {
        val ds = mockk<StatisticDatasource>()
        val gust = "max(wind_speed_of_gust P1M)"
        // Two years of monthly gust data: 2023, 2024
        val monthly = listOf(
            MonthlyObservation(gust, 202301, 25.5),
            MonthlyObservation(gust, 202307, 31.2),
            MonthlyObservation(gust, 202402, 28.0),
            MonthlyObservation(gust, 202412, 33.8)
        )
        coEvery { ds.getMonthlyWindAndSnow("SN44560", "c", 1976, 2025, setOf(gust)) } returns monthly

        val repo = StatisticRepository(ds)
        val result = repo.getAnnualObservations(
            "SN44560", "c", 1976, 2025, elements = setOf(gust)
        )

        assertEquals(2, result.size)
        val y2023 = result.first { it.year == 2023 }
        val y2024 = result.first { it.year == 2024 }
        assertEquals(31.2, y2023.maxWindGust!!, 0.01)
        assertEquals(33.8, y2024.maxWindGust!!, 0.01)
        assertNull(y2023.meanTemp)
        assertNull(y2023.totalPrecip)
        assertNull(y2024.heavyPrecipDays)
    }
}