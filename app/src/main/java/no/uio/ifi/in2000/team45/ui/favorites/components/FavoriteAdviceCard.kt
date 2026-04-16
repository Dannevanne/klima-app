package no.uio.ifi.in2000.team45.ui.favorites.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdviceCard(text: String,
                       backgroundColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Composable
fun FloodZoneTips(locationName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE0B2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Flomsone-varsel for $locationName",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Denne posisjonen ligger i en flomsone. Her er noen tips:",
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("- Sjekk NVE sine flomvarsler jevnlig", fontSize = 13.sp)
            Text("- Ha en beredskapsplan for flom", fontSize = 13.sp)
            Text("- Flytt verdisaker bort fra kjeller/underetasje", fontSize = 13.sp)
            Text("- Sjekk forsikringsdekningen din", fontSize = 13.sp)
        }
    }
}

@Composable
fun RadonZoneTips(locationName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Radon-aktsomhet for $locationName",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF283593)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Denne posisjonen ligger i en radon-aktsomhetssone. Her er noen tips:",
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("- Maal radonnivaaet i boligen din", fontSize = 13.sp)
            Text("- Tiltaksgrensen er 200 Bq/m3 (Statens straalevern)", fontSize = 13.sp)
            Text("- Installer radonbroenner eller ventilasjon ved hoye verdier", fontSize = 13.sp)
            Text("- Sjekk NGU sin radonkartlegging for mer info", fontSize = 13.sp)
        }
    }
}

@Composable
fun QuickClayZoneTips(locationName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Kvikkleireomraade for $locationName",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFFB71C1C)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Denne posisjonen ligger i et kvikkleireomraade. Her er noen tips:",
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("- Unngaa graving og terrenginngrep uten geoteknisk vurdering", fontSize = 13.sp)
            Text("- Kontakt kommunen foer du gjor endringer paa tomten", fontSize = 13.sp)
            Text("- Foelg med paa NVE sine varslinger for jordskred", fontSize = 13.sp)
            Text("- Vurder grunnundersokelse ved bygging", fontSize = 13.sp)
        }
    }
}

@Composable
fun WindZoneTips(locationName: String, avgSpeed: Double) {
    val level = when {
        avgSpeed < 4.0 -> "lav"
        avgSpeed < 6.0 -> "moderat"
        avgSpeed < 8.0 -> "hoey"
        else -> "svart hoey"
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Vindforhold for $locationName",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color(0xFF006064)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Gjennomsnittlig vindstyrke: %.1f m/s (%s)".format(avgSpeed, level),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (avgSpeed >= 6.0) {
                Text("- Sikre lause gjenstander utendoors ved sterk vind", fontSize = 13.sp)
                Text("- Sjekk at tak og kledning taaler vindbelastningen", fontSize = 13.sp)
                Text("- Planlegg levegg eller beplantning som vindskjerm", fontSize = 13.sp)
            }
            Text("- Foelg med paa yr.no og varsom.no for vindvarsler", fontSize = 13.sp)
        }
    }
}