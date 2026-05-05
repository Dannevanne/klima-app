package no.uio.ifi.in2000.team45.ui.statistic.climate

fun vsNormal(value: Double?, normal: Double?): TrendInfo {
    if (value == null || normal == null || normal <= 0.0) {
        return TrendInfo(TrendDirection.STABLE, null)
    }
    val change = ((value - normal) / normal) * 100.0
    val direction = when {
        change > 3.0 -> TrendDirection.UP
        change < -3.0 -> TrendDirection.DOWN
        else -> TrendDirection.STABLE
    }
    return TrendInfo(direction, change)
}