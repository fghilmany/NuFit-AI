package com.fghilmany.nufitai.core.designsystem.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.fghilmany.nufitai.core.designsystem.theme.InputCornerRadius
import com.fghilmany.nufitai.core.designsystem.theme.NuFitTheme

/** Atom: App-prefixed text input, rounded per DESIGN.md's 12dp input radius. */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        enabled = enabled,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(InputCornerRadius),
    )
}

@Preview
@Composable
private fun AppTextFieldPreview() {
    NuFitTheme {
        AppTextField(value = "170", onValueChange = {}, label = "Tinggi (cm)")
    }
}
