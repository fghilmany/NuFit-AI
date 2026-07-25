package com.fghilmany.nufitai.presentation.onboarding.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.core.designsystem.component.AppTextField
import com.fghilmany.nufitai.presentation.onboarding.component.OnboardingTopBar
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.BodyDataEvent
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.BodyDataState
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.BodyDataViewModel
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.common_action_save
import nufitai.shared.generated.resources.common_action_skip
import nufitai.shared.generated.resources.onboarding_bodydata_body
import nufitai.shared.generated.resources.onboarding_bodydata_heading
import nufitai.shared.generated.resources.onboarding_bodydata_height_label
import nufitai.shared.generated.resources.onboarding_bodydata_top_bar_title
import nufitai.shared.generated.resources.onboarding_bodydata_weight_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BodyDataScreen(
    onDone: () -> Unit,
    viewModel: BodyDataViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is BodyDataState.Saved || state is BodyDataState.Skipped) onDone()
    }

    val content = state as? BodyDataState.Content ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        OnboardingTopBar(title = stringResource(Res.string.onboarding_bodydata_top_bar_title))

        Column(
            modifier = Modifier.weight(1f).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(Res.string.onboarding_bodydata_heading), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(Res.string.onboarding_bodydata_body),
                style = MaterialTheme.typography.bodyMedium,
            )

            AppTextField(
                value = content.heightCm,
                onValueChange = { viewModel.onEvent(BodyDataEvent.UpdateHeight(it)) },
                label = stringResource(Res.string.onboarding_bodydata_height_label),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            AppTextField(
                value = content.weightKg,
                onValueChange = { viewModel.onEvent(BodyDataEvent.UpdateWeight(it)) },
                label = stringResource(Res.string.onboarding_bodydata_weight_label),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            AppButton(
                onClick = { viewModel.onEvent(BodyDataEvent.Save) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.common_action_save))
            }
            OutlinedButton(
                onClick = { viewModel.onEvent(BodyDataEvent.Skip) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.common_action_skip))
            }
        }
    }
}
