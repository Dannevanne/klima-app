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
import no.uio.ifi.in2000.team45.ui.map.MapViewModel
import no.uio.ifi.in2000.team45.ui.theme.AppTheme
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.MaterialTheme
import no.uio.ifi.in2000.team45.R
import no.uio.ifi.in2000.team45.ui.common.BackButtonTopBar
import no.uio.ifi.in2000.team45.ui.savedplaces.SavedPlacesViewModel
import no.uio.ifi.in2000.team45.ui.settings.components.LanguageSetting
import no.uio.ifi.in2000.team45.ui.settings.components.ResetAppDataSection
import no.uio.ifi.in2000.team45.ui.settings.components.SoundSetting
import no.uio.ifi.in2000.team45.ui.statistic.StatisticViewModel

/**
 * Main settings screen for the application.

 * @param modifier Optional modifier for layout customization.
 * @param mapViewModel ViewModel responsible for theme and language settings.
 * @param onClick Callback triggered when navigating back.
 * @param statisticViewModel ViewModel handling statistics reset actions.
 * @param savedPlacesViewModel ViewModel handling saved places reset actions.
 * @param soundEnabled Whether sound effects are currently enabled.
 * @param onSoundEnabledChanged Callback triggered when sound setting changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel,
    onClick: () -> Unit,
    statisticViewModel: StatisticViewModel,
    savedPlacesViewModel: SavedPlacesViewModel,
    soundEnabled: Boolean,
    onSoundEnabledChanged: (Boolean) -> Unit
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
                AppThemeSetting(
                    selectedTheme = mapViewModel.appTheme,
                    onThemeSelected = { theme ->
                        mapViewModel.updateAppTheme(theme)
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
                SoundSetting(
                    soundEnabled = soundEnabled,
                    onSoundEnabledChange = onSoundEnabledChanged
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

/**
 * Displays the app theme selection UI with radio buttons for DEVICE, LIGHT, and DARK mode.
 *
 * @param selectedTheme the currently selected app theme.
 * @param onThemeSelected callback invoked when the user selects a different theme option.
 * @param modifier optional modifier for styling and layout adjustments.
 */
@Composable
fun AppThemeSetting(selectedTheme: AppTheme,
                    onThemeSelected: (AppTheme) -> Unit,
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
            RadioButton(selected = selectedTheme == AppTheme.DEVICE,
                onClick = { onThemeSelected(AppTheme.DEVICE)})
            Text(text = stringResource(R.string.device_settings))
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically)
        {
            RadioButton(selected = selectedTheme == AppTheme.LIGHT,
                onClick = { onThemeSelected(AppTheme.LIGHT)})
            Text(text = stringResource(R.string.lightmode))
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically)
        {
            RadioButton(selected = selectedTheme == AppTheme.DARK,
                onClick = { onThemeSelected(AppTheme.DARK)})
            Text(text = stringResource(R.string.darkmode))
        }
    }
}