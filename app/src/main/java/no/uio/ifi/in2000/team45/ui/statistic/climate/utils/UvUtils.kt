package no.uio.ifi.in2000.team45.ui.statistic.climate.utils

// Estimate peak summer UV index based on latitude.
// Southern Norway (~58N) gets UV ~6-7, northern (~70N) gets ~3-4 in summer.
// Current month scales this down (winter = near zero).
fun estimateUvIndex(latitude: Double): Double {
    val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
    // Peak UV for this latitude (summer solstice)
    val peakUv = (10.0 - (latitude - 50.0) * 0.15).coerceIn(1.0, 11.0)
    // Monthly scaling factor (peaks in June/July)
    val monthFactor = when (month) {
        1, 12 -> 0.05
        2, 11 -> 0.10
        3, 10 -> 0.25
        4, 9 -> 0.50
        5, 8 -> 0.75
        6, 7 -> 1.0
        else -> 0.3
    }
    return (peakUv * monthFactor).coerceIn(0.0, 11.0)
}

fun uvLabel(uv: Double): String = when {
    uv < 3 -> "Lav"
    uv < 6 -> "Moderat"
    uv < 8 -> "Hoy"
    uv < 11 -> "Veldig hoy"
    else -> "Ekstrem"
}