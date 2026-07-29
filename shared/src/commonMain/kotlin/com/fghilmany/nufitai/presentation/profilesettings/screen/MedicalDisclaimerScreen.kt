package com.fghilmany.nufitai.presentation.profilesettings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.presentation.onboarding.component.CheckboxRow
import com.fghilmany.nufitai.presentation.onboarding.component.OnboardingTopBar
import com.fghilmany.nufitai.presentation.profilesettings.viewmodel.MedicalDisclaimerState
import com.fghilmany.nufitai.presentation.profilesettings.viewmodel.MedicalDisclaimerViewModel
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.common_action_continue
import nufitai.shared.generated.resources.profilesettings_disclaimer_ack_label
import nufitai.shared.generated.resources.profilesettings_disclaimer_body
import nufitai.shared.generated.resources.profilesettings_disclaimer_title
import nufitai.shared.generated.resources.profilesettings_disclaimer_top_bar_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * techspec profile-settings.md §8 -- when [isFirstRun], mirrors `DoctorConsultScreen`'s
 * acknowledge-to-continue gate (no back-button skip); when re-read from Informasi, it's a plain
 * read-only screen and [onDone] is just "close", no acknowledgement required again.
 */
@Composable
fun MedicalDisclaimerScreen(
    isFirstRun: Boolean,
    onDone: () -> Unit,
    viewModel: MedicalDisclaimerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is MedicalDisclaimerState.Acknowledged) onDone()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OnboardingTopBar(
            title = stringResource(Res.string.profilesettings_disclaimer_top_bar_title),
            // No back button on first-run -- the gate cannot be skipped (techspec §8).
            onBack = if (isFirstRun) null else onDone,
        )

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(Res.string.profilesettings_disclaimer_title), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(Res.string.profilesettings_disclaimer_body), style = MaterialTheme.typography.bodyMedium)

            if (isFirstRun) {
                val content = state as? MedicalDisclaimerState.Content ?: return@Column
                CheckboxRow(
                    checked = content.acknowledged,
                    label = stringResource(Res.string.profilesettings_disclaimer_ack_label),
                    onToggle = viewModel::toggleAcknowledge,
                )
                AppButton(
                    text = stringResource(Res.string.common_action_continue),
                    onClick = viewModel::continueClicked,
                    enabled = content.acknowledged,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
