package com.fghilmany.nufitai.presentation.chatai.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Screenshot §2 (chat-ai.md) -- horizontally scrollable pill row under the latest AI message; tapping sends the chip text as the next user message. */
@Composable
fun SuggestedReplyRow(replies: List<String>, onReplyClick: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(replies) { reply ->
            AssistChip(onClick = { onReplyClick(reply) }, label = { Text(reply) })
        }
    }
}
