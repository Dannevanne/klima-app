package no.uio.ifi.in2000.team45

import io.mockk.coEvery
import org.junit.Test
import org.junit.Assert.*
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.uio.ifi.in2000.team45.data.statistic.ClimateNormal
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
}