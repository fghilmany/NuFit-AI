package com.fghilmany.nufitai.presentation.chatai.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.core.share.ShareLauncher
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessage
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessageStatus
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.chatai_message_copy_content_description
import nufitai.shared.generated.resources.chatai_message_failed_label
import nufitai.shared.generated.resources.chatai_message_retry_cta
import nufitai.shared.generated.resources.chatai_message_sending_label
import nufitai.shared.generated.resources.chatai_message_share_content_description
import org.jetbrains.compose.resources.stringResource

/** Right-aligned dark-green bubble (screenshot §2, chat-ai.md). AC-6/AC-7 -- FAILED shows an inline retry tap target rather than vanishing. */
@Composable
fun UserMessageBubble(message: ChatMessage, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val clipboardManager = LocalClipboardManager.current
    val shareLauncher = remember { ShareLauncher() }
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.widthIn(max = 280.dp)) {
            Box(
                modifier = Modifier
                    .background(NuFitColors.Primary, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(message.text, color = NuFitColors.OnPrimary, style = MaterialTheme.typography.bodyMedium)
            }
            StatusRow(
                message = message,
                onRetry = onRetry,
                onCopy = { clipboardManager.setText(AnnotatedString(message.text)) },
                onShare = { shareLauncher.shareText(message.text) },
            )
        }
    }
}

@Composable
private fun StatusRow(message: ChatMessage, onRetry: () -> Unit, onCopy: () -> Unit, onShare: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 2.dp),
    ) {
        when (message.status) {
            ChatMessageStatus.SENDING -> Text(stringResource(Res.string.chatai_message_sending_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ChatMessageStatus.FAILED -> {
                Text(stringResource(Res.string.chatai_message_failed_label), style = MaterialTheme.typography.labelSmall, color = NuFitColors.Error)
                TextButton(onClick = onRetry) {
                    Text(stringResource(Res.string.chatai_message_retry_cta), style = MaterialTheme.typography.labelSmall)
                }
            }
            ChatMessageStatus.SENT -> {
                Text(formatMessageTime(message.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        contentDescription = stringResource(Res.string.chatai_message_copy_content_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
                IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = stringResource(Res.string.chatai_message_share_content_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}
