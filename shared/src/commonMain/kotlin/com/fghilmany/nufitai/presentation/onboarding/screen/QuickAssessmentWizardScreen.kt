package com.fghilmany.nufitai.presentation.onboarding.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.domain.onboarding.entity.EquipmentType
import com.fghilmany.nufitai.presentation.onboarding.component.CardioOption
import com.fghilmany.nufitai.presentation.onboarding.component.EquipmentGridCard
import com.fghilmany.nufitai.presentation.onboarding.component.EquipmentGridOptions
import com.fghilmany.nufitai.presentation.onboarding.component.EquipmentRowCard
import com.fghilmany.nufitai.presentation.onboarding.component.ExperienceOptions
import com.fghilmany.nufitai.presentation.onboarding.component.FrequencyOptions
import com.fghilmany.nufitai.presentation.onboarding.component.GoalOptions
import com.fghilmany.nufitai.presentation.onboarding.component.OnboardingTopBar
import com.fghilmany.nufitai.presentation.onboarding.component.OptionCard
import com.fghilmany.nufitai.presentation.onboarding.component.ProgressBar
import com.fghilmany.nufitai.presentation.onboarding.component.SelectAllIcon
import com.fghilmany.nufitai.presentation.onboarding.component.SplitOptions
import com.fghilmany.nufitai.presentation.onboarding.component.TipsBanner
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.QUICK_ASSESSMENT_TOTAL_STEPS
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.QuickAssessmentEvent
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.QuickAssessmentState
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.QuickAssessmentViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun QuickAssessmentWizardScreen(
    onCompleted: () -> Unit,
    viewModel: QuickAssessmentViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is QuickAssessmentState.Completed) onCompleted()
    }

    when (val current = state) {
        is QuickAssessmentState.Step -> WizardStepContent(
            step = current,
            onEvent = viewModel::onEvent,
        )
        QuickAssessmentState.Generating -> LoadingBox()
        is QuickAssessmentState.Completed -> LoadingBox() // brief -- LaunchedEffect above navigates away
        is QuickAssessmentState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(current.message) }
    }
}

@Composable
private fun WizardStepContent(step: QuickAssessmentState.Step, onEvent: (QuickAssessmentEvent) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        OnboardingTopBar(
            title = "Quick Assessment",
            onBack = if (step.canGoBack) {
                { onEvent(QuickAssessmentEvent.PreviousStep) }
            } else {
                null
            },
        )

        Column(modifier = Modifier.weight(1f).padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Langkah ${step.stepIndex} dari $QUICK_ASSESSMENT_TOTAL_STEPS",
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    "${(step.stepIndex * 100 / QUICK_ASSESSMENT_TOTAL_STEPS)}% Selesai",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            ProgressBar(
                progress = step.stepIndex / QUICK_ASSESSMENT_TOTAL_STEPS.toFloat(),
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when (step.stepIndex) {
                    1 -> items(ExperienceOptions) { (value, copy) ->
                        OptionCard(
                            icon = copy.icon,
                            title = copy.title,
                            subtitle = copy.subtitle,
                            isSelected = step.experience == value,
                            onClick = { onEvent(QuickAssessmentEvent.SelectExperience(value)) },
                        )
                    }
                    2 -> items(GoalOptions) { (value, copy) ->
                        OptionCard(
                            icon = copy.icon,
                            title = copy.title,
                            subtitle = copy.subtitle,
                            isSelected = step.goal == value,
                            onClick = { onEvent(QuickAssessmentEvent.SelectGoal(value)) },
                        )
                    }
                    3 -> equipmentStep(step, onEvent)
                    4 -> items(FrequencyOptions) { (value, copy) ->
                        OptionCard(
                            icon = copy.icon,
                            title = copy.title,
                            subtitle = copy.subtitle,
                            isSelected = step.frequency == value,
                            onClick = { onEvent(QuickAssessmentEvent.SelectFrequency(value)) },
                        )
                    }
                    5 -> items(SplitOptions) { (value, copy) ->
                        OptionCard(
                            icon = copy.icon,
                            title = copy.title,
                            subtitle = copy.subtitle,
                            isSelected = step.splitPreference == value,
                            onClick = { onEvent(QuickAssessmentEvent.SelectSplit(value)) },
                        )
                    }
                }
            }

            WizardNavigationButtons(step, onEvent)
        }
    }
}

private fun LazyListScope.equipmentStep(
    step: QuickAssessmentState.Step,
    onEvent: (QuickAssessmentEvent) -> Unit,
) {
    val allSelected = EquipmentType.entries.filterNot { it == EquipmentType.CARDIO }.all { it in step.equipment }

    item {
        EquipmentRowCard(
            icon = SelectAllIcon,
            title = "Pilih Semua",
            subtitle = "Saya punya akses gym lengkap",
            isSelected = allSelected,
            onClick = { onEvent(QuickAssessmentEvent.SelectAllEquipment) },
        )
    }
    items(EquipmentGridOptions.chunked(2)) { pair ->
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            pair.forEach { (value, copy) ->
                EquipmentGridCard(
                    icon = copy.icon,
                    title = copy.title,
                    subtitle = copy.subtitle,
                    isSelected = value in step.equipment,
                    onClick = { onEvent(QuickAssessmentEvent.ToggleEquipment(value)) },
                    modifier = Modifier.weight(1f),
                )
            }
            if (pair.size < 2) {
                Spacer(Modifier.weight(1f))
            }
        }
    }
    item {
        EquipmentRowCard(
            icon = CardioOption.icon,
            title = CardioOption.title,
            subtitle = CardioOption.subtitle,
            isSelected = EquipmentType.CARDIO in step.equipment,
            onClick = { onEvent(QuickAssessmentEvent.ToggleEquipment(EquipmentType.CARDIO)) },
        )
    }
    item {
        TipsBanner(
            icon = Icons.Filled.SmartToy,
            title = "Tips NuFit AI",
            body = "Jika kamu latihan di rumah tanpa alat, pilih Bodyweight saja. Kami akan " +
                "mencarikan gerakan yang efektif hanya dengan berat tubuhmu!",
        )
    }
}

@Composable
private fun WizardNavigationButtons(step: QuickAssessmentState.Step, onEvent: (QuickAssessmentEvent) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (step.canGoBack) {
            OutlinedButton(
                onClick = { onEvent(QuickAssessmentEvent.PreviousStep) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Kembali")
            }
        }
        Button(
            onClick = { onEvent(QuickAssessmentEvent.NextStep) },
            enabled = step.canGoNext,
            modifier = Modifier.weight(1f),
        ) {
            Text(if (step.stepIndex < QUICK_ASSESSMENT_TOTAL_STEPS) "Lanjutkan" else "Selesai")
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
