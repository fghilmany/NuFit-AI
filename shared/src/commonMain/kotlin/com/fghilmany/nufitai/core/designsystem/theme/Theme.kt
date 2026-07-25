package com.fghilmany.nufitai.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = NuFitColors.Primary,
    onPrimary = NuFitColors.OnPrimary,
    primaryContainer = NuFitColors.PrimaryContainer,
    onPrimaryContainer = NuFitColors.OnPrimaryContainer,
    inversePrimary = NuFitColors.InversePrimary,
    secondary = NuFitColors.Secondary,
    onSecondary = NuFitColors.OnSecondary,
    secondaryContainer = NuFitColors.SecondaryContainer,
    onSecondaryContainer = NuFitColors.OnSecondaryContainer,
    tertiary = NuFitColors.Tertiary,
    onTertiary = NuFitColors.OnTertiary,
    tertiaryContainer = NuFitColors.TertiaryContainer,
    onTertiaryContainer = NuFitColors.OnTertiaryContainer,
    error = NuFitColors.Error,
    onError = NuFitColors.OnError,
    errorContainer = NuFitColors.ErrorContainer,
    onErrorContainer = NuFitColors.OnErrorContainer,
    background = NuFitColors.Background,
    onBackground = NuFitColors.OnBackground,
    surface = NuFitColors.Surface,
    onSurface = NuFitColors.OnSurface,
    surfaceVariant = NuFitColors.SurfaceVariant,
    onSurfaceVariant = NuFitColors.OnSurfaceVariant,
    surfaceDim = NuFitColors.SurfaceDim,
    surfaceBright = NuFitColors.SurfaceBright,
    surfaceContainerLowest = NuFitColors.SurfaceContainerLowest,
    surfaceContainerLow = NuFitColors.SurfaceContainerLow,
    surfaceContainer = NuFitColors.SurfaceContainer,
    surfaceContainerHigh = NuFitColors.SurfaceContainerHigh,
    surfaceContainerHighest = NuFitColors.SurfaceContainerHighest,
    outline = NuFitColors.Outline,
    outlineVariant = NuFitColors.OutlineVariant,
    inverseSurface = NuFitColors.InverseSurface,
    inverseOnSurface = NuFitColors.InverseOnSurface,
    surfaceTint = NuFitColors.SurfaceTint,
)

/**
 * NuFit AI design system (see /asset/DESIGN.md): a "Supportive Mentor" brand -- deep green
 * palette, Plus Jakarta Sans, extreme roundedness (24dp cards, pill buttons). Light scheme only
 * for now; DESIGN.md doesn't define a dark variant yet.
 */
@Composable
fun NuFitTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = nuFitTypography(),
        shapes = NuFitShapes,
        content = content,
    )
}
