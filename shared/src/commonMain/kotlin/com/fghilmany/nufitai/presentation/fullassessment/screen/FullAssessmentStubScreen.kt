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
import com.fghilmany.nufitai.presentation.fullassessment.component.FullAssessmentParQCopy
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
        OnboardingTopBar(title = "Full Assessment")
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
        items(FullAssessmentParQCopy) { (questionId, text) ->
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
            ) { Text("Lanjutkan") }
        }
    }
}

@Composable
private fun GateBlockedPhase(step: FullAssessmentState.Step, viewModel: FullAssessmentViewModel) {
    val flagged = step.gateResult?.flaggedHardStopQuestions.orEmpty()
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        AppElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sebaiknya konsultasi ke dokter dulu", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Berdasarkan jawaban Anda, ada kondisi yang sebaiknya didiskusikan dengan dokter sebelum " +
                        "melanjutkan asesmen ini.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                flagged.forEach { Text("• ${it.shortLabel()}", style = MaterialTheme.typography.bodyMedium) }
            }
        }
        CheckboxRow(
            checked = step.gateAcknowledged,
            label = "Saya memahami dan akan/sudah berkonsultasi dengan dokter mengenai kondisi ini.",
            onToggle = { viewModel.onEvent(FullAssessmentEvent.ToggleGateAcknowledge) },
            modifier = Modifier.padding(top = 16.dp),
        )
        AppButton(
            onClick = { viewModel.onEvent(FullAssessmentEvent.ContinueFromGate) },
            enabled = step.canProceedFromGate,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        ) { Text("Lanjutkan") }
    }
}

private val HARI_LABELS = listOf(1 to "Sen", 2 to "Sel", 3 to "Rab", 4 to "Kam", 5 to "Jum", 6 to "Sab", 7 to "Min")

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
                label = "Usia",
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ToggleButton("Pria", step.gender == Gender.PRIA, Modifier.weight(1f)) {
                    viewModel.onEvent(FullAssessmentEvent.SetGender(Gender.PRIA))
                }
                ToggleButton("Wanita", step.gender == Gender.WANITA, Modifier.weight(1f)) {
                    viewModel.onEvent(FullAssessmentEvent.SetGender(Gender.WANITA))
                }
            }
        }
        item { Text("Tujuan latihan", style = MaterialTheme.typography.titleSmall) }
        items(GoalCategory.entries.toList()) { goal ->
            ToggleButton(goal.name, step.goal == goal, Modifier.fillMaxWidth()) {
                viewModel.onEvent(FullAssessmentEvent.SetGoal(goal))
            }
        }
        item {
            AppTextField(
                value = step.frekuensiPerMinggu,
                onValueChange = { viewModel.onEvent(FullAssessmentEvent.SetFrekuensiPerMinggu(it)) },
                label = "Frekuensi latihan per minggu",
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item { Text("Hari latihan", style = MaterialTheme.typography.titleSmall) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                HARI_LABELS.forEach { (day, label) ->
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
                label = "Durasi sesi (menit)",
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item { Text("Preferensi alat", style = MaterialTheme.typography.titleSmall) }
        items(EquipmentCategory.entries.toList()) { equipment ->
            CheckboxRow(
                checked = equipment in step.preferensiAlat,
                label = equipment.name,
                onToggle = { viewModel.onEvent(FullAssessmentEvent.TogglePreferensiAlat(equipment)) },
            )
        }
        item { Text("Riwayat cedera / nyeri (area)", style = MaterialTheme.typography.titleSmall) }
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
            ) { Text("Lanjutkan") }
        }
    }
}

@Composable
private fun PosturalPhase(step: FullAssessmentState.Step, viewModel: FullAssessmentViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Temuan asesmen postur", style = MaterialTheme.typography.titleSmall)
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
        ) { Text("Lanjutkan") }
    }
}

@Composable
private fun MovementPhase(step: FullAssessmentState.Step, viewModel: FullAssessmentViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Temuan movement screening", style = MaterialTheme.typography.titleSmall)
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
        ) { Text("Lanjutkan") }
    }
}

@Composable
private fun CapacityTestPhase(step: FullAssessmentState.Step, viewModel: FullAssessmentViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Tes kapasitas fisik (opsional)", style = MaterialTheme.typography.titleSmall)
        AppTextField(
            value = step.pushupReps,
            onValueChange = { viewModel.onEvent(FullAssessmentEvent.SetPushupReps(it)) },
            label = "Push-up (reps)",
            enabled = !step.capacityTestSkipped,
            modifier = Modifier.fillMaxWidth(),
        )
        AppTextField(
            value = step.plankDetik,
            onValueChange = { viewModel.onEvent(FullAssessmentEvent.SetPlankDetik(it)) },
            label = "Plank (detik)",
            enabled = !step.capacityTestSkipped,
            modifier = Modifier.fillMaxWidth(),
        )
        AppTextField(
            value = step.sitToStandReps,
            onValueChange = { viewModel.onEvent(FullAssessmentEvent.SetSitToStandReps(it)) },
            label = "Sit-to-Stand 30 detik (reps)",
            enabled = !step.capacityTestSkipped,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(
            onClick = { viewModel.onEvent(FullAssessmentEvent.ToggleSkipCapacityTest) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (step.capacityTestSkipped) "Batal lewati" else "Lewati tes ini") }
        AppButton(
            onClick = { viewModel.onEvent(FullAssessmentEvent.Submit) },
            enabled = step.canSubmit,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Selesai") }
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
