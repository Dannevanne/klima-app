package no.uio.ifi.in2000.team45.ui.savedplaces.components

import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.theme.LinkBlue
import no.uio.ifi.in2000.team45.ui.theme.StatisticsGray
import no.uio.ifi.in2000.team45.ui.theme.StatisticsGreen
import no.uio.ifi.in2000.team45.ui.theme.StatisticsOrange
import no.uio.ifi.in2000.team45.ui.theme.StatisticsRed
import androidx.core.net.toUri

private val RiskHigh = StatisticsRed
private val RiskModerate = StatisticsOrange
private val RiskLow = StatisticsGreen
private val RiskUnknown = StatisticsGray

data class AdviceCategory(
    val title: String,
    val dotColor: Color,
    val bullets: List<String>,
    val linkText: String,
    val linkUrl: String
)

private fun riskColor(level: String): Color = when (level) {
    "High" -> RiskHigh
    "Moderate" -> RiskModerate
    "Low" -> RiskLow
    else -> RiskUnknown
}

@Composable
private fun floodAdvice(level: String): AdviceCategory {
    val bullets = when (level) {
        "High" -> listOf(
            stringResource(R.string.flood_advice_backflow_valve),
            stringResource(R.string.flood_advice_raise_threshold),
            stringResource(R.string.flood_advice_drainage_direction),
            stringResource(R.string.flood_advice_insurance)
        )
        "Moderate" -> listOf(
            stringResource(R.string.flood_advice_drainage_direction),
            stringResource(R.string.flood_advice_insurance),
            stringResource(R.string.flood_advice_gutters)
        )
        else -> listOf(
            stringResource(R.string.flood_advice_gutters),
            stringResource(R.string.flood_advice_evacuation_routes)
        )
    }
    return AdviceCategory(
        title = stringResource(R.string.flood_measures),
        dotColor = riskColor(level),
        bullets = bullets,
        linkText = stringResource(R.string.nve_flood_link),
        linkUrl = "https://www.dibk.no/saksbehandling-tilsyn-og-kontroll/temaveiledning-utbygging-i-fareomrader/3.-flom#34-sikringstiltak"
    )
}

@Composable
private fun landslideAdvice(level: String): AdviceCategory {
    val bullets = when (level) {
        "High" -> listOf(
            stringResource(R.string.landslide_advice_hazard_maps_high),
            stringResource(R.string.landslide_advice_geotechnical),
            stringResource(R.string.landslide_advice_slopes_vegetation),
            stringResource(R.string.landslide_advice_evacuation_plan)
        )
        "Moderate" -> listOf(
            stringResource(R.string.landslide_advice_hazard_maps_moderate),
            stringResource(R.string.landslide_advice_slopes_vegetation),
            stringResource(R.string.landslide_advice_monitor_cracks)
        )
        else -> listOf(
            stringResource(R.string.landslide_advice_remove_vegetation),
            stringResource(R.string.landslide_advice_nve_warnings)
        )
    }
    return AdviceCategory(
        title = stringResource(R.string.landslide_prevention),
        dotColor = riskColor(level),
        bullets = bullets,
        linkText = stringResource(R.string.nve_landslide_link),
        linkUrl = "https://www.dibk.no/saksbehandling-tilsyn-og-kontroll/temaveiledning-utbygging-i-fareomrader/5.-skred#54-sikringstiltak"
    )
}

// Quick clay is a type of landslide, so its advice reuses the landslide
// bullets. The DiBK skred page covers both. Kept as its own category so the
// user sees a dot matching their quick clay risk level alongside the other
// hazard rows.
@Composable
private fun quickClayAdvice(level: String): AdviceCategory {
    val bullets = when (level) {
        "High" -> listOf(
            stringResource(R.string.landslide_advice_hazard_maps_high),
            stringResource(R.string.landslide_advice_geotechnical),
            stringResource(R.string.landslide_advice_slopes_vegetation),
            stringResource(R.string.landslide_advice_evacuation_plan)
        )
        "Moderate" -> listOf(
            stringResource(R.string.landslide_advice_hazard_maps_moderate),
            stringResource(R.string.landslide_advice_slopes_vegetation),
            stringResource(R.string.landslide_advice_monitor_cracks)
        )
        else -> listOf(
            stringResource(R.string.landslide_advice_remove_vegetation),
            stringResource(R.string.landslide_advice_nve_warnings)
        )
    }
    return AdviceCategory(
        title = stringResource(R.string.quick_clay_precautions),
        dotColor = riskColor(level),
        bullets = bullets,
        linkText = stringResource(R.string.nve_landslide_link),
        linkUrl = "https://www.dibk.no/saksbehandling-tilsyn-og-kontroll/temaveiledning-utbygging-i-fareomrader/5.-skred#54-sikringstiltak"
    )
}

@Composable
private fun windAdvice(level: String): AdviceCategory {
    val bullets = when (level) {
        "High" -> listOf(
            stringResource(R.string.wind_advice_secure_loose),
            stringResource(R.string.wind_advice_inspect_roof),
            stringResource(R.string.wind_advice_trim_trees),
            stringResource(R.string.wind_advice_fencing)
        )
        "Moderate" -> listOf(
            stringResource(R.string.wind_advice_secure_loose),
            stringResource(R.string.wind_advice_inspect_roof),
            stringResource(R.string.wind_advice_trim_trees)
        )
        else -> listOf(
            stringResource(R.string.wind_advice_inspect_roof),
            stringResource(R.string.wind_advice_garden_furniture)
        )
    }
    return AdviceCategory(
        title = stringResource(R.string.wind_protection),
        dotColor = riskColor(level),
        bullets = bullets,
        linkText = stringResource(R.string.dsb_storm_link),
        linkUrl = "https://www.dsb.no/sikkerhverdag/produkter-utstyr-og-fritid/slik-forbereder-du-deg-pa-storm/"
    )
}

@Composable
fun adviceForRiskLevels(
    floodLevel: String,
    landslideLevel: String,
    quickClayLevel: String,
    windLevel: String
): List<AdviceCategory> = listOf(
    floodAdvice(floodLevel),
    landslideAdvice(landslideLevel),
    quickClayAdvice(quickClayLevel),
    windAdvice(windLevel)
)

@Composable
fun WhatCanIDoSection(
    floodLevel: String,
    landslideLevel: String,
    quickClayLevel: String,
    windLevel: String,
    savedPlacesCount: Int
) {
    var expandedIndex by remember { mutableStateOf<Int?>(null) }
    var adviceExpanded by remember { mutableStateOf(false) }
    val categories = adviceForRiskLevels(floodLevel, landslideLevel, quickClayLevel, windLevel)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.what_can_i_do),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            modifier = Modifier.weight(1f)
        )

        if (savedPlacesCount > 2) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = if (adviceExpanded) {
                    stringResource(R.string.collapse)
                } else {
                    stringResource(R.string.expand)
                       },
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .rotate(if (adviceExpanded) 180f else 0f)
                    .clickable { adviceExpanded = !adviceExpanded }
            )
        }
    }

    val showAdvice = savedPlacesCount <= 2 || adviceExpanded

    if (showAdvice) {
        categories.forEachIndexed { index, category ->
            AdviceAccordionCard(
                category = category,
                isExpanded = expandedIndex == index,
                onToggle = {
                    expandedIndex = if (expandedIndex == index) null else index
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun AdviceAccordionCard(
    category: AdviceCategory,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    val rotation by animateFloatAsState(if (isExpanded) 180f else 0f, label = "chevron")

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(category.dotColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = category.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(
                        start = 38.dp, end = 16.dp, bottom = 16.dp
                    )
                ) {
                    category.bullets.forEach { bullet ->
                        Row(modifier = Modifier.padding(bottom = 6.dp)) {
                            Text(
                                text = "\u2022",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = bullet,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = category.linkText,
                        fontSize = 13.sp,
                        color = LinkBlue,
                        modifier = Modifier.clickable {
                            try {
                                // prøver å åpne med CustomTabsIntent
                                val customTabsIntent = CustomTabsIntent.Builder().build()
                                customTabsIntent.launchUrl(context, category.linkUrl.toUri())
                            } catch (_: Exception) {
                                // hvis customtabsintent ikke starter, bruker vi vanlige launchingen
                                val intent = Intent(Intent.ACTION_VIEW, category.linkUrl.toUri())
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }
}
