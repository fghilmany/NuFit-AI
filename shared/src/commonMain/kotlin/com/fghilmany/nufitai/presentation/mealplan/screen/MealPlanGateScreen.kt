package com.fghilmany.nufitai.presentation.mealplan.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.presentation.mealplan.viewmodel.MealPlanGateState
import com.fghilmany.nufitai.presentation.mealplan.viewmodel.MealPlanGateViewModel
import com.fghilmany.nufitai.presentation.monthlyplan.component.BottomNavBar
import com.fghilmany.nufitai.presentation.monthlyplan.component.NavTab
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.mealplan_gate_cta_complete_data
import nufitai.shared.generated.resources.mealplan_gate_cta_login
import nufitai.shared.generated.resources.mealplan_gate_guest_body
import nufitai.shared.generated.resources.mealplan_gate_guest_title
import nufitai.shared.generated.resources.mealplan_gate_needs_body_data_body
import nufitai.shared.generated.resources.mealplan_gate_needs_body_data_title
import nufitai.shared.generated.resources.mealplan_gate_cta_subscribe
import nufitai.shared.generated.resources.mealplan_gate_paywall_body
import nufitai.shared.generated.resources.mealplan_gate_paywall_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** P-17 A gate (AC-1/AC-2/AC-3, BLOCKING). Mirrors `AiCustomPlanGateScreen`/`ChatGateScreen`. */
@Composable
fun MealPlanGateScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToCompleteBodyData: () -> Unit,
    onPassThrough: () -> Unit,
    onHomeClick: () -> Unit,
    onPlansClick: () -> Unit,
    onChatAiClick: () -> Unit,
    onAccountClick: () -> Unit,
    viewModel: MealPlanGateViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is MealPlanGateState.PassThrough) onPassThrough()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val current = state) {
                MealPlanGateState.Loading, MealPlanGateState.PassThrough -> CircularProgressIndicator()
                MealPlanGateState.Guest -> GateCard(
                    title = stringResource(Res.string.mealplan_gate_guest_title),
                    body = stringResource(Res.string.mealplan_gate_guest_body),
                    ctaLabel = stringResource(Res.string.mealplan_gate_cta_login),
                    onCta = onNavigateToLogin,
                    onDebugSimulateSubscriber = viewModel::debugSimulateSubscriber,
                )
                MealPlanGateState.NonSubscriberPaywall -> GateCard(
                    title = stringResource(Res.string.mealplan_gate_paywall_title),
                    body = stringResource(Res.string.mealplan_gate_paywall_body),
                    ctaLabel = stringResource(Res.string.mealplan_gate_cta_subscribe),
                    onCta = {}, // billing (RevenueCat) doesn't exist yet -- no-op until then, mirrors AiCustomPlanGateScreen's precedent.
                    onDebugSimulateSubscriber = viewModel::debugSimulateSubscriber,
                )
                MealPlanGateState.NeedsBodyData -> GateCard(
                    title = stringResource(Res.string.mealplan_gate_needs_body_data_title),
                    body = stringResource(Res.string.mealplan_gate_needs_body_data_body),
                    ctaLabel = stringResource(Res.string.mealplan_gate_cta_complete_data),
                    onCta = onNavigateToCompleteBodyData,
                )
                is MealPlanGateState.Error -> Text(current.message)
            }
        }

        BottomNavBar(
            activeTab = NavTab.MAKAN,
            onNavigate = { tab ->
                when (tab) {
                    NavTab.HOME -> onHomeClick()
                    NavTab.RENCANA -> onPlansClick()
                    NavTab.CHAT_AI -> onChatAiClick()
                    NavTab.AKUN -> onAccountClick()
                    else -> Unit
                }
            },
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}

@Composable
private fun GateCard(
    title: String,
    body: String,
    ctaLabel: String,
    onCta: () -> Unit,
    onDebugSimulateSubscriber: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(body, style = MaterialTheme.typography.bodyMedium)
        AppButton(text = ctaLabel, onClick = onCta, modifier = Modifier.fillMaxWidth())
        // DEBUG ONLY -- remove once real auth/billing land (mirrors AiCustomPlanGateScreen's precedent).
        if (onDebugSimulateSubscriber != null) {
            TextButton(onClick = onDebugSimulateSubscriber, modifier = Modifier.fillMaxWidth()) {
                Text("[DEBUG] Simulate subscriber")
            }
        }
    }
}
