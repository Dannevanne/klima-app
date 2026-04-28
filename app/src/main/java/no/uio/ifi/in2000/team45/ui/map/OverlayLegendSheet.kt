package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                    boldPrefix = true
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
    boldPrefix: Boolean
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
        items.forEach { item ->
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "•  ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
}

@Composable
private fun legendCaveat(overlay: OverlayType): String? = when (overlay) {
    OverlayType.FLOOD -> stringResource(R.string.legend_flood_caveat)
    OverlayType.RADON -> stringResource(R.string.legend_radon_caveat)
    OverlayType.LANDSLIDE -> stringResource(R.string.legend_landslide_caveat)
    OverlayType.WIND -> stringResource(R.string.legend_wind_caveat)
    else -> null
}

@Composable
private fun legendSource(overlay: OverlayType): String? = when (overlay) {
    OverlayType.FLOOD -> stringResource(R.string.legend_flood_src)
    OverlayType.QUICK_CLAY -> stringResource(R.string.legend_quickclay_src)
    OverlayType.RADON -> stringResource(R.string.legend_radon_src)
    OverlayType.LANDSLIDE -> stringResource(R.string.legend_landslide_src)
    OverlayType.WIND -> stringResource(R.string.legend_wind_src)
    OverlayType.NONE -> null
}
