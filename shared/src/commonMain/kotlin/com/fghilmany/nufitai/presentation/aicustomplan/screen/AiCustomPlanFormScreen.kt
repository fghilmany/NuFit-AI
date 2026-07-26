package com.fghilmany.nufitai.presentation.aicustomplan.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.fghilmany.nufitai.domain.exerciselibrary.entity.MuscleGroup
import com.fghilmany.nufitai.presentation.aicustomplan.viewmodel.AiCustomPlanFormEvent
import com.fghilmany.nufitai.presentation.aicustomplan.viewmodel.AiCustomPlanFormState
import com.fghilmany.nufitai.presentation.aicustomplan.viewmodel.AiCustomPlanFormViewModel
import com.fghilmany.nufitai.presentation.exerciselibrary.component.shortLabel
import com.fghilmany.nufitai.presentation.onboarding.component.equipmentGridOptions
import com.fghilmany.nufitai.presentation.onboarding.component.goalOptions
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.aicustomplan_form_equipment_title
import nufitai.shared.generated.resources.aicustomplan_form_generating
import nufitai.shared.generated.resources.aicustomplan_form_goal_title
import nufitai.shared.generated.resources.aicustomplan_form_muscle_focus_title
import nufitai.shared.generated.resources.aicustomplan_form_plan_duration_title
import nufitai.shared.generated.resources.aicustomplan_form_session_duration_title
import nufitai.shared.generated.resources.aicustomplan_form_sessions_per_week_label
import nufitai.shared.generated.resources.aicustomplan_form_submit
import nufitai.shared.generated.resources.aicustomplan_form_subtitle
import nufitai.shared.generated.resources.aicustomplan_form_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val SESSION_DURATION_OPTIONS = listOf(30, 45, 60, 90)
private val PLAN_DURATION_OPTIONS = 1..4

/** P-12 B (UC-3). */
@Composable
fun AiCustomPlanFormScreen(
    onGenerated: () -> Unit,
    viewModel: AiCustomPlanFormViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is AiCustomPlanFormState.Generated) onGenerated()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when (val current = state) {
            is AiCustomPlanFormState.Editing -> FormContent(current, viewModel)
            AiCustomPlanFormState.Generating -> LoadingBox()
            is AiCustomPlanFormState.Error -> Column {
                Text(current.failure.message, modifier = Modifier.padding(24.dp))
                FormContent(current.retainedInput, viewModel)
            }
            AiCustomPlanFormState.Generated -> LoadingBox() // brief -- LaunchedEffect navigates away
        }
    }
}

@Composable
private fun FormContent(editing: AiCustomPlanFormState.Editing, viewModel: AiCustomPlanFormViewModel) {
    val goalOptions = goalOptions()
    val equipmentOptions = equipmentGridOptions()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text(stringResource(Res.string.aicustomplan_form_title), style = MaterialTheme.typography.headlineSmall) }
        item { Text(stringResource(Res.string.aicustomplan_form_subtitle), style = MaterialTheme.typography.bodyMedium) }

        item { Text(stringResource(Res.string.aicustomplan_form_goal_title), style = MaterialTheme.typography.titleSmall) }
        items(goalOptions) { (goal, copy) ->
            ToggleButton(copy.title, editing.goal == goal, Modifier.fillMaxWidth()) {
                viewModel.onEvent(AiCustomPlanFormEvent.SetGoal(goal))
            }
        }

        item {
            Column {
                Text(stringResource(Res.string.aicustomplan_form_sessions_per_week_label), style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = {
                        viewModel.onEvent(AiCustomPlanFormEvent.SetSessionsPerWeek((editing.sessionsPerWeek - 1).coerceIn(1, 7)))
                    }) { Text("-") }
                    Text("${editing.sessionsPerWeek}", style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(onClick = {
                        viewModel.onEvent(AiCustomPlanFormEvent.SetSessionsPerWeek((editing.sessionsPerWeek + 1).coerceIn(1, 7)))
                    }) { Text("+") }
                }
            }
        }

        item { Text(stringResource(Res.string.aicustomplan_form_session_duration_title), style = MaterialTheme.typography.titleSmall) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SESSION_DURATION_OPTIONS.forEach { minutes ->
                    ToggleButton("$minutes", editing.sessionDurationMinutes == minutes, Modifier.weight(1f)) {
                        viewModel.onEvent(AiCustomPlanFormEvent.SetSessionDurationMinutes(minutes))
                    }
                }
            }
        }

        item { Text(stringResource(Res.string.aicustomplan_form_equipment_title), style = MaterialTheme.typography.titleSmall) }
        items(equipmentOptions) { (equipment, copy) ->
            ToggleButton(copy.title, equipment in editing.equipmentPreference, Modifier.fillMaxWidth()) {
                viewModel.onEvent(AiCustomPlanFormEvent.ToggleEquipment(equipment))
            }
        }

        item { Text(stringResource(Res.string.aicustomplan_form_muscle_focus_title), style = MaterialTheme.typography.titleSmall) }
        items(MuscleGroup.entries.toList()) { muscleGroup ->
            ToggleButton(muscleGroup.shortLabel(), muscleGroup in editing.muscleFocus, Modifier.fillMaxWidth()) {
                viewModel.onEvent(AiCustomPlanFormEvent.ToggleMuscleFocus(muscleGroup))
            }
        }

        item { Text(stringResource(Res.string.aicustomplan_form_plan_duration_title), style = MaterialTheme.typography.titleSmall) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PLAN_DURATION_OPTIONS.forEach { weeks ->
                    ToggleButton("$weeks", editing.planDurationWeeks == weeks, Modifier.weight(1f)) {
                        viewModel.onEvent(AiCustomPlanFormEvent.SetPlanDurationWeeks(weeks))
                    }
                }
            }
        }

        item {
            AppButton(
                text = stringResource(Res.string.aicustomplan_form_submit),
                onClick = { viewModel.onEvent(AiCustomPlanFormEvent.Submit) },
                enabled = editing.canSubmit,
                modifier = Modifier.fillMaxWidth(),
            )
        }
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
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator()
            Text(stringResource(Res.string.aicustomplan_form_generating))
        }
    }
}
