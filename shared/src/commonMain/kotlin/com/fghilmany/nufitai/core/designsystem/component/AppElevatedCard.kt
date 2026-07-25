package com.fghilmany.nufitai.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.theme.CardCornerRadius
import com.fghilmany.nufitai.core.designsystem.theme.NuFitTheme

/**
 * Atom: the elevated/bordered/clickable card variant -- wraps Material3 [Card] rather than
 * duplicating its elevation+ripple+border machinery. Sibling to [AppCard] (flat tinted box):
 * use this one when the source screen used a raw `Card`/`Card(onClick = ...)`, use [AppCard]
 * when it used a plain `Row/Column.background(...)`.
 */
@Composable
fun AppElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    shape: Shape = RoundedCornerShape(CardCornerRadius),
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(containerColor = backgroundColor)
    val cardElevation = CardDefaults.cardElevation(defaultElevation = elevation)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors, elevation = cardElevation, border = border, content = content)
    } else {
        Card(modifier = modifier, shape = shape, colors = colors, elevation = cardElevation, border = border, content = content)
    }
}

@Preview
@Composable
private fun AppElevatedCardPreview() {
    NuFitTheme {
        AppElevatedCard(onClick = {}) {
            Text("AppElevatedCard content")
        }
    }
}
