package com.fghilmany.nufitai.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.common_action_retry
import org.jetbrains.compose.resources.stringResource

/**
 * Dev report (18 Nov 2026): a load failure replaced the whole screen with a bare `Text(message)`
 * -- no retry, user stuck. One shared view instead of 17 copies of the same Box/Column/Button.
 * Not used for inline form-submit errors (those stay next to the field/button they belong to).
 */
@Composable
fun ErrorRetryView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
            AppButton(text = stringResource(Res.string.common_action_retry), onClick = onRetry)
        }
    }
}
