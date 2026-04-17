package no.uio.ifi.in2000.team45.ui.statistic.climate.utils

import no.uio.ifi.in2000.team45.data.statistic.MonthlyClimateStats
import no.uio.ifi.in2000.team45.ui.statistic.climate.ColorScale
import no.uio.ifi.in2000.team45.ui.statistic.climate.TableRow

fun buildTemperatureRows(stats: List<MonthlyClimateStats>): List<TableRow> {
    val rows = mutableListOf<TableRow>()

    if (stats.any { it.meanTemp != null }) {
        rows.add(
            TableRow(
                label = "Snitt",
                unit = "C",
                values = stats.map { formatValue(it.meanTemp) },
                rawValues = stats.map { it.meanTemp },
                colorScale = ColorScale.TEMPERATURE
            )
        )
    }
    if (stats.any { it.maxTemp != null }) {
        rows.add(
            TableRow(
                label = "Maks",
                unit = "C",
                values = stats.map { formatValue(it.maxTemp) },
                rawValues = stats.map { it.maxTemp },
                colorScale = ColorScale.TEMPERATURE
            )
        )
    }
    if (stats.any { it.minTemp != null }) {
        rows.add(
            TableRow(
                label = "Min",
                unit = "C",
                values = stats.map { formatValue(it.minTemp) },
                rawValues = stats.map { it.minTemp },
                colorScale = ColorScale.TEMPERATURE
            )
        )
    }
    if (stats.any { it.recordHigh != null }) {
        rows.add(
            TableRow(
                label = "Rekord H",
                unit = "C",
                values = stats.map { formatValue(it.recordHigh) },
                rawValues = stats.map { it.recordHigh },
                colorScale = ColorScale.TEMPERATURE
            )
        )
    }
    if (stats.any { it.recordLow != null }) {
        rows.add(
            TableRow(
                label = "Rekord L",
                unit = "C",
                values = stats.map { formatValue(it.recordLow) },
                rawValues = stats.map { it.recordLow },
                colorScale = ColorScale.TEMPERATURE
            )
        )
    }
    return rows
}

fun buildPrecipitationRows(stats: List<MonthlyClimateStats>): List<TableRow> {
    return listOf(
        TableRow(
            label = "Nedbor",
            unit = "mm",
            values = stats.map { formatValue(it.precipitation) },
            rawValues = stats.map { it.precipitation },
            colorScale = ColorScale.PRECIPITATION
        )
    )
}

fun buildWindRows(stats: List<MonthlyClimateStats>): List<TableRow> {
    val rows = mutableListOf<TableRow>()

    if (stats.any { it.windSpeed != null }) {
        rows.add(
            TableRow(
                label = "Snitt",
                unit = "m/s",
                values = stats.map { formatValue(it.windSpeed) },
                rawValues = stats.map { it.windSpeed },
                colorScale = ColorScale.WIND
            )
        )
    }
    if (stats.any { it.windGust != null }) {
        rows.add(
            TableRow(
                label = "Kast",
                unit = "m/s",
                values = stats.map { formatValue(it.windGust) },
                rawValues = stats.map { it.windGust },
                colorScale = ColorScale.WIND
            )
        )
    }
    return rows
}

fun buildSnowRows(stats: List<MonthlyClimateStats>): List<TableRow> {
    return listOf(
        TableRow(
            label = "Snitt",
            unit = "cm",
            values = stats.map { formatValue(it.snowDepth) },
            rawValues = stats.map { it.snowDepth },
            colorScale = ColorScale.SNOW
        )
    )
}

fun buildCloudRows(stats: List<MonthlyClimateStats>): List<TableRow> {
    return listOf(
        TableRow(
            label = "Snitt",
            unit = "%",
            values = stats.map { formatValue(it.cloudCover) },
            rawValues = stats.map { it.cloudCover },
            colorScale = ColorScale.NONE
        )
    )
}