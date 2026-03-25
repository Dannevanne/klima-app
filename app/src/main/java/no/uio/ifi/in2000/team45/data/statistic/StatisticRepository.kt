package no.uio.ifi.in2000.team45.data.statistic

class StatisticRepository(
    val statisticDatasource: StatisticDatasource = StatisticDatasource()
) {

    suspend fun getStatistic(): String {
        return statisticDatasource.getStatistic()
    }
}