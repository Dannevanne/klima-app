package no.uio.ifi.in2000.team45.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team45.R

private const val PAGE_COUNT = 4

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            OnboardingTopBar(
                pageIndex = pagerState.currentPage,
                onSkip = onFinished
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> MapPage()
                    2 -> SavedPlacesPage()
                    3 -> StatisticsPage()
                }
            }

            PaginationDots(currentPage = pagerState.currentPage, total = PAGE_COUNT)

            Spacer(Modifier.height(8.dp))

            CtaButton(
                pageIndex = pagerState.currentPage,
                onClick = {
                    if (pagerState.currentPage == PAGE_COUNT - 1) {
                        onFinished()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun OnboardingTopBar(pageIndex: Int, onSkip: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (pageIndex in 1..3) {
            Text(
                text = stringResource(R.string.onboarding_progress, pageIndex),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onSkip) {
            Text(
                text = stringResource(R.string.onboarding_skip),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.safeproperty_logo_v6),
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.size(220.dp)
        )
        Spacer(Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.onboarding_welcome_subtitle),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun MapPage() {
    OnboardingListPage(
        title = stringResource(R.string.map),
        subtitle = stringResource(R.string.onboarding_kart_subtitle),
        items = listOf(
            OnboardingItem(
                icon = Icons.Filled.Search,
                title = stringResource(R.string.search_for_places),
                description = stringResource(R.string.paste_a_place_name_in_the_search_bar_to_navigate_on_the_map)
            ),
            OnboardingItem(
                icon = Icons.Filled.KeyboardArrowUp,
                title = stringResource(R.string.select_overlay),
                description = stringResource(R.string.onboarding_select_overlay_desc)
            ),
            OnboardingItem(
                icon = Icons.Outlined.LocationOn,
                title = stringResource(R.string.my_location),
                description = stringResource(R.string.onboarding_my_location_desc)
            ),
            OnboardingItem(
                icon = Icons.Outlined.Info,
                title = stringResource(R.string.overlay_explanation),
                description = stringResource(R.string.onboarding_overlay_explanation_desc)
            )
        )
    )
}

@Composable
private fun SavedPlacesPage() {
    OnboardingListPage(
        title = stringResource(R.string.saved_places),
        subtitle = stringResource(R.string.onboarding_saved_subtitle),
        items = listOf(
            OnboardingItem(
                icon = Icons.Filled.FavoriteBorder,
                title = stringResource(R.string.save_place),
                description = stringResource(R.string.tap_and_hold_on_a_location_on_the_map_to_save_it_to_your_saved_places)
            ),
            OnboardingItem(
                icon = Icons.Outlined.Place,
                title = stringResource(R.string.show_on_map),
                description = stringResource(R.string.show_on_map_help)
            ),
            OnboardingItem(
                icon = Icons.Filled.KeyboardArrowDown,
                title = stringResource(R.string.safety_information),
                description = stringResource(R.string.expand_advice_help)
            ),
            OnboardingItem(
                icon = Icons.Filled.Delete,
                title = stringResource(R.string.remove_saved_place),
                description = stringResource(R.string.remove_saved_place_help)
            )
        )
    )
}

@Composable
private fun StatisticsPage() {
    OnboardingListPage(
        title = stringResource(R.string.statistics),
        subtitle = stringResource(R.string.onboarding_stats_subtitle),
        items = listOf(
            OnboardingItem(
                icon = Icons.Filled.Search,
                title = stringResource(R.string.search_for_places),
                description = stringResource(R.string.search_for_a_place_to_see_climate_statistics)
            ),
            OnboardingItem(
                icon = Icons.Outlined.Info,
                title = stringResource(R.string.select_a_time_period),
                description = stringResource(R.string.use_10_30_or_50_years_to_see_climate_trends)
            ),
            OnboardingItem(
                icon = Icons.Outlined.Star,
                title = stringResource(R.string.save_area),
                description = stringResource(R.string.press_the_star_to_save_an_area)
            ),
            OnboardingItem(
                icon = Icons.Outlined.Warning,
                title = stringResource(R.string.missing_data),
                description = stringResource(R.string.warning_when_missing_data)
            )
        ),
        footer = stringResource(R.string.onboarding_footer_help)
    )
}

private data class OnboardingItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@Composable
private fun OnboardingListPage(
    title: String,
    subtitle: String,
    items: List<OnboardingItem>,
    footer: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp
        )
        Spacer(Modifier.height(24.dp))
        items.forEachIndexed { index, item ->
            OnboardingItemRow(item)
            if (index != items.lastIndex) Spacer(Modifier.height(18.dp))
        }
        if (footer != null) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = footer,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun OnboardingItemRow(item: OnboardingItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.padding(top = 2.dp)) {
            Text(
                text = item.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.description,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun PaginationDots(currentPage: Int, total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { i ->
            val active = i == currentPage
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = if (active) 22.dp else 8.dp, height = 8.dp)
                    .background(
                        color = if (active) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun CtaButton(pageIndex: Int, onClick: () -> Unit) {
    val isLast = pageIndex == PAGE_COUNT - 1
    val isFirst = pageIndex == 0
    val label = if (isLast) {
        stringResource(R.string.onboarding_get_started)
    } else {
        stringResource(R.string.onboarding_next)
    }

    if (isFirst) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
