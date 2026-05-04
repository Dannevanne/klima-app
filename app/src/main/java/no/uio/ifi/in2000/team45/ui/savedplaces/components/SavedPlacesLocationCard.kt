package no.uio.ifi.in2000.team45.ui.savedplaces.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.data.savedplaces.SavedPlacesLocation
import no.uio.ifi.in2000.team45.ui.savedplaces.LocationStatus
import no.uio.ifi.in2000.team45.ui.savedplaces.RiskLevel
import no.uio.ifi.in2000.team45.ui.theme.StatisticsGray
import no.uio.ifi.in2000.team45.ui.theme.StatisticsGreen
import no.uio.ifi.in2000.team45.ui.theme.StatisticsOrange
import no.uio.ifi.in2000.team45.ui.theme.StatisticsRed

private val RiskHigh = StatisticsRed
private val RiskModerate = StatisticsOrange
private val RiskLow = StatisticsGreen
private val RiskUnknown = StatisticsGray

private fun windRiskLevel(windSpeed: Double?): RiskLevel = when {
    windSpeed == null -> RiskLevel.UNKNOWN
    windSpeed >= 8.0 -> RiskLevel.HIGH
    windSpeed >= 5.0 -> RiskLevel.MODERATE
    else -> RiskLevel.LOW
}

private fun riskColor(level: RiskLevel): Color = when (level) {
    RiskLevel.HIGH -> RiskHigh
    RiskLevel.MODERATE -> RiskModerate
    RiskLevel.LOW -> RiskLow
    RiskLevel.UNKNOWN -> RiskUnknown
}

@Composable
private fun riskLevelLabel(level: RiskLevel): String = when (level) {
    RiskLevel.HIGH -> stringResource(R.string.risk_high)
    RiskLevel.MODERATE -> stringResource(R.string.risk_moderate)
    RiskLevel.LOW -> stringResource(R.string.risk_low)
    RiskLevel.UNKNOWN -> stringResource(R.string.risk_unknown)
}

private fun overallRisk(vararg levels: RiskLevel): RiskLevel = when {
    levels.any { it == RiskLevel.HIGH } -> RiskLevel.HIGH
    levels.any { it == RiskLevel.MODERATE } -> RiskLevel.MODERATE
    levels.any { it == RiskLevel.LOW } -> RiskLevel.LOW
    else -> RiskLevel.UNKNOWN
}

@Composable
fun SavedPlacesLocationCard(
    location: SavedPlacesLocation,
    status: LocationStatus?,
    onRemove: () -> Unit,
    onShowOnMap: () -> Unit,
    savedPlacesCount: Int
) {
    val loading = status == null
    val floodLevel = status?.floodLevel ?: RiskLevel.UNKNOWN
    val landslideLevel = status?.landslideLevel ?: RiskLevel.UNKNOWN
    val avalancheLevel = status?.avalancheLevel ?: RiskLevel.UNKNOWN
    val quickClayLevel = status?.quickClayLevel ?: RiskLevel.UNKNOWN
    val radonLevel = status?.radonLevel ?: RiskLevel.UNKNOWN
    val windLevel = windRiskLevel(status?.avgWindSpeed)
    val overall = overallRisk(
        floodLevel, landslideLevel, avalancheLevel, quickClayLevel, radonLevel, windLevel
    )

    var showRadonInfo by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: name + star
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = location.label.ifBlank { stringResource(R.string.map_location_placeholder) },
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onShowOnMap) {
                    Icon(
                        Icons.Outlined.Place,
                        contentDescription = stringResource(R.string.show_on_map),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.remove),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Overall risk
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.overall_risk),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp)
                )
                if (loading) RiskSpinner() else RiskPill(level = overall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3x2 grid of risk categories. IntrinsicSize.Max + fillMaxHeight
            // keeps each row's boxes the same height when one label wraps to
            // two lines (e.g. "Jord- og flomskred" or longer Sami labels).
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RiskCategoryBox(
                    label = stringResource(R.string.flood),
                    level = floodLevel,
                    loading = loading,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                RiskCategoryBox(
                    label = stringResource(R.string.landslide),
                    level = landslideLevel,
                    loading = loading,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                RiskCategoryBox(
                    label = stringResource(R.string.avalanche),
                    level = avalancheLevel,
                    loading = loading,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RiskCategoryBox(
                    label = stringResource(R.string.quick_clay),
                    level = quickClayLevel,
                    loading = loading,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                RiskCategoryBox(
                    label = stringResource(R.string.wind),
                    level = windLevel,
                    loading = loading,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                RiskCategoryBox(
                    label = stringResource(R.string.radon),
                    level = radonLevel,
                    loading = loading,
                    onInfoClick = if (status?.radonClass != null) {
                        { showRadonInfo = true }
                    } else null,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            if (showRadonInfo && status?.radonClass != null) {
                RadonInfoDialog(
                    radonClass = status.radonClass,
                    onDismiss = { showRadonInfo = false }
                )
            }

            if (!loading) {
                WhatCanIDoSection(
                    floodLevel = floodLevel,
                    landslideLevel = landslideLevel,
                    quickClayLevel = quickClayLevel,
                    windLevel = windLevel,
                    savedPlacesCount = savedPlacesCount
                )
            }
        }
    }
}

@Composable
private fun RiskPill(level: RiskLevel) {
    Box(
        modifier = Modifier
            .background(riskColor(level), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = riskLevelLabel(level),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RiskCategoryBox(
    label: String,
    level: RiskLevel,
    loading: Boolean,
    onInfoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.background,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        if (onInfoClick != null) {
            IconButton(
                onClick = onInfoClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.info),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = riskLevelLabel(level),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = riskColor(level)
                )
            }
        }
    }
}

@Composable
private fun RiskSpinner() {
    CircularProgressIndicator(
        modifier = Modifier.size(16.dp),
        strokeWidth = 2.dp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
