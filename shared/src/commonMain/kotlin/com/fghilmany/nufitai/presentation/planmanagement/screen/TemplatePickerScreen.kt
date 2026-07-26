package com.fghilmany.nufitai.presentation.planmanagement.screen

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
import com.fghilmany.nufitai.domain.monthlyplan.entity.MonthlyPlan
import com.fghilmany.nufitai.domain.onboarding.entity.Level
import com.fghilmany.nufitai.domain.onboarding.entity.ResolvedSplit
import com.fghilmany.nufitai.presentation.assessmentdetail.component.shortLabel
import com.fghilmany.nufitai.presentation.onboarding.component.goalOptions
import com.fghilmany.nufitai.presentation.planmanagement.viewmodel.TemplatePickerEvent
import com.fghilmany.nufitai.presentation.planmanagement.viewmodel.TemplatePickerState
import com.fghilmany.nufitai.presentation.planmanagement.viewmodel.TemplatePickerViewModel
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.planmanagement_picker_activate_now
import nufitai.shared.generated.resources.planmanagement_picker_goal_title
import nufitai.shared.generated.resources.planmanagement_picker_level_title
import nufitai.shared.generated.resources.planmanagement_picker_save_draft
import nufitai.shared.generated.resources.planmanagement_picker_split_title
import nufitai.shared.generated.resources.planmanagement_picker_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** P-14 UC-4 -- 3-step manual picker, no Figma frame (techspec §2: "not designed"). */
@Composable
fun TemplatePickerScreen(
    onCompleted: (MonthlyPlan) -> Unit,
    viewModel: TemplatePickerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        val current = state
        if (current is TemplatePickerState.Completed) onCompleted(current.plan)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            stringResource(Res.string.planmanagement_picker_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(24.dp),
        )
        when (val current = state) {
            is TemplatePickerState.Step -> StepContent(current, viewModel)
            TemplatePickerState.Submitting -> LoadingBox()
            is TemplatePickerState.Completed -> LoadingBox() // brief -- LaunchedEffect navigates away
            is TemplatePickerState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(current.message) }
        }
    }
}

@Composable
private fun StepContent(step: TemplatePickerState.Step, viewModel: TemplatePickerViewModel) {
    val goalOptions = goalOptions()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { Text(stringResource(Res.string.planmanagement_picker_level_title), style = MaterialTheme.typography.titleSmall) }
        items(Level.entries.toList()) { level ->
            ToggleButton(level.shortLabel(), step.level == level, Modifier.fillMaxWidth()) {
                viewModel.onEvent(TemplatePickerEvent.SetLevel(level))
            }
        }

        item { Text(stringResource(Res.string.planmanagement_picker_goal_title), style = MaterialTheme.typography.titleSmall) }
        items(goalOptions) { (goal, copy) ->
            ToggleButton(copy.title, step.goal == goal, Modifier.fillMaxWidth()) {
                viewModel.onEvent(TemplatePickerEvent.SetGoal(goal))
            }
        }

        item { Text(stringResource(Res.string.planmanagement_picker_split_title), style = MaterialTheme.typography.titleSmall) }
        items(ResolvedSplit.entries.toList()) { split ->
            ToggleButton(split.shortLabel(), step.resolvedSplit == split, Modifier.fillMaxWidth()) {
                viewModel.onEvent(TemplatePickerEvent.SetSplit(split))
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                OutlinedButton(
                    onClick = { viewModel.onEvent(TemplatePickerEvent.Submit(activateNow = false)) },
                    enabled = step.canSubmit,
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(Res.string.planmanagement_picker_save_draft)) }
                AppButton(
                    onClick = { viewModel.onEvent(TemplatePickerEvent.Submit(activateNow = true)) },
                    enabled = step.canSubmit,
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(Res.string.planmanagement_picker_activate_now)) }
            }
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
        CircularProgressIndicator()
    }
}
