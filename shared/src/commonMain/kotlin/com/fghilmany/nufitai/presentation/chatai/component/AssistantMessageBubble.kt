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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.chatai_message_copy_content_description
import nufitai.shared.generated.resources.chatai_message_share_content_description
import org.jetbrains.compose.resources.stringResource

/** Left-aligned white/card bubble with a small bot avatar (screenshot §2, chat-ai.md); may embed an [ExerciseSuggestionCard]. */
@Composable
fun AssistantMessageBubble(
    message: ChatMessage,
    resolvedExercise: Exercise?,
    applyState: ApplyCardState,
    onApplyToPlan: () -> Unit,
    onAskMore: () -> Unit,
    onExerciseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current
    val shareLauncher = remember { ShareLauncher() }
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier.size(28.dp).background(NuFitColors.SecondaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.SmartToy, contentDescription = null, tint = NuFitColors.OnSecondaryContainer, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.widthIn(max = 280.dp)) {
            // Guard-blocked/redirected replies (chatGuard.ts, not a real model answer) get a
            // slightly darker bubble so the user can tell them apart from an actual AI answer.
            val bubbleColor = if (message.isPolicyResponse) NuFitColors.SurfaceContainerLow else NuFitColors.SurfaceContainerLowest
            Box(modifier = Modifier.background(bubbleColor, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(parseSimpleMarkdown(message.text), style = MaterialTheme.typography.bodyMedium, color = NuFitColors.OnBackground)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                Text(formatMessageTime(message.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(onClick = { clipboardManager.setText(AnnotatedString(message.text)) }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        contentDescription = stringResource(Res.string.chatai_message_copy_content_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
                IconButton(onClick = { shareLauncher.shareText(message.text) }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = stringResource(Res.string.chatai_message_share_content_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            if (message.suggestion != null && resolvedExercise != null) {
                ExerciseSuggestionCard(
                    exercise = resolvedExercise,
                    replacesExerciseId = message.suggestion.replacesExerciseId,
                    applyState = applyState,
                    onApplyToPlan = onApplyToPlan,
                    onAskMore = onAskMore,
                    onExerciseClick = onExerciseClick,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
