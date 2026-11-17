package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkCyan,
    onPrimary = BlueprintNavyDark,
    primaryContainer = Color(0xFF15385A),
    onPrimaryContainer = Color(0xFFD6EEFF),
    secondary = DarkTeal,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFFCCFBF1),
    background = DarkCanvas,
    onBackground = DarkInkPrimary,
    surface = DarkSurface,
    onSurface = DarkInkPrimary,
    surfaceVariant = DarkSurfaceAlt,
    onSurfaceVariant = DarkInkSecondary,
    outline = DarkBorder,
    outlineVariant = Color(0xFF1E354E),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
)

private val LightColorScheme = lightColorScheme(
    primary = BlueprintNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEBFA),
    onPrimaryContainer = BlueprintNavy,
    secondary = BlueprintCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF075985),
    tertiary = BlueprintTeal,
    onTertiary = Color.White,
    background = DraftingPaper,
    onBackground = InkPrimary,
    surface = DraftingSurface,
    onSurface = InkPrimary,
    surfaceVariant = DraftingSurfaceAlt,
    onSurfaceVariant = InkSecondary,
    outline = DraftingBorder,
    outlineVariant = Color(0xFFE2E8F0),
    error = BlueprintError,
    onError = Color.White,
)

@Composable
fun MeasureSheetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
