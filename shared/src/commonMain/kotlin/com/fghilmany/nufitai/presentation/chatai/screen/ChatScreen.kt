package com.fghilmany.nufitai.presentation.chatai.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessage
import com.fghilmany.nufitai.domain.chatai.entity.ChatMessageRole
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.presentation.chatai.component.ApplyCardState
import com.fghilmany.nufitai.presentation.chatai.component.AssistantMessageBubble
import com.fghilmany.nufitai.presentation.chatai.component.ChatComposer
import com.fghilmany.nufitai.presentation.chatai.component.QuotaBanner
import com.fghilmany.nufitai.presentation.chatai.component.SuggestedReplyRow
import com.fghilmany.nufitai.presentation.chatai.component.UserMessageBubble
import com.fghilmany.nufitai.presentation.chatai.viewmodel.ApplySuggestionResult
import com.fghilmany.nufitai.presentation.chatai.viewmodel.ChatViewModel
import com.fghilmany.nufitai.presentation.monthlyplan.component.BottomNavBar
import com.fghilmany.nufitai.presentation.monthlyplan.component.NavTab
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.chatai_empty_starter_1
import nufitai.shared.generated.resources.chatai_empty_starter_2
import nufitai.shared.generated.resources.chatai_empty_starter_3
import nufitai.shared.generated.resources.chatai_empty_state_title
import nufitai.shared.generated.resources.chatai_overflow_clear_conversation
import nufitai.shared.generated.resources.chatai_overflow_content_description
import nufitai.shared.generated.resources.chatai_top_bar_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * P-16 (techspec chat-ai.md). Tab-switch destination (keputusan #12) -- no back arrow, unlike
 * the screenshot's original context, since this is reached via the bottom nav like Home/MyPlans/
 * Account, not pushed onto a stack (assumption, no design signal either way).
 */
@Composable
fun ChatScreen(
    onHomeClick: () -> Unit,
    onPlansClick: () -> Unit,
    onAccountClick: () -> Unit,
    onMealPlanClick: () -> Unit,
    onExerciseClick: (exerciseId: String) -> Unit,
    viewModel: ChatViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar()

            state.quota?.let { quota ->
                QuotaBanner(
                    quota = quota,
                    isExhausted = state.isQuotaExhausted,
                    onUpsellClick = {}, // billing (RevenueCat) doesn't exist yet -- no-op until then, mirrors AiCustomPlanGateScreen's onNavigateToSubscribe precedent.
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            if (state.messages.isEmpty()) {
                EmptyState(
                    onStarterClick = viewModel::onSuggestedReplyClick,
                    modifier = Modifier.weight(1f),
                )
            } else {
                MessageList(
                    messages = state.messages,
                    resolvedExercises = state.resolvedExercises,
                    applyResult = state.applyResult,
                    onRetry = viewModel::onRetry,
                    onApplyToPlan = { messageId, replacesId, substituteId -> viewModel.onApplyToPlan(messageId, replacesId, substituteId) },
                    // "Tanya Lagi" -- no design/product spec for its behavior beyond "keep chatting"; the
                    // composer is already right below the card, so this is a no-op focus hint only.
                    onAskMore = {},
                    onExerciseClick = onExerciseClick,
                    modifier = Modifier.weight(1f),
                )

                val latest = state.messages.lastOrNull()
                if (latest != null && latest.role == ChatMessageRole.ASSISTANT && latest.suggestedReplies.isNotEmpty()) {
                    SuggestedReplyRow(
                        replies = latest.suggestedReplies,
                        onReplyClick = viewModel::onSuggestedReplyClick,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
            }

            ChatComposer(
                value = state.draft,
                onValueChange = viewModel::onDraftChange,
                onSend = viewModel::onSend,
                sendEnabled = !state.isSending,
                fieldEnabled = !state.isQuotaExhausted,
                modifier = Modifier.padding(bottom = 96.dp), // clears BottomNavBar, mirrors HomeScreen/AccountScreen's contentPadding
            )
        }

        BottomNavBar(
            activeTab = NavTab.CHAT_AI,
            onNavigate = { tab ->
                when (tab) {
                    NavTab.HOME -> onHomeClick()
                    NavTab.RENCANA -> onPlansClick()
                    NavTab.AKUN -> onAccountClick()
                    NavTab.MAKAN -> onMealPlanClick()
                    else -> Unit
                }
            },
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}

@Composable
private fun TopBar() {
    var overflowExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(Res.string.chatai_top_bar_title), style = MaterialTheme.typography.titleLarge)
        Box {
            IconButton(onClick = { overflowExpanded = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(Res.string.chatai_overflow_content_description))
            }
            // §9 item 13 -- placeholder only, full scope undefined by design; no-op until a real "clear conversation" usecase is scoped.
            DropdownMenu(expanded = overflowExpanded, onDismissRequest = { overflowExpanded = false }) {
                DropdownMenuItem(text = { Text(stringResource(Res.string.chatai_overflow_clear_conversation)) }, onClick = { overflowExpanded = false })
            }
        }
    }
}

@Composable
private fun EmptyState(onStarterClick: (String) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(24.dp)) {
            Text(stringResource(Res.string.chatai_empty_state_title), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            SuggestedReplyRow(
                replies = listOf(
                    stringResource(Res.string.chatai_empty_starter_1),
                    stringResource(Res.string.chatai_empty_starter_2),
                    stringResource(Res.string.chatai_empty_starter_3),
                ),
                onReplyClick = onStarterClick,
            )
        }
    }
}

@Composable
private fun MessageList(
    messages: List<ChatMessage>,
    resolvedExercises: Map<String, Exercise>,
    applyResult: ApplySuggestionResult?,
    onRetry: (ChatMessage) -> Unit,
    onApplyToPlan: (messageId: String, replacesExerciseId: String, substituteExerciseId: String) -> Unit,
    onAskMore: () -> Unit,
    onExerciseClick: (exerciseId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(messages, key = { it.id }) { message ->
            when (message.role) {
                ChatMessageRole.USER -> UserMessageBubble(message = message, onRetry = { onRetry(message) })
                ChatMessageRole.ASSISTANT -> {
                    val suggestion = message.suggestion
                    val resolvedExercise = suggestion?.let { resolvedExercises[it.exerciseId] }
                    val applyState = when {
                        applyResult is ApplySuggestionResult.Success && applyResult.messageId == message.id -> ApplyCardState.Applied
                        applyResult is ApplySuggestionResult.Error && applyResult.messageId == message.id -> ApplyCardState.Failed
                        else -> ApplyCardState.Idle
                    }
                    AssistantMessageBubble(
                        message = message,
                        resolvedExercise = resolvedExercise,
                        applyState = applyState,
                        onApplyToPlan = {
                            if (suggestion?.replacesExerciseId != null) {
                                onApplyToPlan(message.id, suggestion.replacesExerciseId, suggestion.exerciseId)
                            }
                        },
                        onAskMore = onAskMore,
                        onExerciseClick = { if (resolvedExercise != null) onExerciseClick(resolvedExercise.id) },
                    )
                }
            }
        }
    }
}
