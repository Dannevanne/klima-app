package no.uio.ifi.in2000.team45.ui.statistic.climate.utils

import androidx.compose.ui.graphics.Color
import no.uio.ifi.in2000.team45.ui.statistic.climate.ColorScale

// Blue (cold) -> white (mild) -> red (hot)
fun temperatureColor(value: Double?): Color {
    if (value == null) return Color.Transparent
    return when {
        value <= -15 -> Color(0xFFBBDEFB)
        value <= -5 -> Color(0xFFE3F2FD)
        value <= 5 -> Color(0xFFF5F5F5)
        value <= 15 -> Color(0xFFFFF9C4)
        value <= 25 -> Color(0xFFFFCCBC)
        else -> Color(0xFFEF9A9A)
    }
}

// Green (dry) -> blue (wet)
fun precipitationColor(value: Double?): Color {
    if (value == null) return Color.Transparent
    return when {
        value <= 30 -> Color(0xFFE8F5E9)
        value <= 80 -> Color(0xFFC8E6C9)
        value <= 150 -> Color(0xFFBBDEFB)
        value <= 250 -> Color(0xFF90CAF9)
        else -> Color(0xFF64B5F6)
    }
}

// Green (calm) -> red (strong)
fun windColor(value: Double?): Color {
    if (value == null) return Color.Transparent
    return when {
        value <= 2 -> Color(0xFFE8F5E9)
        value <= 5 -> Color(0xFFC8E6C9)
        value <= 8 -> Color(0xFFFFF9C4)
        value <= 12 -> Color(0xFFFFCCBC)
        else -> Color(0xFFEF9A9A)
    }
}

// White (no snow) -> light blue (deep snow)
fun snowColor(value: Double?): Color {
    if (value == null) return Color.Transparent
    return when {
        value <= 1 -> Color(0xFFF5F5F5)
        value <= 10 -> Color(0xFFE3F2FD)
        value <= 30 -> Color(0xFFBBDEFB)
        value <= 60 -> Color(0xFF90CAF9)
        else -> Color(0xFF64B5F6)
    }
}

fun cellColor(value: Double?, scale: ColorScale): Color {
    return when (scale) {
        ColorScale.TEMPERATURE -> temperatureColor(value)
        ColorScale.PRECIPITATION -> precipitationColor(value)
        ColorScale.WIND -> windColor(value)
        ColorScale.SNOW -> snowColor(value)
        ColorScale.NONE -> Color.Transparent
    }
}