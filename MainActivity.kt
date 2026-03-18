package no.uio.ifi.in2000.team45

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.data.kart.KartDatasource
import no.uio.ifi.in2000.team45.ui.theme.Team45Theme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Team45Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    App(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun App(modifier: Modifier = Modifier) {
    var tekst by remember { mutableStateOf("Laster...") }
    LaunchedEffect(Unit){
        try{
            tekst = KartDatasource().getKart()
        }
        catch(e: Exception){
            tekst = "Kunne ikke hente kart"
        }
    }
    Text(text = tekst, modifier = modifier)
}

fun getKart(): String {
    val kart = KartDatasource()
    var tekst = ""
    GlobalScope.launch(Dispatchers.Default) {
        tekst = kart.getKart()
    }
    return tekst
}