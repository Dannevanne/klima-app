package no.uio.ifi.in2000.team45.ui.savedplaces.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import no.uio.ifi.in2000.team45.ui.theme.StatisticsGreen
import no.uio.ifi.in2000.team45.ui.theme.StatisticsOrange
import no.uio.ifi.in2000.team45.ui.theme.StatisticsRed

private val RiskHigh = StatisticsRed
private val RiskModerate = StatisticsOrange
private val RiskLow = StatisticsGreen

private fun riskLevel(isInZone: Boolean, windSpeed: Double? = null, isWind: Boolean = false): String {
    if (isWind) {
        return when {
            windSpeed == null -> "Low"
            windSpeed >= 8.0 -> "High"
            windSpeed >= 5.0 -> "Moderate"
            else -> "Low"
        }
    }
    return if (isInZone) "High" else "Low"
}

private fun riskColor(level: String): Color {
    return when (level) {
        "High" -> RiskHigh
        "Moderate" -> RiskModerate
        else -> RiskLow
    }
}

@Composable
private fun riskLevelLabel(level: String): String {
    return when (level) {
        "High" -> stringResource(R.string.risk_high)
        "Moderate" -> stringResource(R.string.risk_moderate)
        else -> stringResource(R.string.risk_low)
    }
}

private fun overallRisk(vararg levels: String): String {
    return when {
        levels.any { it == "High" } -> "High"
        levels.any { it == "Moderate" } -> "Moderate"
        else -> "Low"
    }
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
    val floodLevel = status?.floodLevel ?: "Low"
    val landslideLevel = status?.landslideLevel ?: "Low"
    val kvikkleireLevel = status?.kvikkleireLevel ?: "Low"
    val radonLevel = status?.radonLevel ?: "Low"
    val windLevel = riskLevel(false, status?.avgWindSpeed, isWind = true)
    val overall = overallRisk(
        floodLevel, landslideLevel, kvikkleireLevel, radonLevel, windLevel
    )

    var showKvikkleireInfo by remember { mutableStateOf(false) }
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

            // Five risk categories: 3 on the top row, 2 centered below.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RiskCategoryBox(
                    label = stringResource(R.string.flood),
                    level = floodLevel,
                    loading = loading,
                    modifier = Modifier.weight(1f)
                )
                RiskCategoryBox(
                    label = stringResource(R.string.landslide),
                    level = landslideLevel,
                    loading = loading,
                    modifier = Modifier.weight(1f)
                )
                RiskCategoryBox(
                    label = stringResource(R.string.quick_clay),
                    level = kvikkleireLevel,
                    loading = loading,
                    onInfoClick = if (status?.kvikkleireFaregrad != null) {
                        { showKvikkleireInfo = true }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.weight(0.5f))
                RiskCategoryBox(
                    label = stringResource(R.string.wind),
                    level = windLevel,
                    loading = loading,
                    modifier = Modifier.weight(1f)
                )
                RiskCategoryBox(
                    label = stringResource(R.string.radon),
                    level = radonLevel,
                    loading = loading,
                    onInfoClick = if (status?.radonClass != null) {
                        { showRadonInfo = true }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(0.5f))
            }

            if (showKvikkleireInfo && status?.kvikkleireFaregrad != null) {
                KvikkleireInfoDialog(
                    faregrad = status.kvikkleireFaregrad,
                    onDismiss = { showKvikkleireInfo = false }
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
                    windLevel = windLevel,
                    savedPlacesCount = savedPlacesCount
                )
            }
        }
    }
}

@Composable
private fun RiskPill(level: String) {
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
    level: String,
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
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
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
