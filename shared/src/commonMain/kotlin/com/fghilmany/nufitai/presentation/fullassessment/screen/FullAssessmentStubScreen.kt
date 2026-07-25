package com.fghilmany.nufitai.presentation.fullassessment.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.fghilmany.nufitai.core.designsystem.component.AppElevatedCard
import com.fghilmany.nufitai.core.designsystem.component.AppTextField
import com.fghilmany.nufitai.domain.exerciselibrary.entity.BodyArea
import com.fghilmany.nufitai.domain.exerciselibrary.entity.EquipmentCategory
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.fullassessment.entity.Gender
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import com.fghilmany.nufitai.presentation.fullassessment.component.fullAssessmentParQCopy
import com.fghilmany.nufitai.presentation.fullassessment.component.shortLabel
import com.fghilmany.nufitai.presentation.fullassessment.viewmodel.FullAssessmentEvent
import com.fghilmany.nufitai.presentation.fullassessment.viewmodel.FullAssessmentPhase
import com.fghilmany.nufitai.presentation.fullassessment.viewmodel.FullAssessmentState
import com.fghilmany.nufitai.presentation.fullassessment.viewmodel.FullAssessmentViewModel
import com.fghilmany.nufitai.presentation.fullassessment.viewmodel.MOVEMENT_FLAG_OPTIONS
import com.fghilmany.nufitai.presentation.fullassessment.viewmodel.POSTURAL_FLAG_OPTIONS
import com.fghilmany.nufitai.presentation.onboarding.component.CheckboxRow
import com.fghilmany.nufitai.presentation.onboarding.component.OnboardingTopBar
import com.fghilmany.nufitai.presentation.onboarding.component.QuestionCard
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.common_action_continue
import nufitai.shared.generated.resources.fullassessment_capacity_cancel_skip
import nufitai.shared.generated.resources.fullassessment_capacity_plank_label
import nufitai.shared.generated.resources.fullassessment_capacity_pushup_label
import nufitai.shared.generated.resources.fullassessment_capacity_sit_to_stand_label
import nufitai.shared.generated.resources.fullassessment_capacity_skip_test
import nufitai.shared.generated.resources.fullassessment_capacity_submit
import nufitai.shared.generated.resources.fullassessment_capacity_title
import nufitai.shared.generated.resources.fullassessment_day_fri
import nufitai.shared.generated.resources.fullassessment_day_mon
import nufitai.shared.generated.resources.fullassessment_day_sat
import nufitai.shared.generated.resources.fullassessment_day_sun
import nufitai.shared.generated.resources.fullassessment_day_thu
import nufitai.shared.generated.resources.fullassessment_day_tue
import nufitai.shared.generated.resources.fullassessment_day_wed
import nufitai.shared.generated.resources.fullassessment_gate_blocked_ack_label
import nufitai.shared.generated.resources.fullassessment_gate_blocked_body
import nufitai.shared.generated.resources.fullassessment_gate_blocked_title
import nufitai.shared.generated.resources.fullassessment_interview_age_label
import nufitai.shared.generated.resources.fullassessment_interview_equipment_title
import nufitai.shared.generated.resources.fullassessment_interview_frequency_label
import nufitai.shared.generated.resources.fullassessment_interview_gender_female
import nufitai.shared.generated.resources.fullassessment_interview_gender_male
import nufitai.shared.generated.resources.fullassessment_interview_goal_title
import nufitai.shared.generated.resources.fullassessment_interview_injury_history_title
import nufitai.shared.generated.resources.fullassessment_interview_session_duration_label
import nufitai.shared.generated.resources.fullassessment_interview_training_days_title
import nufitai.shared.generated.resources.fullassessment_movement_title
import nufitai.shared.generated.resources.fullassessment_postural_title
import nufitai.shared.generated.resources.fullassessment_top_bar_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Functional, not polished (issue #76 §2) -- no P-11 Figma frame exists yet. Reuses the Quick
 * Assessment wizard's card/stepper visual pattern (QuestionCard/CheckboxRow/OnboardingTopBar).
 */
@Composable
fun FullAssessmentStubScreen(
    onCompleted: (FullAssessmentResult) -> Unit,
    viewModel: FullAssessmentViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        val current = state
        if (current is FullAssessmentState.Completed) onCompleted(current.result)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OnboardingTopBar(title = stringResource(Res.string.fullassessment_top_bar_title))
        Box(modifier = Modifier.weight(1f)) {
            when (val current = state) {
                is FullAssessmentState.Step -> StepContent(current, viewModel)
                FullAssessmentState.Submitting -> LoadingBox()
                is FullAssessmentState.Completed -> LoadingBox() // brief -- LaunchedEffect navigates away
                is FullAssessmentState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(current.message) }
            }
        }
    }
}

@Composable
private fun StepContent(step: FullAssessmentState.Step, viewModel: FullAssessmentViewModel) {
    when (step.phase) {
        FullAssessmentPhase.PAR_Q -> ParQPhase(step, viewModel)
        FullAssessmentPhase.GATE_BLOCKED -> GateBlockedPhase(step, viewModel)
        FullAssessmentPhase.INTERVIEW -> InterviewPhase(step, viewModel)
        FullAssessmentPhase.POSTURAL -> PosturalPhase(step, viewModel)
        FullAssessmentPhase.MOVEMENT -> MovementPhase(step, viewModel)
        FullAssessmentPhase.CAPACITY_TEST -> CapacityTestPhase(step, viewModel)
    }
}

@Composable
private fun ParQPhase(step: FullAssessmentState.Step, viewModel: FullAssessmentViewModel) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(fullAssessmentParQCopy()) { (questionId, text) ->
            QuestionCard(
                questionNumber = questionId.ordinal + 1,
                questionText = text,
                selected = step.parQAnswers[questionId],
                onAnswer = { viewModel.onEvent(FullAssessmentEvent.AnswerParQ(questionId, it)) },
            )
        }
        item {
            AppButton(
                onClick = { viewModel.onEvent(FullAssessmentEvent.SubmitParQ) },
                enabled = step.canSubmitParQ,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) { Text(stringResource(Res.string.common_action_continue)) }
        }
    }
}

@Composable
private fun GateBlockedPhase(step: FullAssessmentState.Step, viewModel: FullAssessmentViewModel) {
    val flagged = step.gateResult?.flaggedHardStopQuestions.orEmpty()
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        AppElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.fullassessment_gate_blocked_title), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(Res.string.fullassessment_gate_blocked_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
                flagged.forEach { Text("• ${it.shortLabel()}", style = MaterialTheme.typography.bodyMedium) }
            }
        }
        CheckboxRow(
            checked = step.gateAcknowledged,
            label = stringResource(Res.string.fullassessment_gate_blocked_ack_label),
            onToggle = { viewModel.onEvent(FullAssessmentEvent.ToggleGateAcknowledge) },
            modifier = Modifier.padding(top = 16.dp),
        )
        AppButton(
            onClick = { viewModel.onEvent(FullAssessmentEvent.ContinueFromGate) },
            enabled = step.canProceedFromGate,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        ) { Text(stringResource(Res.string.common_action_continue)) }
    }
}

@Composable
private fun hariLabels(): List<Pair<Int, String>> = listOf(
    1 to stringResource(Res.string.fullassessment_day_mon),
    2 to stringResource(Res.string.fullassessment_day_tue),
    3 to stringResource(Res.string.fullassessment_day_wed),
    4 to stringResource(Res.string.fullassessment_day_thu),
    5 to stringResource(Res.string.fullassessment_day_fri),
    6 to stringResource(Res.string.fullassessment_day_sat),
    7 to stringResource(Res.string.fullassessment_day_sun),
)

@Composable
private fun InterviewPhase(step: FullAssessmentState.Step, viewModel: FullAssessmentViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            AppTextField(
                value = step.usia,
                onValueChange = { viewModel.onEvent(FullAssessmentEvent.SetUsia(it)) },
                label = stringResource(Res.string.fullassessment_interview_age_label),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ToggleButton(
                    stringResource(Res.string.fullassessment_interview_gender_male),
                    step.gender == Gender.PRIA,
                    Modifier.weight(1f),
                ) {
                    viewModel.onEvent(FullAssessmentEvent.SetGender(Gender.PRIA))
                }
                ToggleButton(
                    stringResource(Res.string.fullassessment_interview_gender_female),
                    step.gender == Gender.WANITA,
                    Modifier.weight(1f),
                ) {
                    viewModel.onEvent(FullAssessmentEvent.SetGender(Gender.WANITA))
                }
            }
        }
        item { Text(stringResource(Res.string.fullassessment_interview_goal_title), style = MaterialTheme.typography.titleSmall) }
        items(GoalCategory.entries.toList()) { goal ->
            ToggleButton(goal.name, step.goal == goal, Modifier.fillMaxWidth()) {
                viewModel.onEvent(FullAssessmentEvent.SetGoal(goal))
            }
        }
        item {
            AppTextField(
                value = step.frekuensiPerMinggu,
                onValueChange = { viewModel.onEvent(FullAssessmentEvent.SetFrekuensiPerMinggu(it)) },
                label = stringResource(Res.string.fullassessment_interview_frequency_label),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item { Text(stringResource(Res.string.fullassessment_interview_training_days_title), style = MaterialTheme.typography.titleSmall) }
        item {
            val hariLabels = hariLabels()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                hariLabels.forEach { (day, label) ->
                    ToggleButton(label, day in step.hariPilihan, Modifier.weight(1f)) {
                        viewModel.onEvent(FullAssessmentEvent.ToggleHariPilihan(day))
                    }
                }
            }
        }
        item {
            AppTextField(
                value = step.durasiSesiMenit,
                onValueChange = { viewModel.onEvent(FullAssessmentEvent.SetDurasiSesiMenit(it)) },
                label = stringResource(Res.string.fullassessment_interview_session_duration_label),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item { Text(stringResource(Res.string.fullassessment_interview_equipment_title), style = MaterialTheme.typography.titleSmall) }
        items(EquipmentCategory.entries.toList()) { equipment ->
            CheckboxRow(
                checked = equipment in step.preferensiAlat,
                label = equipment.name,
                onToggle = { viewModel.onEvent(FullAssessmentEvent.TogglePreferensiAlat(equipment)) },
            )
        }
        item {
            Text(
                stringResource(Res.string.fullassessment_interview_injury_history_title),
                style = MaterialTheme.typography.titleSmall,
            )
        }
        items(BodyArea.entries.toList()) { area ->
            CheckboxRow(
                checked = area in step.riwayatCedera,
                label = area.shortLabel(),
                onToggle = { viewModel.onEvent(FullAssessmentEvent.ToggleRiwayatCedera(area)) },
            )
        }
        item {
            AppButton(
                onClick = { viewModel.onEvent(FullAssessmentEvent.NextFromInterview) },
                enabled = step.canProceedFromInterview,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(Res.string.common_action_continue)) }
        }
    }
}

@Composable
private fun PosturalPhase(step: FullAssessmentState.Step, viewModel: FullAssessmentViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(stringResource(Res.string.fullassessment_postural_title), style = MaterialTheme.typography.titleSmall)
        POSTURAL_FLAG_OPTIONS.forEach { flag ->
            CheckboxRow(
                checked = flag in step.flagsPostural,
                label = flag.shortLabel(),
                onToggle = { viewModel.onEvent(FullAssessmentEvent.TogglePosturalFlag(flag)) },
            )
        }
        AppButton(
            onClick = { viewModel.onEvent(FullAssessmentEvent.NextFromPostural) },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) { Text(stringResource(Res.string.common_action_continue)) }
    }
}

@Composable
private fun MovementPhase(step: FullAssessmentState.Step, viewModel: FullAssessmentViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(stringResource(Res.string.fullassessment_movement_title), style = MaterialTheme.typography.titleSmall)
        MOVEMENT_FLAG_OPTIONS.forEach { flag ->
            CheckboxRow(
                checked = flag in step.flagsGerak,
                label = flag.shortLabel(),
                onToggle = { viewModel.onEvent(FullAssessmentEvent.ToggleMovementFlag(flag)) },
            )
        }
        AppButton(
            onClick = { viewModel.onEvent(FullAssessmentEvent.NextFromMovement) },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) { Text(stringResource(Res.string.common_action_continue)) }
    }
}

@Composable
private fun CapacityTestPhase(step: FullAssessmentState.Step, viewModel: FullAssessmentViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(Res.string.fullassessment_capacity_title), style = MaterialTheme.typography.titleSmall)
        AppTextField(
            value = step.pushupReps,
            onValueChange = { viewModel.onEvent(FullAssessmentEvent.SetPushupReps(it)) },
            label = stringResource(Res.string.fullassessment_capacity_pushup_label),
            enabled = !step.capacityTestSkipped,
            modifier = Modifier.fillMaxWidth(),
        )
        AppTextField(
            value = step.plankDetik,
            onValueChange = { viewModel.onEvent(FullAssessmentEvent.SetPlankDetik(it)) },
            label = stringResource(Res.string.fullassessment_capacity_plank_label),
            enabled = !step.capacityTestSkipped,
            modifier = Modifier.fillMaxWidth(),
        )
        AppTextField(
            value = step.sitToStandReps,
            onValueChange = { viewModel.onEvent(FullAssessmentEvent.SetSitToStandReps(it)) },
            label = stringResource(Res.string.fullassessment_capacity_sit_to_stand_label),
            enabled = !step.capacityTestSkipped,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(
            onClick = { viewModel.onEvent(FullAssessmentEvent.ToggleSkipCapacityTest) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (step.capacityTestSkipped) {
                    stringResource(Res.string.fullassessment_capacity_cancel_skip)
                } else {
                    stringResource(Res.string.fullassessment_capacity_skip_test)
                },
            )
        }
        AppButton(
            onClick = { viewModel.onEvent(FullAssessmentEvent.Submit) },
            enabled = step.canSubmit,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(Res.string.fullassessment_capacity_submit)) }
    }
}

@Composable
private fun ToggleButton(label: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (isSelected) {
        AppButton(onClick = onClick, modifier = modifier) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(label) }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
