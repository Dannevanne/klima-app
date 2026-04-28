package no.uio.ifi.in2000.team45.ui.statistic.climate


fun computeTrend(values: List<Double>): TrendInfo {
    if (values.size < 4) return TrendInfo(TrendDirection.STABLE, null)
    val mid = values.size / 2
    val firstHalf = values.subList(0, mid).average()
    val secondHalf = values.subList(mid, values.size).average()

    if (firstHalf == 0.0) return TrendInfo(TrendDirection.STABLE, null)

    val change = ((secondHalf - firstHalf) / firstHalf) * 100.0
    val direction = when {
        change > 3.0 -> TrendDirection.UP
        change < -3.0 -> TrendDirection.DOWN
        else -> TrendDirection.STABLE
    }
    return TrendInfo(direction, change)
}

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