package no.uio.ifi.in2000.team45.ui.statistic.climate

enum class ColorScale { TEMPERATURE, PRECIPITATION, WIND, SNOW, NONE }

data class TableRow(
    val label: String,
    val unit: String,
    val values: List<String>,
    val rawValues: List<Double?> = emptyList(),
    val colorScale: ColorScale = ColorScale.NONE
)

data class TrendInfo(
    val direction: TrendDirection,
    val percentChange: Double?
)

enum class TrendDirection { UP, DOWN, STABLE }