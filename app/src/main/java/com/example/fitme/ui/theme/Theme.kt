package com.example.fitme.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- PURPLE ---
private val PurpleDarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF151218), 
    surface = Color(0xFF151218),
    primaryContainer = Color(0xFF673AB7),   // Lvl 1
    secondaryContainer = Color(0xFF9575CD), // Lvl 2
    tertiaryContainer = Color(0xFFE91E63)   // Lvl 3
)

private val PurpleLightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    primaryContainer = Color(0xFFD1C4E9),
    secondaryContainer = Color(0xFFE1BEE7),
    tertiaryContainer = Color(0xFFF8BBD0)
)

// --- GREEN ---
private val GreenDarkColorScheme = darkColorScheme(
    primary = Green80,
    secondary = GreenGrey80,
    tertiary = Green80,
    background = Color(0xFF0C1410),
    surface = Color(0xFF0C1410),
    primaryContainer = Color(0xFF2E7D32),   // Lvl 1: Ярко-зеленый
    secondaryContainer = Color(0xFF00897B), // Lvl 2: Мятный
    tertiaryContainer = Color(0xFFAFB42B)   // Lvl 3: Охра / Горчичный
)

private val GreenLightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = Green40,
    primaryContainer = Color(0xFFC8E6C9),
    secondaryContainer = Color(0xFFB2DFDB),
    tertiaryContainer = Color(0xFFF0F4C3)
)

// --- BLUE ---
private val BlueDarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = BlueGrey80,
    tertiary = Blue80,
    background = Color(0xFF0A0E1A),
    surface = Color(0xFF0A0E1A),
    primaryContainer = Color(0xFF1976D2),   // Lvl 1
    secondaryContainer = Color(0xFF0288D1), // Lvl 2
    tertiaryContainer = Color(0xFF512DA8)   // Lvl 3
)

private val BlueLightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = BlueGrey40,
    tertiary = Blue40,
    primaryContainer = Color(0xFFBBDEFB),
    secondaryContainer = Color(0xFFB3E5FC),
    tertiaryContainer = Color(0xFFD1C4E9)
)

// --- ORANGE ---
private val OrangeDarkColorScheme = darkColorScheme(
    primary = Orange80,
    secondary = OrangeGrey80,
    tertiary = Orange80,
    background = Color(0xFF1A120B),
    surface = Color(0xFF1A120B),
    primaryContainer = Color(0xFFE64A19),   // Lvl 1
    secondaryContainer = Color(0xFFFFA000), // Lvl 2
    tertiaryContainer = Color(0xFFFBC02D)   // Lvl 3
)

private val OrangeLightColorScheme = lightColorScheme(
    primary = Orange40,
    secondary = OrangeGrey40,
    tertiary = Orange40,
    primaryContainer = Color(0xFFFFCCBC),
    secondaryContainer = Color(0xFFFFECB3),
    tertiaryContainer = Color(0xFFFFF9C4)
)

@Composable
fun FitmeTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    colorVariant: ColorVariant = ColorVariant.PURPLE,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when (colorVariant) {
        ColorVariant.PURPLE -> if (darkTheme) PurpleDarkColorScheme else PurpleLightColorScheme
        ColorVariant.GREEN -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
        ColorVariant.BLUE -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
        ColorVariant.ORANGE -> if (darkTheme) OrangeDarkColorScheme else OrangeLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
