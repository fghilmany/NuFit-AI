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
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessage
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise

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
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier.size(28.dp).background(NuFitColors.SecondaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.SmartToy, contentDescription = null, tint = NuFitColors.OnSecondaryContainer, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.widthIn(max = 280.dp)) {
            Box(modifier = Modifier.background(NuFitColors.SurfaceContainerLowest, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(message.text, style = MaterialTheme.typography.bodyMedium, color = NuFitColors.OnBackground)
            }
            Text(formatMessageTime(message.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))

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
