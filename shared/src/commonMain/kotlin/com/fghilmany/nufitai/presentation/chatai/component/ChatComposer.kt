package com.fghilmany.nufitai.presentation.chatai.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppTextField
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.chatai_composer_placeholder
import nufitai.shared.generated.resources.chatai_composer_send_content_description
import org.jetbrains.compose.resources.stringResource

/**
 * Screenshot §2 (chat-ai.md) -- rounded pill text field + circular send button.
 * AC-3, BLOCKING: [fieldEnabled] false only for quota-exhausted (the whole composer disables
 * with the upsell shown via [QuotaBanner], per §8). [sendEnabled] additionally gates just the
 * send button (e.g. while a send is in flight, §9 item 10's double-tap guard) -- the field
 * itself stays typable so the offline case (§8: "composer stays enabled... honest feedback")
 * never blocks the user from drafting.
 */
@Composable
fun ChatComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    sendEnabled: Boolean,
    fieldEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = stringResource(Res.string.chatai_composer_placeholder),
            enabled = fieldEnabled,
            modifier = Modifier.weight(1f),
        )
        val canSend = sendEnabled && fieldEnabled
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(if (canSend) NuFitColors.Primary else NuFitColors.SurfaceContainerHigh, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = onSend, enabled = canSend && value.isNotBlank()) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = stringResource(Res.string.chatai_composer_send_content_description),
                    tint = if (canSend) NuFitColors.OnPrimary else NuFitColors.OnSurfaceVariant,
                )
            }
        }
    }
}
