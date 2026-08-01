package com.fghilmany.nufitai.presentation.chatai.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.chatai_thinking_label
import org.jetbrains.compose.resources.stringResource

/** Shown while waiting for the assistant's reply (`ChatState.isSending`) -- same avatar/bubble
 * shape as [AssistantMessageBubble] so it reads as "the AI is about to answer here", not a
 * generic spinner. */
@Composable
fun ThinkingBubble(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier.size(28.dp).background(NuFitColors.SecondaryContainer, CircleShape),
        ) {
            Icon(
                Icons.Filled.SmartToy,
                contentDescription = null,
                tint = NuFitColors.OnSecondaryContainer,
                modifier = Modifier.size(16.dp).align(Alignment.Center),
            )
        }
        Row(
            modifier = Modifier.background(NuFitColors.SurfaceContainerLowest, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(Res.string.chatai_thinking_label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TypingDots()
        }
    }
}

@Composable
private fun TypingDots(modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { index ->
            val transition = rememberInfiniteTransition(label = "dot$index")
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, delayMillis = index * 150, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dotAlpha$index",
            )
            Box(
                modifier = Modifier.size(6.dp).alpha(alpha).background(MaterialTheme.colorScheme.onSurfaceVariant, CircleShape),
            )
        }
    }
}
