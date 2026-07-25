package com.fghilmany.nufitai.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Extreme roundedness per DESIGN.md: 24dp cards, 12dp inputs, pill buttons (M3 default). */
val NuFitShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

val CardCornerRadius = 24.dp
val InputCornerRadius = 12.dp
