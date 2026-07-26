package com.fghilmany.nufitai.presentation.aicustomplan.screen

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
import com.fghilmany.nufitai.core.designsystem.component.AppElevatedCard
import com.fghilmany.nufitai.presentation.aicustomplan.viewmodel.AiCustomPlanGateState
import com.fghilmany.nufitai.presentation.aicustomplan.viewmodel.AiCustomPlanGateViewModel
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.aicustomplan_gate_cta_login
import nufitai.shared.generated.resources.aicustomplan_gate_cta_start_assessment
import nufitai.shared.generated.resources.aicustomplan_gate_cta_subscribe
import nufitai.shared.generated.resources.aicustomplan_gate_guest_body
import nufitai.shared.generated.resources.aicustomplan_gate_guest_title
import nufitai.shared.generated.resources.aicustomplan_gate_needs_assessment_body
import nufitai.shared.generated.resources.aicustomplan_gate_needs_assessment_title
import nufitai.shared.generated.resources.aicustomplan_gate_paywall_body
import nufitai.shared.generated.resources.aicustomplan_gate_paywall_chat_ai_reminder
import nufitai.shared.generated.resources.aicustomplan_gate_paywall_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** P-12 A (UC-1). */
@Composable
fun AiCustomPlanGateScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSubscribe: () -> Unit,
    onNavigateToFullAssessment: () -> Unit,
    onPassThrough: () -> Unit,
    viewModel: AiCustomPlanGateViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is AiCustomPlanGateState.PassThrough) onPassThrough()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val current = state) {
            AiCustomPlanGateState.Loading, AiCustomPlanGateState.PassThrough -> CircularProgressIndicator()
            AiCustomPlanGateState.Guest -> GateCard(
                title = stringResource(Res.string.aicustomplan_gate_guest_title),
                body = stringResource(Res.string.aicustomplan_gate_guest_body),
                ctaLabel = stringResource(Res.string.aicustomplan_gate_cta_login),
                onCta = onNavigateToLogin,
                onDebugSimulateSubscriber = viewModel::debugSimulateSubscriber,
            )
            AiCustomPlanGateState.NonSubscriberPaywall -> GateCard(
                title = stringResource(Res.string.aicustomplan_gate_paywall_title),
                body = stringResource(Res.string.aicustomplan_gate_paywall_body),
                reminder = stringResource(Res.string.aicustomplan_gate_paywall_chat_ai_reminder),
                ctaLabel = stringResource(Res.string.aicustomplan_gate_cta_subscribe),
                onCta = onNavigateToSubscribe,
                onDebugSimulateSubscriber = viewModel::debugSimulateSubscriber,
            )
            AiCustomPlanGateState.NeedsFullAssessment -> GateCard(
                title = stringResource(Res.string.aicustomplan_gate_needs_assessment_title),
                body = stringResource(Res.string.aicustomplan_gate_needs_assessment_body),
                ctaLabel = stringResource(Res.string.aicustomplan_gate_cta_start_assessment),
                onCta = onNavigateToFullAssessment,
            )
            is AiCustomPlanGateState.Error -> Text(current.message)
        }
    }
}

@Composable
private fun GateCard(
    title: String,
    body: String,
    ctaLabel: String,
    onCta: () -> Unit,
    reminder: String? = null,
    onDebugSimulateSubscriber: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(body, style = MaterialTheme.typography.bodyMedium)
        reminder?.let {
            AppElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text(it, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
        AppButton(text = ctaLabel, onClick = onCta, modifier = Modifier.fillMaxWidth())
        // DEBUG ONLY -- remove once real auth/billing land (techspec §9 item 8, ai-custom-plan.md).
        if (onDebugSimulateSubscriber != null) {
            TextButton(onClick = onDebugSimulateSubscriber, modifier = Modifier.fillMaxWidth()) {
                Text("[DEBUG] Simulate subscriber")
            }
        }
    }
}
