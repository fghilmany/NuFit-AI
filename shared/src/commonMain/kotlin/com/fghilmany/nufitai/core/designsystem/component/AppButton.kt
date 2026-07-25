package com.fghilmany.nufitai.core.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import com.fghilmany.nufitai.core.designsystem.theme.NuFitTheme

/** Atom: App-prefixed wrapper over Material3 Button, slot API for icon+text combinations. */
@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable RowScope.() -> Unit,
) {
    if (shape != null) {
        Button(onClick = onClick, modifier = modifier, enabled = enabled, shape = shape, colors = colors, content = content)
    } else {
        Button(onClick = onClick, modifier = modifier, enabled = enabled, colors = colors, content = content)
    }
}

/** Convenience overload for the common single-label case. */
@Composable
fun AppButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    AppButton(onClick = onClick, modifier = modifier, enabled = enabled) { Text(text) }
}

@Preview
@Composable
private fun AppButtonPreview() {
    NuFitTheme {
        AppButton(text = "Lanjutkan", onClick = {})
    }
}
