package no.uio.ifi.in2000.team45.ui.help

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOutMap
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.maps.extension.compose.annotation.IconImage
import com.mapbox.maps.extension.style.expressions.dsl.generated.mod
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.common.BackButtonTopBar
import androidx.compose.material.icons.outlined.WifiOff

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BackButtonTopBar(
                title = stringResource(R.string.help),
                onBackClick = onClick
            )
        },
    ) { innerpadding ->
        LazyColumn(
            modifier = modifier
                .padding(innerpadding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Availability-section
            item {
                HelpSectionTitle(stringResource(R.string.offline_support))
            }
            item {
                HelpItem(
                    iconImage = Icons.Outlined.WifiOff,
                    title = stringResource(R.string.without_internet_connection),
                    description = stringResource(R.string.offline_limitations_description)
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Map-section
            item {
                HelpSectionTitle(stringResource(R.string.map))
            }
            item {
                HelpItem(
                    iconImage = Icons.Filled.Search,
                    title = stringResource(R.string.search_for_places),
                    description = stringResource(R.string.paste_a_placename_in_the_searchbar_to_navigate_on_the_map)
                )
            }
            item {
                HelpItem(
                    iconImage = Icons.Filled.KeyboardArrowUp,
                    title = stringResource(R.string.select_overlay),
                    description = stringResource(R.string.select_an_overlay_by_pushing_the_bottom_left_button)
                )
            }
            item {
                HelpItem(
                    iconImage = Icons.Outlined.Info,
                    title = stringResource(R.string.overlay_explanation),
                    description = stringResource(R.string.press_to_see_explanation_for_selected_overlay)
                )
            }
            item {
                HelpItem(
                    iconImage = Icons.Outlined.ZoomOutMap,
                    title = stringResource(R.string.zoom),
                    description = stringResource(R.string.zoom_explanation)
                )
            }
            item {
                HelpItem(
                    iconImage = Icons.Filled.FavoriteBorder,
                    title = stringResource(R.string.save_place),
                    description = stringResource(R.string.tap_and_hold_on_a_location_on_the_map_to_save_it_to_your_saved_places)
                )
            }
            item {
                HelpItem(
                    iconImage = Icons.Outlined.LocationOn,
                    title = stringResource(R.string.my_location),
                    description = stringResource(R.string.button_to_see_location_or_give_permission)
                )
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Saved places - section
            item {
                HelpSectionTitle(stringResource(R.string.saved_places))
            }
            item {
                HelpItem(
                    iconImage = Icons.Filled.FavoriteBorder,
                    title = stringResource(R.string.saved_places),
                    description = stringResource(R.string.section_for_your_saved_places_for_quick_access)
                )
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Statistics-section
            item {
                HelpSectionTitle(stringResource(R.string.statistics))
            }
            item {
                HelpItem(
                    iconImage = Icons.Filled.Search,
                    title = stringResource(R.string.search_for_places),
                    description = stringResource(R.string.search_for_a_place_to_see_climate_statistics)
                )
            }
            item {
                HelpItem(
                    iconImage = Icons.Outlined.Info,
                    title = stringResource(R.string.select_a_time_period),
                    description = stringResource(R.string.use_10_30_or_50_years_to_see_climate_trends)
                )
            }
            item {
                HelpItem(
                    iconImage = Icons.Outlined.Star,
                    title = stringResource(R.string.save_area),
                    description = stringResource(R.string.press_the_star_to_save_an_area)
                )
            }
            item {
                HelpItem(
                    iconImage = Icons.Outlined.Warning,
                    title = stringResource(R.string.missing_data),
                    description = stringResource(R.string.warning_when_missing_data)
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// Structure for section-title
@Composable
private fun HelpSectionTitle(title: String){
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

// Structure for help-cards in help-screen
@Composable
private fun HelpItem(
    iconImage: ImageVector,
    title: String,
    description: String
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ){
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ){
            Icon(
                imageVector = iconImage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
        Column{
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}