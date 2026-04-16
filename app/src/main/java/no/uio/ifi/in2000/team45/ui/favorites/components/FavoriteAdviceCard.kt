package no.uio.ifi.in2000.team45.ui.favorites.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val RiskHigh = Color(0xFFEF4444)
private val RiskModerate = Color(0xFFF97316)
private val RiskLow = Color(0xFF22C55E)
private val LinkBlue = Color(0xFF60A5FA)

data class AdviceCategory(
    val title: String,
    val dotColor: Color,
    val bullets: List<String>,
    val linkText: String,
    val linkUrl: String
)

val adviceCategories = listOf(
    AdviceCategory(
        title = "Flomtiltak for huseiere",
        dotColor = RiskHigh,
        bullets = listOf(
            "Installer tilbakeslagsventil pa avlop for a hindre kloakk inn i huset under flom.",
            "Hev terskelen til kjeller og kryprom.",
            "Sorg for at overflatevann ledes bort fra husveggene.",
            "Sjekk forsikringsdekningen din for flomskade."
        ),
        linkText = "Les mer hos NVE sin flomhandbok",
        linkUrl = "https://www.nve.no/flom-og-skred/sikring-mot-flom-og-erosjon/"
    ),
    AdviceCategory(
        title = "Jordskredforebygging",
        dotColor = RiskModerate,
        bullets = listOf(
            "Sjekk NVE sine faresoner for a se om eiendommen din er i et risikomrade.",
            "Unnga a endre skraninger eller fjerne vegetasjon uten faglig vurdering.",
            "Kontakt geotekniker hvis du ser bevegelse i grunnen eller sprekker i vegger."
        ),
        linkText = "Les mer hos NVE sin skredhandbok",
        linkUrl = "https://www.nve.no/flom-og-skred/sikring-mot-skred/"
    ),
    AdviceCategory(
        title = "Vindbeskyttelse",
        dotColor = RiskLow,
        bullets = listOf(
            "Sikre lause gjenstander i hagen og pa balkonger for stormer.",
            "Inspiser takstein og takrenner arlig for skade.",
            "Trim store traer naer huset for a redusere vindbelastning."
        ),
        linkText = "Les mer hos NVE sin flomsikringshandbok",
        linkUrl = "https://www.nve.no/flom-og-skred/sikring-mot-flom-og-erosjon/"
    )
)

@Composable
fun WhatCanIDoSection() {
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    Text(
        text = "HVA KAN JEG GJORE?",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )

    adviceCategories.forEachIndexed { index, category ->
        AdviceAccordionCard(
            category = category,
            isExpanded = expandedIndex == index,
            onToggle = {
                expandedIndex = if (expandedIndex == index) null else index
            }
        )
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun AdviceAccordionCard(
    category: AdviceCategory,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Header row (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(category.dotColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = category.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded)
                        Icons.Filled.KeyboardArrowUp
                    else
                        Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Lukk" else "Utvid",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expandable content
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(
                        start = 38.dp, end = 16.dp, bottom = 16.dp
                    )
                ) {
                    category.bullets.forEach { bullet ->
                        Row(modifier = Modifier.padding(bottom = 6.dp)) {
                            Text(
                                text = "\u2022",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = bullet,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${category.linkText} ->",
                        fontSize = 13.sp,
                        color = LinkBlue,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(category.linkUrl))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
