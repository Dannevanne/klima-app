package no.uio.ifi.in2000.team45.ui.statistic.climate


data class TrendInfo(
    val direction: TrendDirection,
    val percentChange: Double?
)

enum class TrendDirection { UP, DOWN, STABLE }