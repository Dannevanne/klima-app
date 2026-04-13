package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// This composable doesn't know about MapBox, the camera or the ViewModel
// it just sends the buttons to whoeever uses the component
@Composable
fun MapZoomControls(onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier
){
    Column(modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledIconButton(
            onClick = onZoomIn,
            colors = IconButtonColors(
                Color.LightGray,
                Color.White,
                Color.LightGray,
                Color.LightGray)
        ) {
            Text(text = "+",
                fontSize = 20.sp)
        }
        FilledIconButton(
            onClick = onZoomOut,
            colors = IconButtonColors(
                Color.LightGray,
                Color.White,
                Color.LightGray,
                Color.LightGray)) {
            Text(text = "-",
                fontSize = 20.sp)
        }
    }
}