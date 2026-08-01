package com.fghilmany.nufitai.presentation.chatai.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.chatai.entity.ChatQuota
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.chatai_quota_exhausted_body
import nufitai.shared.generated.resources.chatai_quota_exhausted_cta
import nufitai.shared.generated.resources.chatai_quota_label
import org.jetbrains.compose.resources.stringResource

/**
 * AC-3, BLOCKING (chat-ai.md) -- "X/5 chat hari ini" shown always; when exhausted, upsell copy +
 * CTA replaces the plain label (composer itself disables separately in `ChatScreen`). Server-
 * authoritative only: never computed/decremented client-side (§5's business rule).
 */
@Composable
fun QuotaBanner(quota: ChatQuota, isExhausted: Boolean, onUpsellClick: () -> Unit, modifier: Modifier = Modifier) {
    if (isExhausted) {
        AppCard(
            modifier = modifier.fillMaxWidth(),
            backgroundColor = NuFitColors.WarningContainer,
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.chatai_quota_exhausted_body), style = MaterialTheme.typography.bodySmall, color = NuFitColors.OnWarningContainerBody, modifier = Modifier.weight(1f))
                TextButton(onClick = onUpsellClick) {
                    Text(stringResource(Res.string.chatai_quota_exhausted_cta))
                }
            }
        }
    } else {
        Text(
            stringResource(Res.string.chatai_quota_label, quota.used, quota.max),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
    }
}
