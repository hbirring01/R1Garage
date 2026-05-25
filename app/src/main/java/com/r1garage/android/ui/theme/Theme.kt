package com.r1garage.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RivianColors = darkColorScheme(
    primary = RivianYellow,
    onPrimary = RivianBlack,
    primaryContainer = RivianYellowDim,
    onPrimaryContainer = RivianBlack,
    secondary = RivianTextPrimary,
    onSecondary = RivianBlack,
    secondaryContainer = RivianSurfaceHigh,
    onSecondaryContainer = RivianTextPrimary,
    tertiary = RivianYellow,
    onTertiary = RivianBlack,
    tertiaryContainer = RivianSurfaceHigh,
    onTertiaryContainer = RivianTextPrimary,
    background = RivianBlack,
    onBackground = RivianTextPrimary,
    surface = RivianAlmostBlack,
    onSurface = RivianTextPrimary,
    surfaceVariant = RivianSurface,
    onSurfaceVariant = RivianTextSecondary,
    surfaceContainer = RivianSurface,
    surfaceContainerHigh = RivianSurfaceHigh,
    surfaceContainerHighest = RivianSurfaceHighest,
    surfaceContainerLow = RivianAlmostBlack,
    surfaceContainerLowest = RivianBlack,
    outline = RivianOutline,
    outlineVariant = RivianOutlineDim,
    error = RivianError,
    onError = RivianBlack,
    inverseSurface = RivianTextPrimary,
    inverseOnSurface = RivianBlack,
    inversePrimary = RivianYellowDim,
    scrim = RivianBlack,
)

/**
 * Single, force-dark Rivian-inspired theme. Dynamic color is intentionally
 * NOT used — the brand is the brand.
 */
@Composable
fun R1GarageTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = RivianColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
