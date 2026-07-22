package com.fghilmany.nufitai.presentation.onboarding.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.ui.theme.NuFitColors

/**
 * Shared top app bar for the onboarding flow (PAR-Q, wizard, consult-doctor, body data) --
 * back button (only if [onBack] is provided) + title, matching the Figma header pattern
 * (nodes 12:26, 12:117, 77:146: bg = background, 64dp height, title in Primary color).
 */
@Composable
fun OnboardingTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .height(64.dp)
            .padding(horizontal = if (onBack != null) 8.dp else 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali", tint = NuFitColors.Primary)
            }
        }
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = NuFitColors.Primary,
            modifier = Modifier.weight(1f).padding(start = if (onBack != null) 8.dp else 0.dp),
        )
        trailing?.invoke(this)
    }
}
