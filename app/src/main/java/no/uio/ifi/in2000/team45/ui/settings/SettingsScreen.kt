package no.uio.ifi.in2000.team45.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.team45.ui.map.MapTheme
import no.uio.ifi.in2000.team45.ui.map.MapViewModel
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.MaterialTheme
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.common.BackButtonTopBar
import no.uio.ifi.in2000.team45.ui.savedplaces.SavedPlacesViewModel
import no.uio.ifi.in2000.team45.ui.statistic.StatisticViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel,
    onClick: () -> Unit,
    statisticViewModel: StatisticViewModel,
    savedPlacesViewModel: SavedPlacesViewModel,
    musicEnabled: Boolean,
    onMusicEnabledChanged: (Boolean) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BackButtonTopBar(
                title = stringResource(R.string.settings),
                onBackClick = onClick
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
                MusicSetting(
                    musicEnabled = musicEnabled,
                    onMusicEnabledChanged = onMusicEnabledChanged
                )
            }
            item {
                HorizontalDivider()
            }
            item {
                ResetAppDataSection(
                    onResetConfirmed = {
                        savedPlacesViewModel.removeAllSavedPlaces()
                        statisticViewModel.resetAllStatisticData()
                    }
                )
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