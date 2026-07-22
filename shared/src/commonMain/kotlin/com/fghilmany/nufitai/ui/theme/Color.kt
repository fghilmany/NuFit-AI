package com.fghilmany.nufitai.ui.theme

import androidx.compose.ui.graphics.Color

// Design tokens from /Users/faris/Documents/learn/gym-app/asset/DESIGN.md -- do not hand-tune,
// update the source doc and re-sync instead.
object NuFitColors {
    val Primary = Color(0xFF012D1D)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFF1B4332)
    val OnPrimaryContainer = Color(0xFF86AF99)
    val InversePrimary = Color(0xFFA5D0B9)

    val Secondary = Color(0xFF4C6452)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFCCE6D0)
    val OnSecondaryContainer = Color(0xFF506856)

    val Tertiary = Color(0xFF002D1C)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryContainer = Color(0xFF00452D)
    val OnTertiaryContainer = Color(0xFF64B68E)

    val Error = Color(0xFFBA1A1A)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF93000A)

    val Background = Color(0xFFF8F9FA)
    val OnBackground = Color(0xFF191C1D)
    val Surface = Color(0xFFF8F9FA)
    val OnSurface = Color(0xFF191C1D)
    val SurfaceVariant = Color(0xFFE1E3E4)
    val OnSurfaceVariant = Color(0xFF414844)

    val SurfaceDim = Color(0xFFD9DADB)
    val SurfaceBright = Color(0xFFF8F9FA)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerLow = Color(0xFFF3F4F5)
    val SurfaceContainer = Color(0xFFEDEEEF)
    val SurfaceContainerHigh = Color(0xFFE7E8E9)
    val SurfaceContainerHighest = Color(0xFFE1E3E4)

    val Outline = Color(0xFF717973)
    val OutlineVariant = Color(0xFFC1C8C2)
    val InverseSurface = Color(0xFF2E3132)
    val InverseOnSurface = Color(0xFFF0F1F2)
    val SurfaceTint = Color(0xFF3F6653)

    // "Fixed" tones (DESIGN.md front-matter) -- used for the brighter mint accents (e.g. tips
    // banners) that are deliberately NOT theme-reactive, unlike the roles above.
    val TertiaryFixed = Color(0xFFA0F4C8)
    val OnTertiaryFixed = Color(0xFF002113)
}
