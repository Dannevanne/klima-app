package no.uio.ifi.in2000.team45.ui.statistic.climate.utils

fun formatValue(value: Double?): String {
    if (value == null) return "-"
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format("%.1f", value)
    }
}