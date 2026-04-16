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
        title = "Flood measures for homeowners",
        dotColor = RiskHigh,
        bullets = listOf(
            "Install a backflow valve on your drainage to prevent sewage from entering the house during flooding.",
            "Raise the threshold to the basement and crawl space.",
            "Ensure drainage around the house directs water away from house walls.",
            "Check your insurance coverage for flood damage."
        ),
        linkText = "Read more at NVE flood protection handbook",
        linkUrl = "https://www.nve.no/flom-og-skred/sikring-mot-flom-og-erosjon/"
    ),
    AdviceCategory(
        title = "Landslide prevention",
        dotColor = RiskModerate,
        bullets = listOf(
            "Check NVE's hazard zone maps to identify if your property is in a risk area.",
            "Avoid altering slopes or removing vegetation near the home without assessment.",
            "Contact a geotechnical engineer if you notice ground movement or cracks in walls."
        ),
        linkText = "Read more at NVE landslide handbook",
        linkUrl = "https://www.nve.no/flom-og-skred/sikring-mot-skred/"
    ),
    AdviceCategory(
        title = "Wind protection",
        dotColor = RiskLow,
        bullets = listOf(
            "Secure loose items in the garden and on balconies before storms.",
            "Inspect roof tiles and gutters annually for damage.",
            "Trim large trees close to the house to reduce wind load."
        ),
        linkText = "Read more at NVE flood protection handbook",
        linkUrl = "https://www.nve.no/flom-og-skred/sikring-mot-flom-og-erosjon/"
    )
)

@Composable
fun WhatCanIDoSection() {
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    Text(
        text = "WHAT CAN I DO?",
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
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
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
