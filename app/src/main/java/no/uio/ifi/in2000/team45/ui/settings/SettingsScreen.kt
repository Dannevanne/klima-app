package no.uio.ifi.in2000.team45.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onMapClick: () -> Unit,
    onStatisticClick: () -> Unit,
    modifier: Modifier = Modifier,
    mapViewModel: MapViewModel
) {
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(8.dp),
                navigationIcon = { Icon(Icons.Filled.Settings, contentDescription = "") },
                title = {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = "Settings",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold)
                }
            )
        },
        bottomBar = {
            BottomAppBar{
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton({}) {
                        Icon(Icons.Filled.Settings, contentDescription = "")
                    }
                    IconButton(onClick = onMapClick) {
                        Icon(Icons.Outlined.Place, contentDescription = "to map")
                    }
                    IconButton(onClick = onStatisticClick) {
                        Icon(Icons.Outlined.Info, contentDescription = "to statistics")
                    }
                }
            }
        }
    ) {
        innerPadding ->
        LazyColumn(modifier = modifier.padding(innerPadding).padding(8.dp)) {
            item {
                MapThemeSetting(selectedTheme = mapViewModel.mapTheme,
                    onThemeSelected = { theme ->
                        mapViewModel.updateMapTheme(theme)
                    })
            }
            item {
                HorizontalDivider()
            }
            item {
                Row(
                    modifier = Modifier.padding(4.dp).fillMaxWidth(),
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
    Column(modifier = modifier.fillMaxWidth().padding(4.dp)) {
        Text(modifier = Modifier.padding(8.dp),
            text = "Theme",
            fontSize = 18.sp)

        Row(modifier = Modifier.fillMaxWidth().
        padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically)
        {
            RadioButton(selected = selectedTheme == MapTheme.DEVICE,
                onClick = { onThemeSelected(MapTheme.DEVICE)})
            Text(text = "Use device settings")
        }

        Row(modifier = Modifier.fillMaxWidth().
        padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically)
        {
            RadioButton(selected = selectedTheme == MapTheme.LIGHT,
                onClick = { onThemeSelected(MapTheme.LIGHT)})
            Text(text = "Lightmode")
        }

        Row(modifier = Modifier.fillMaxWidth().
        padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically)
        {
            RadioButton(selected = selectedTheme == MapTheme.DARK,
                onClick = { onThemeSelected(MapTheme.DARK)})
            Text(text = "Darkmode   ")
        }
    }
}