package no.uio.ifi.in2000.team45.ui.info

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NoAccounts
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.Water
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.common.BackButtonTopBar
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BackButtonTopBar(
                title = stringResource(R.string.info),
                onBackClick = onClick
            )
        },
    ) { innerpadding ->
        LazyColumn(
            modifier = modifier.padding(innerpadding)
        ) {
            item {
                AppRepresentation()
            }
            // Data and API sources
            item { InfoSectionName(title = stringResource(R.string.datasources)) }
            item {
                InfoDataCard(
                    name = stringResource(R.string.nve),
                    description = stringResource(R.string.flood_landslides_quick_clay_wind),
                    containerColor = Color(0xFFE3F2FD),
                    iconColor = Color(0xFF1565C0),
                    icon = Icons.Outlined.Water,
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.nve.no/"))
                        )
                    }
                )
            }
            item {
                InfoDataCard(
                    name = stringResource(R.string.frost_api_v0),
                    description = stringResource(R.string.climate_statistics),
                    containerColor = Color(0xFFE8F5E9),
                    iconColor = Color(0xFF388E3C),
                    icon = Icons.Outlined.DeviceThermostat,
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://frost.met.no/index.html"))
                        )
                    }
                )
            }
            item {
                InfoDataCard(
                    name = stringResource(R.string.mapbox),
                    description = stringResource(R.string.map_and_search),
                    containerColor = Color(0xFFEDE7F6),
                    iconColor = Color(0xFF6A1B9A),
                    icon = Icons.Outlined.TravelExplore,
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.mapbox.com/"))
                        )
                    }
                )
            }
            item {
                InfoDataCard(
                    name = stringResource(R.string.ngu_radonwms),
                    description = stringResource(R.string.radon_gas),
                    containerColor = Color(0xFFFFF3E0),
                    iconColor = Color(0xFFE65100),
                    icon = Icons.Outlined.Cloud,
                    onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ngu.no/"))
                        )
                    }
                )
            }

            // Privacy
            item { InfoSectionName(stringResource(R.string.privacy)) }
            item {
                InfoPrivacySection(
                    icon = Icons.Outlined.Lock,
                    text = stringResource(R.string.location_data_is_used_only_locally_and_is_not_shared_with_third_parties)
                )
            }
            item {
                InfoPrivacySection(
                    icon = Icons.Outlined.NoAccounts,
                    text = stringResource(R.string.the_app_does_not_require_a_login_or_account)
                )
            }
            // The team
            item { InfoSectionName(stringResource(R.string.the_team)) }
            item {
                TeamSection()
            }

            // Version
            item {
                VersionSection()
            }
        }
    }
}

@Composable
private fun InfoSectionName(
    title: String
) {
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 12.dp)
    )
}

// Structure for info-cards
@Composable
private fun InfoDataCard(
    name: String,
    description: String,
    containerColor: Color,
    iconColor: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, start = 16.dp, end = 16.dp)
            .clickable { onClick() }
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color = containerColor, shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Column {
            Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// Structure for privacy section
@Composable
private fun InfoPrivacySection(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .padding(start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun VersionSection(){
    Text(
        text = stringResource(R.string.versjon_1_0_0),
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    )
}

@Composable
fun TeamSection(){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(start = 16.dp, end = 16.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(99.dp)
            )
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(99.dp)
            )
            .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.t45),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.team_45_in2000_uio_2026),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AppRepresentation(){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(24.dp)
    ){
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.app_description),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}