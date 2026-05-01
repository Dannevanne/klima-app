package no.uio.ifi.in2000.team45.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Darkmode
val DarkBar = Color(0xFF1C3A5A)
val DarkBackground = Color(0xFF0F263E)
val DarkSurface = Color(0xFF1E3A5F)
val DarkOnSurface = Color(0xFFFFFFFF)
val DarkBox = Color(0xFF2C4F7C)

// Lightmode
val LightBackground = Color(0xFFC5D8EB)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF000000)
val LightBar = Color(0xFFFFFFFF)
val LightBox = Color(0xFFFFFFFF)

// Common
val StarGold = Color(0xFFC97B00)

// Loading indicators. Blue stands out on the white-ish light background and
// the deep-blue dark background without going to pure black/white.
val LoadingBlue = Color(0xFF1976D2)
val LoadingBlueLight = Color(0xFF90CAF9)

// colors for house icon on map
val IconWhite = Color(0xFFFFFFFF)
val IconBlack = Color(0xFF000000)

// Colors for switch buttons
val SwitchButtonGreen = Color(0xFF2E7D32)
val SwitchButtonGray = Color(0xFF5F6368)

// Colors for infoDatasourceIcons
val IconLightBlue = Color(0xFFE3F2FD)
val IconBlue = Color(0xFF1565C0)
val IconLightGreen = Color(0xFFE8F5E9)
val IconGreen = Color(0xFF388E3C)
val IconLightPurple = Color(0xFFEDE7F6)
val IconPurple = Color(0xFF6A1B9A)
val IconLightOrange = Color(0xFFFFF3E0)
val IconOrange = Color(0xFFE65100)

//Colors for ClimateDataView
val LineColorBlue = Color(0xFF42A5F5)
val FillColorBlue = Color(0x3042A5F5)
val LineColorOrange = Color(0xFFFF9800)
val FillColorOrange = Color(0x30FF9800)
val LineColorPurple = Color(0xFF7E57C2)
val FillColorPurple = Color(0x307E57C2)
val LineColorTurquoise = Color(0xFF26C6DA)
val FillColorTurquoise = Color(0x3026C6DA)

// Colors for ClimateComponents and savedPlaces statistics
val StatisticsRed = Color(0xFFEF4444)
val StatisticsGreen = Color(0xFF22C55E)

val StatisticsOrange = Color(0xFFF97316)

val LinkBlue = Color(0xFF60A5FA)
@Composable
fun loadingColor(): Color =
    if (isSystemInDarkTheme()) LoadingBlueLight else LoadingBlue