package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LuxuryDarkColorScheme = darkColorScheme(
    primary = ChampagneGold,
    onPrimary = ObsidianBg,
    primaryContainer = GoldContainerDark,
    onPrimaryContainer = ChampagneGoldBright,
    secondary = EmeraldAccent,
    onSecondary = ObsidianBg,
    secondaryContainer = EmeraldContainerDark,
    onSecondaryContainer = EmeraldBright,
    tertiary = AmberWarning,
    onTertiary = ObsidianBg,
    tertiaryContainer = AmberContainerDark,
    onTertiaryContainer = ChampagneGoldBright,
    background = ObsidianBg,
    onBackground = IvoryText,
    surface = ObsidianSurface,
    onSurface = IvoryText,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = SlateTextMuted,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = ObsidianCard,
    surfaceContainerHigh = ObsidianCardHigh,
    outline = ObsidianBorder,
    outlineVariant = ObsidianBorder.copy(alpha = 0.5f),
    error = CrimsonError,
    onError = Color.White,
    errorContainer = CrimsonContainerDark,
    onErrorContainer = Color(0xFFFCA5A5)
)

private val LuxuryLightColorScheme = lightColorScheme(
    primary = AntiqueGold,
    onPrimary = Color.White,
    primaryContainer = GoldContainerLight,
    onPrimaryContainer = Color(0xFF5C4106),
    secondary = DeepEmerald,
    onSecondary = Color.White,
    secondaryContainer = EmeraldContainerLight,
    onSecondaryContainer = Color(0xFF064E3B),
    tertiary = AmberWarning,
    onTertiary = Color.White,
    tertiaryContainer = GoldContainerLight,
    onTertiaryContainer = Color(0xFF6B4E08),
    background = IvoryBg,
    onBackground = InkNavy,
    surface = IvorySurface,
    onSurface = InkNavy,
    surfaceVariant = IvoryCard,
    onSurfaceVariant = InkSlateMuted,
    surfaceContainerLow = IvorySurface,
    surfaceContainer = IvoryCard,
    surfaceContainerHigh = IvoryCardHigh,
    outline = IvoryBorder,
    outlineVariant = IvoryBorder.copy(alpha = 0.6f),
    error = CrimsonError,
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D)
)

@Composable
fun JioPanalTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LuxuryDarkColorScheme else LuxuryLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = JioPanalTypography,
        content = content
    )
}
