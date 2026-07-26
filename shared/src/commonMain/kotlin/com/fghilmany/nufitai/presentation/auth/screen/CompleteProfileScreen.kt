package com.fghilmany.nufitai.presentation.auth.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.fghilmany.nufitai.domain.fullassessment.entity.Gender
import com.fghilmany.nufitai.presentation.auth.viewmodel.CompleteProfileState
import com.fghilmany.nufitai.presentation.auth.viewmodel.CompleteProfileViewModel
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.auth_complete_profile_age_label
import nufitai.shared.generated.resources.auth_complete_profile_gender_label
import nufitai.shared.generated.resources.auth_complete_profile_subtitle
import nufitai.shared.generated.resources.auth_complete_profile_title
import nufitai.shared.generated.resources.common_action_save
import nufitai.shared.generated.resources.fullassessment_interview_gender_female
import nufitai.shared.generated.resources.fullassessment_interview_gender_male
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** "Lengkapi Profil" (techspec §6, auth-sync.md), reuses the existing [Gender] type. */
@Composable
fun CompleteProfileScreen(onCompleted: () -> Unit, viewModel: CompleteProfileViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is CompleteProfileState.Saved) onCompleted()
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(Res.string.auth_complete_profile_title), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(Res.string.auth_complete_profile_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val editing = state as? CompleteProfileState.Editing
        if (editing != null) {
            AppTextField(
                value = editing.ageInput,
                onValueChange = viewModel::onAgeChanged,
                label = stringResource(Res.string.auth_complete_profile_age_label),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            Text(stringResource(Res.string.auth_complete_profile_gender_label), style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                GenderToggle(
                    label = stringResource(Res.string.fullassessment_interview_gender_male),
                    isSelected = editing.gender == Gender.MALE,
                    modifier = Modifier.fillMaxWidth().padding(end = 0.dp),
                    onClick = { viewModel.onGenderChanged(Gender.MALE) },
                )
                GenderToggle(
                    label = stringResource(Res.string.fullassessment_interview_gender_female),
                    isSelected = editing.gender == Gender.FEMALE,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.onGenderChanged(Gender.FEMALE) },
                )
            }

            editing.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            AppButton(
                text = stringResource(Res.string.common_action_save),
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun GenderToggle(label: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (isSelected) {
        Button(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(12.dp)) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors()) {
            Text(label)
        }
    }
}
