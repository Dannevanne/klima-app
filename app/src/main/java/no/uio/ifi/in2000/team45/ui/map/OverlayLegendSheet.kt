package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.map.OverlayType
import no.uio.ifi.in2000.team45.ui.theme.IconBlue
import no.uio.ifi.in2000.team45.ui.theme.IconPurple
import no.uio.ifi.in2000.team45.ui.theme.StatisticsGray
import no.uio.ifi.in2000.team45.ui.theme.StatisticsGreen
import no.uio.ifi.in2000.team45.ui.theme.StatisticsOrange
import no.uio.ifi.in2000.team45.ui.theme.StatisticsRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayLegendSheet(
    overlay: OverlayType,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val title = stringResource(overlay.labelRes)
    val whatShows = legendWhatShows(overlay)
    val categories = legendCategories(overlay)
    val caveat = legendCaveat(overlay)
    val source = legendSource(overlay)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = overlayEmoji(overlay), fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.overlay_legend_title, title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (whatShows != null) {
                LegendSection(
                    icon = "🔍",
                    heading = stringResource(R.string.legend_what_shows),
                    items = listOf(whatShows),
                    boldPrefix = false
                )
            }

            if (categories.isNotEmpty()) {
                LegendSection(
                    icon = "🎨",
                    heading = stringResource(R.string.legend_categories),
                    items = categories,
                    boldPrefix = true,
                    bulletColors = legendCategoryColors(overlay)
                )
            }

            if (caveat != null) {
                LegendSection(
                    icon = "⚠️",
                    heading = stringResource(R.string.legend_caveat),
                    items = listOf(caveat),
                    boldPrefix = false
                )
            }

            if (source != null) {
                LegendSection(
                    icon = "📚",
                    heading = stringResource(R.string.legend_source),
                    items = listOf(source),
                    boldPrefix = false
                )
            }
        }
    }
}

@Composable
private fun LegendSection(
    icon: String,
    heading: String,
    items: List<String>,
    boldPrefix: Boolean,
    bulletColors: List<Color?>? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = heading.uppercase(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.Top) {
                val swatch = bulletColors?.getOrNull(index)
                if (swatch != null) {
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp, end = 8.dp)
                            .size(12.dp)
                            .background(swatch, CircleShape)
                    )
                } else {
                    Text(
                        text = "•  ",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = if (boldPrefix) bulletAnnotated(item) else AnnotatedString(item),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun bulletAnnotated(text: String): AnnotatedString = buildAnnotatedString {
    val emDash = " — "
    val idx = text.indexOf(emDash)
    if (idx >= 0) {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(text.substring(0, idx))
        }
        append(text.substring(idx))
    } else {
        append(text)
    }
}

@Composable
private fun legendWhatShows(overlay: OverlayType): String? = when (overlay) {
    OverlayType.NONE -> stringResource(R.string.legend_none_what)
    OverlayType.FLOOD -> stringResource(R.string.legend_flood_what)
    OverlayType.QUICK_CLAY -> stringResource(R.string.legend_quickclay_what)
    OverlayType.RADON -> stringResource(R.string.legend_radon_what)
    OverlayType.LANDSLIDE -> stringResource(R.string.legend_landslide_what)
    OverlayType.WIND -> stringResource(R.string.legend_wind_what)
    OverlayType.AVALANCHE -> stringResource(R.string.legend_avalanche_what)
}

// Swatch colors for the categories list, lined up by index with the strings
// returned from `legendCategories`. Null means "no swatch, fall back to a
// plain bullet" (used for instructional items and the wind gradient).
private val LegendYellow = Color(0xFFFCD34D)

private fun legendCategoryColors(overlay: OverlayType): List<Color?> = when (overlay) {
    OverlayType.NONE -> listOf(null)
    OverlayType.FLOOD -> listOf(IconBlue)
    OverlayType.QUICK_CLAY -> listOf(
        LegendYellow,
        StatisticsOrange,
        StatisticsRed,
        StatisticsGreen,
        StatisticsGray
    )
    OverlayType.RADON -> listOf(
        IconPurple,
        StatisticsRed,
        LegendYellow,
        StatisticsGray
    )
    OverlayType.LANDSLIDE -> listOf(StatisticsOrange)
    OverlayType.WIND -> listOf(null)
}

@Composable
private fun legendCategories(overlay: OverlayType): List<String> = when (overlay) {
    OverlayType.NONE -> listOf(stringResource(R.string.legend_none_cat_pick))
    OverlayType.FLOOD -> listOf(stringResource(R.string.legend_flood_cat_polygon))
    OverlayType.QUICK_CLAY -> listOf(
        stringResource(R.string.legend_quickclay_cat_lav),
        stringResource(R.string.legend_quickclay_cat_middels),
        stringResource(R.string.legend_quickclay_cat_hoy),
        stringResource(R.string.legend_quickclay_cat_safe),
        stringResource(R.string.legend_quickclay_cat_other)
    )
    OverlayType.RADON -> listOf(
        stringResource(R.string.legend_radon_cat_purple),
        stringResource(R.string.legend_radon_cat_red),
        stringResource(R.string.legend_radon_cat_yellow),
        stringResource(R.string.legend_radon_cat_grey)
    )
    OverlayType.LANDSLIDE -> listOf(stringResource(R.string.legend_landslide_cat_polygon))
    OverlayType.WIND -> listOf(stringResource(R.string.legend_wind_cat_scale))
    OverlayType.AVALANCHE -> listOf(stringResource(R.string.legend_avalanche_cat_polygon))
}

@Composable
private fun legendCaveat(overlay: OverlayType): String? = when (overlay) {
    OverlayType.FLOOD -> stringResource(R.string.legend_flood_caveat)
    OverlayType.RADON -> stringResource(R.string.legend_radon_caveat)
    OverlayType.LANDSLIDE -> stringResource(R.string.legend_landslide_caveat)
    OverlayType.WIND -> stringResource(R.string.legend_wind_caveat)
    OverlayType.AVALANCHE -> stringResource(R.string.legend_avalanche_caveat)
    else -> null
}

@Composable
private fun legendSource(overlay: OverlayType): String? = when (overlay) {
    OverlayType.FLOOD -> stringResource(R.string.legend_flood_src)
    OverlayType.QUICK_CLAY -> stringResource(R.string.legend_quickclay_src)
    OverlayType.RADON -> stringResource(R.string.legend_radon_src)
    OverlayType.LANDSLIDE -> stringResource(R.string.legend_landslide_src)
    OverlayType.WIND -> stringResource(R.string.legend_wind_src)
    OverlayType.AVALANCHE -> stringResource(R.string.legend_avalanche_src)
    OverlayType.NONE -> null
}
