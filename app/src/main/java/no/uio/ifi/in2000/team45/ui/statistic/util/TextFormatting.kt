package no.uio.ifi.in2000.team45.ui.statistic.util

// Frost station and place fields come back ALL CAPS. Anywhere the user reads
// them on screen, run through this so "OSLO - BLINDERN" reads as
// "Oslo - Blindern" and "TROMS OG FINNMARK" as "Troms Og Finnmark".
fun String.toTitleCaseWords(): String {
    if (isBlank()) return this
    return split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }
}
