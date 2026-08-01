package com.fghilmany.nufitai.presentation.chatai.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.presentation.chatai.viewmodel.ChatGateState
import com.fghilmany.nufitai.presentation.chatai.viewmodel.ChatGateViewModel
import com.fghilmany.nufitai.presentation.monthlyplan.component.BottomNavBar
import com.fghilmany.nufitai.presentation.monthlyplan.component.NavTab
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.chatai_gate_cta_login
import nufitai.shared.generated.resources.chatai_gate_guest_body
import nufitai.shared.generated.resources.chatai_gate_guest_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** P-16 A (AC-1, BLOCKING). Mirrors `AiCustomPlanGateScreen`'s `GateCard` shape; tab-switch destination like Home/MyPlans/Account. */
@Composable
fun ChatGateScreen(
    onNavigateToLogin: () -> Unit,
    onPassThrough: () -> Unit,
    onHomeClick: () -> Unit,
    onPlansClick: () -> Unit,
    onAccountClick: () -> Unit,
    onMealPlanClick: () -> Unit,
    viewModel: ChatGateViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is ChatGateState.PassThrough) onPassThrough()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val current = state) {
                ChatGateState.Loading, ChatGateState.PassThrough -> CircularProgressIndicator()
                ChatGateState.Guest -> GateCard(
                    title = stringResource(Res.string.chatai_gate_guest_title),
                    body = stringResource(Res.string.chatai_gate_guest_body),
                    ctaLabel = stringResource(Res.string.chatai_gate_cta_login),
                    onCta = onNavigateToLogin,
                )
                is ChatGateState.Error -> Text(current.message)
            }
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
private fun GateCard(title: String, body: String, ctaLabel: String, onCta: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(body, style = MaterialTheme.typography.bodyMedium)
        AppButton(text = ctaLabel, onClick = onCta, modifier = Modifier.fillMaxWidth())
    }
}
