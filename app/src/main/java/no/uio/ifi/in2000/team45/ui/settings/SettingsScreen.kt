package no.uio.ifi.in2000.team45.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapbox.maps.extension.style.expressions.dsl.generated.hsl
import no.uio.ifi.in2000.team45.ui.map.MapTheme
import no.uio.ifi.in2000.team45.ui.map.MapViewModel
import androidx.compose.material3.RadioButton
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.res.painterResource
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.NavBarItem
import no.uio.ifi.in2000.team45.ui.common.AppBottomBar
import no.uio.ifi.in2000.team45.ui.common.AppTopBar
import no.uio.ifi.in2000.team45.ui.common.BottomBarItemData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onMapClick: () -> Unit,
    onStatisticClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.settings),
                navigationIcon = {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        },
        bottomBar = {
            AppBottomBar(
                items = listOf(
                    BottomBarItemData(
                        label = stringResource(R.string.nav_map),
                        onClick = onMapClick,
                        icon = { Icon(Icons.Filled.Place, contentDescription = null) }
                    ),
                    BottomBarItemData(
                        label = stringResource(R.string.saved_places),
                        onClick = onFavoritesClick,
                        icon = { Icon(Icons.Filled.FavoriteBorder, contentDescription = null) }
                    ),
                    BottomBarItemData(
                        label = stringResource(R.string.nav_statistics),
                        onClick = onStatisticClick,
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.filled_analytics),
                                contentDescription = null
                            )
                        }
                    )
                )
            )
        }
    ) {
        innerPadding ->
        LazyColumn(modifier = modifier
            .padding(innerPadding)
            .padding(8.dp)) {
            item {
                MapThemeSetting(
                    selectedTheme = mapViewModel.mapTheme,
                    onThemeSelected = { theme ->
                        mapViewModel.updateMapTheme(theme)
                    }
                )
            }

            item {
                HorizontalDivider()
            }

            item {
                LanguageSetting(
                    selectedLanguage = mapViewModel.selectedLanguage,
                    onLanguageSelected = { language ->
                        mapViewModel.updateLanguage(language)
                    }
                )
            }
            item {
                HorizontalDivider()
            }
            item {
                Row(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "Something something 2",
                        fontSize = 18.sp)
                    Switch(
                        modifier = Modifier.padding(8.dp),
                        checked = false,
                        onCheckedChange = {}
                    )
                }
            }
            item {
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun MapThemeSetting(selectedTheme: MapTheme,
                    onThemeSelected: (MapTheme) -> Unit,
                    modifier: Modifier = Modifier){
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(4.dp)) {
        Text(modifier = Modifier.padding(8.dp),
            text = stringResource(R.string.theme),
            fontSize = 18.sp)

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically)
        {
            RadioButton(selected = selectedTheme == MapTheme.DEVICE,
                onClick = { onThemeSelected(MapTheme.DEVICE)})
            Text(text = stringResource(R.string.device_settings))
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically)
        {
            RadioButton(selected = selectedTheme == MapTheme.LIGHT,
                onClick = { onThemeSelected(MapTheme.LIGHT)})
            Text(text = stringResource(R.string.lightmode))
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically)
        {
            RadioButton(selected = selectedTheme == MapTheme.DARK,
                onClick = { onThemeSelected(MapTheme.DARK)})
            Text(text = stringResource(R.string.darkmode))
        }
    }
}