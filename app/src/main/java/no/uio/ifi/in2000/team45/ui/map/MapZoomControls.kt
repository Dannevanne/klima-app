package no.uio.ifi.in2000.team45.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import java.lang.reflect.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MapZoomControls(onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier
){
    Column(modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledIconButton(onClick = onZoomIn) {
            Text(text = "+",
                fontSize = 20.sp)
        }
        FilledIconButton(onClick = onZoomOut) {
            Text(text = "-",
                fontSize = 20.sp)
        }
    }
}