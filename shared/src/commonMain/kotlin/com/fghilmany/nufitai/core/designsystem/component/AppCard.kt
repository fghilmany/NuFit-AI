package com.fghilmany.nufitai.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.theme.CardCornerRadius
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.core.designsystem.theme.NuFitTheme

/**
 * Atom: the rounded, tinted-background card shape repeated across every feature (summary
 * cells, banners, flag cards, plan cards) -- was hand-rolled per call site as
 * `Row/Column.background(color, RoundedCornerShape(24.dp)).padding(...)` before this existed.
 * Fully state-hoisted; caller supplies color/shape/padding, this owns only the layout shell.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = NuFitColors.SurfaceContainerLowest,
    shape: Shape = RoundedCornerShape(CardCornerRadius),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .background(backgroundColor, shape)
            .padding(contentPadding),
        content = content,
    )
}

@Preview
@Composable
private fun AppCardPreview() {
    NuFitTheme {
        AppCard(backgroundColor = NuFitColors.SecondaryContainer) {
            Text("AppCard content")
        }
    }
}
