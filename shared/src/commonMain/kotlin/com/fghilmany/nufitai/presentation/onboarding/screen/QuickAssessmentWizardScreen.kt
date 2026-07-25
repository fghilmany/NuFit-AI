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
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.presentation.onboarding.component.EquipmentGridCard
import com.fghilmany.nufitai.presentation.onboarding.component.EquipmentRowCard
import com.fghilmany.nufitai.presentation.onboarding.component.OnboardingTopBar
import com.fghilmany.nufitai.presentation.onboarding.component.OptionCard
import com.fghilmany.nufitai.presentation.onboarding.component.OptionCopy
import com.fghilmany.nufitai.presentation.onboarding.component.ProgressBar
import com.fghilmany.nufitai.presentation.onboarding.component.SelectAllIcon
import com.fghilmany.nufitai.presentation.onboarding.component.TipsBanner
import com.fghilmany.nufitai.presentation.onboarding.component.cardioOption
import com.fghilmany.nufitai.presentation.onboarding.component.equipmentGridOptions
import com.fghilmany.nufitai.presentation.onboarding.component.experienceOptions
import com.fghilmany.nufitai.presentation.onboarding.component.frequencyOptions
import com.fghilmany.nufitai.presentation.onboarding.component.goalOptions
import com.fghilmany.nufitai.presentation.onboarding.component.splitOptions
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.QUICK_ASSESSMENT_TOTAL_STEPS
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.QuickAssessmentEvent
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.QuickAssessmentState
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.QuickAssessmentViewModel
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.common_action_back
import nufitai.shared.generated.resources.common_action_continue
import nufitai.shared.generated.resources.common_action_done
import nufitai.shared.generated.resources.onboarding_wizard_percent_complete
import nufitai.shared.generated.resources.onboarding_wizard_select_all_subtitle
import nufitai.shared.generated.resources.onboarding_wizard_select_all_title
import nufitai.shared.generated.resources.onboarding_wizard_step_label
import nufitai.shared.generated.resources.onboarding_wizard_tips_body
import nufitai.shared.generated.resources.onboarding_wizard_tips_title
import nufitai.shared.generated.resources.onboarding_wizard_top_bar_title
import org.jetbrains.compose.resources.stringResource
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
            title = stringResource(Res.string.onboarding_wizard_top_bar_title),
            onBack = if (step.canGoBack) {
                { onEvent(QuickAssessmentEvent.PreviousStep) }
            } else {
                null
            },
        )

        Column(modifier = Modifier.weight(1f).padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(Res.string.onboarding_wizard_step_label, step.stepIndex, QUICK_ASSESSMENT_TOTAL_STEPS),
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    stringResource(Res.string.onboarding_wizard_percent_complete, step.stepIndex * 100 / QUICK_ASSESSMENT_TOTAL_STEPS),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            ProgressBar(
                progress = step.stepIndex / QUICK_ASSESSMENT_TOTAL_STEPS.toFloat(),
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )

            // Resolved here (composable context) -- LazyListScope's content lambda is not
            // @Composable itself, so stringResource()-backed option lists can't be built inline below.
            val experienceOptions = experienceOptions()
            val goalOptions = goalOptions()
            val frequencyOptions = frequencyOptions()
            val splitOptions = splitOptions()
            val equipmentGridOptions = equipmentGridOptions()
            val cardioOption = cardioOption()
            val selectAllTitle = stringResource(Res.string.onboarding_wizard_select_all_title)
            val selectAllSubtitle = stringResource(Res.string.onboarding_wizard_select_all_subtitle)
            val tipsTitle = stringResource(Res.string.onboarding_wizard_tips_title)
            val tipsBody = stringResource(Res.string.onboarding_wizard_tips_body)

            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when (step.stepIndex) {
                    1 -> items(experienceOptions) { (value, copy) ->
                        OptionCard(
                            icon = copy.icon,
                            title = copy.title,
                            subtitle = copy.subtitle,
                            isSelected = step.experience == value,
                            onClick = { onEvent(QuickAssessmentEvent.SelectExperience(value)) },
                        )
                    }
                    2 -> items(goalOptions) { (value, copy) ->
                        OptionCard(
                            icon = copy.icon,
                            title = copy.title,
                            subtitle = copy.subtitle,
                            isSelected = step.goal == value,
                            onClick = { onEvent(QuickAssessmentEvent.SelectGoal(value)) },
                        )
                    }
                    3 -> equipmentStep(
                        step = step,
                        onEvent = onEvent,
                        selectAllTitle = selectAllTitle,
                        selectAllSubtitle = selectAllSubtitle,
                        equipmentGridOptions = equipmentGridOptions,
                        cardioOption = cardioOption,
                        tipsTitle = tipsTitle,
                        tipsBody = tipsBody,
                    )
                    4 -> items(frequencyOptions) { (value, copy) ->
                        OptionCard(
                            icon = copy.icon,
                            title = copy.title,
                            subtitle = copy.subtitle,
                            isSelected = step.frequency == value,
                            onClick = { onEvent(QuickAssessmentEvent.SelectFrequency(value)) },
                        )
                    }
                    5 -> items(splitOptions) { (value, copy) ->
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
    selectAllTitle: String,
    selectAllSubtitle: String,
    equipmentGridOptions: List<Pair<EquipmentCategory, OptionCopy>>,
    cardioOption: OptionCopy,
    tipsTitle: String,
    tipsBody: String,
) {
    val allSelected = EquipmentCategory.entries.filterNot { it == EquipmentCategory.CARDIO_EQUIPMENT }.all { it in step.equipment }

    item {
        EquipmentRowCard(
            icon = SelectAllIcon,
            title = selectAllTitle,
            subtitle = selectAllSubtitle,
            isSelected = allSelected,
            onClick = { onEvent(QuickAssessmentEvent.SelectAllEquipment) },
        )
    }
    items(equipmentGridOptions.chunked(2)) { pair ->
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
            icon = cardioOption.icon,
            title = cardioOption.title,
            subtitle = cardioOption.subtitle,
            isSelected = EquipmentCategory.CARDIO_EQUIPMENT in step.equipment,
            onClick = { onEvent(QuickAssessmentEvent.ToggleEquipment(EquipmentCategory.CARDIO_EQUIPMENT)) },
        )
    }
    item {
        TipsBanner(
            icon = Icons.Filled.SmartToy,
            title = tipsTitle,
            body = tipsBody,
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
                Text(stringResource(Res.string.common_action_back))
            }
        }
        AppButton(
            onClick = { onEvent(QuickAssessmentEvent.NextStep) },
            enabled = step.canGoNext,
            modifier = Modifier.weight(1f),
        ) {
            Text(
                stringResource(
                    if (step.stepIndex < QUICK_ASSESSMENT_TOTAL_STEPS) {
                        Res.string.common_action_continue
                    } else {
                        Res.string.common_action_done
                    },
                ),
            )
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
