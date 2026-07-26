package com.fghilmany.nufitai.presentation.aicustomplan.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.core.designsystem.component.AppElevatedCard
import com.fghilmany.nufitai.domain.monthlyplan.entity.DayType
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanDay
import com.fghilmany.nufitai.presentation.aicustomplan.component.copyText
import com.fghilmany.nufitai.presentation.aicustomplan.viewmodel.AiCustomPlanPreviewEvent
import com.fghilmany.nufitai.presentation.aicustomplan.viewmodel.AiCustomPlanPreviewState
import com.fghilmany.nufitai.presentation.aicustomplan.viewmodel.AiCustomPlanPreviewViewModel
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.aicustomplan_preview_activate
import nufitai.shared.generated.resources.aicustomplan_preview_ai_badge
import nufitai.shared.generated.resources.aicustomplan_preview_adjust_set_reps_label
import nufitai.shared.generated.resources.aicustomplan_preview_adjust_set_sets_label
import nufitai.shared.generated.resources.aicustomplan_preview_adjust_set_title
import nufitai.shared.generated.resources.aicustomplan_preview_delete_action
import nufitai.shared.generated.resources.aicustomplan_preview_injury_conflict_banner
import nufitai.shared.generated.resources.aicustomplan_preview_phase_adaptation
import nufitai.shared.generated.resources.aicustomplan_preview_phase_intensity
import nufitai.shared.generated.resources.aicustomplan_preview_phase_volume
import nufitai.shared.generated.resources.aicustomplan_preview_quota_full_body
import nufitai.shared.generated.resources.aicustomplan_preview_quota_full_title
import nufitai.shared.generated.resources.aicustomplan_preview_regenerate
import nufitai.shared.generated.resources.aicustomplan_preview_remove_action
import nufitai.shared.generated.resources.aicustomplan_preview_save_draft
import nufitai.shared.generated.resources.aicustomplan_preview_session_summary
import nufitai.shared.generated.resources.aicustomplan_preview_swap_action
import nufitai.shared.generated.resources.aicustomplan_preview_swap_dialog_empty
import nufitai.shared.generated.resources.aicustomplan_preview_swap_dialog_title
import nufitai.shared.generated.resources.aicustomplan_preview_warning_dropped_exercise
import nufitai.shared.generated.resources.aicustomplan_preview_week_label
import nufitai.shared.generated.resources.common_action_cancel
import nufitai.shared.generated.resources.common_action_done
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private fun weekNumberFor(dayNumber: Int): Int = ((dayNumber - 1) / 7) + 1

/** P-13 (UC-4). No live Figma re-check this pass (MCP rate-limited) -- rebuilt from the reviewed reference: week accordions, per-session sub-cards, swap/adjust-set actions. */
@Composable
fun AiCustomPlanPreviewScreen(
    onSaved: () -> Unit,
    onActivated: () -> Unit,
    viewModel: AiCustomPlanPreviewViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        when (state) {
            AiCustomPlanPreviewState.Saved -> onSaved()
            AiCustomPlanPreviewState.Activated -> onActivated()
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val current = state) {
            AiCustomPlanPreviewState.Loading, AiCustomPlanPreviewState.Regenerating -> LoadingBox()
            AiCustomPlanPreviewState.Saved, AiCustomPlanPreviewState.Activated -> LoadingBox() // brief -- LaunchedEffect navigates away
            is AiCustomPlanPreviewState.Loaded -> PreviewContent(current, viewModel)
            is AiCustomPlanPreviewState.QuotaFull -> QuotaFullContent(current, viewModel)
            is AiCustomPlanPreviewState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(current.message) }
        }
    }
}

@Composable
private fun PreviewContent(loaded: AiCustomPlanPreviewState.Loaded, viewModel: AiCustomPlanPreviewViewModel) {
    val weeks = loaded.days.groupBy { weekNumberFor(it.dayNumber) }.toSortedMap()
    var expandedWeek by rememberSaveable { mutableStateOf(weeks.keys.firstOrNull() ?: 1) }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(
                stringResource(Res.string.aicustomplan_preview_ai_badge),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        item { Text(loaded.smartGoalSummary, style = MaterialTheme.typography.titleMedium) }

        if (loaded.injuryConflicts.isNotEmpty()) {
            item { Banner(stringResource(Res.string.aicustomplan_preview_injury_conflict_banner), MaterialTheme.colorScheme.errorContainer) }
        }
        if (loaded.exerciseWarnings.isNotEmpty()) {
            item { Banner(stringResource(Res.string.aicustomplan_preview_warning_dropped_exercise), MaterialTheme.colorScheme.surfaceContainerHigh) }
        }
        items(loaded.conflicts) { conflict -> Banner(conflict.copyText(), MaterialTheme.colorScheme.surfaceContainerHigh) }
        items(loaded.serverNotes) { note -> Banner(note, MaterialTheme.colorScheme.surfaceContainerHigh) }

        weeks.forEach { (weekNumber, daysInWeek) ->
            item {
                WeekAccordionHeader(
                    weekNumber = weekNumber,
                    totalWeeks = weeks.size,
                    isExpanded = weekNumber == expandedWeek,
                    onClick = { expandedWeek = if (expandedWeek == weekNumber) -1 else weekNumber },
                )
            }
            if (weekNumber == expandedWeek) {
                items(daysInWeek.filter { it.type == DayType.SESSION }) { day ->
                    SessionGroupCard(day, loaded, viewModel)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { viewModel.onEvent(AiCustomPlanPreviewEvent.Regenerate) },
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(Res.string.aicustomplan_preview_regenerate)) }
                OutlinedButton(
                    onClick = { viewModel.onEvent(AiCustomPlanPreviewEvent.SaveDraft) },
                    modifier = Modifier.weight(1f),
                ) { Text(stringResource(Res.string.aicustomplan_preview_save_draft)) }
            }
        }
        item {
            AppButton(
                text = stringResource(Res.string.aicustomplan_preview_activate),
                onClick = { viewModel.onEvent(AiCustomPlanPreviewEvent.Activate) },
                enabled = loaded.canActivate,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    loaded.swapDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(AiCustomPlanPreviewEvent.DismissSwapDialog) },
            title = { Text(stringResource(Res.string.aicustomplan_preview_swap_dialog_title)) },
            text = {
                if (dialog.candidates.isEmpty()) {
                    Text(stringResource(Res.string.aicustomplan_preview_swap_dialog_empty))
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(dialog.candidates) { candidate ->
                            Text(
                                candidate.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { viewModel.onEvent(AiCustomPlanPreviewEvent.SelectSwapCandidate(candidate.id)) }
                                    .padding(vertical = 12.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(AiCustomPlanPreviewEvent.DismissSwapDialog) }) {
                    Text(stringResource(Res.string.common_action_cancel))
                }
            },
        )
    }

    loaded.adjustSetDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(AiCustomPlanPreviewEvent.DismissAdjustSetDialog) },
            title = { Text(stringResource(Res.string.aicustomplan_preview_adjust_set_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text(stringResource(Res.string.aicustomplan_preview_adjust_set_sets_label), style = MaterialTheme.typography.labelMedium)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconButton(onClick = { viewModel.onEvent(AiCustomPlanPreviewEvent.ChangeAdjustSetSets(-1)) }) {
                                Icon(Icons.Filled.Remove, contentDescription = null)
                            }
                            Text("${dialog.sets}", style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { viewModel.onEvent(AiCustomPlanPreviewEvent.ChangeAdjustSetSets(1)) }) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = dialog.repRangeOrDuration,
                        onValueChange = { viewModel.onEvent(AiCustomPlanPreviewEvent.ChangeAdjustSetRepRange(it)) },
                        label = { Text(stringResource(Res.string.aicustomplan_preview_adjust_set_reps_label)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(AiCustomPlanPreviewEvent.ConfirmAdjustSet) }) {
                    Text(stringResource(Res.string.common_action_done))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(AiCustomPlanPreviewEvent.DismissAdjustSetDialog) }) {
                    Text(stringResource(Res.string.common_action_cancel))
                }
            },
        )
    }
}

@Composable
private fun WeekAccordionHeader(weekNumber: Int, totalWeeks: Int, isExpanded: Boolean, onClick: () -> Unit) {
    val phaseLabel = when {
        weekNumber == 1 -> stringResource(Res.string.aicustomplan_preview_phase_adaptation)
        weekNumber == totalWeeks -> stringResource(Res.string.aicustomplan_preview_phase_intensity)
        else -> stringResource(Res.string.aicustomplan_preview_phase_volume)
    }
    AppElevatedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(stringResource(Res.string.aicustomplan_preview_week_label, weekNumber), style = MaterialTheme.typography.titleMedium)
                Text(phaseLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null)
        }
    }
}

@Composable
private fun SessionGroupCard(day: PlanDay, loaded: AiCustomPlanPreviewState.Loaded, viewModel: AiCustomPlanPreviewViewModel) {
    AppElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Sesi ${day.templateLetter.orEmpty()}",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                stringResource(Res.string.aicustomplan_preview_session_summary, loaded.sessionDurationMinutes, day.mainExercises.orEmpty().size),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            day.mainExercises.orEmpty().forEach { exercise ->
                val isConflicted = loaded.injuryConflicts.any { it.dayNumber == day.dayNumber && it.exerciseId == exercise.exerciseId }
                val exerciseName = loaded.exercisesById[exercise.exerciseId]?.name ?: exercise.exerciseId
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "$exerciseName -- ${exercise.sets}x${exercise.repRangeOrDuration}",
                        color = if (isConflicted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { viewModel.onEvent(AiCustomPlanPreviewEvent.RequestSwap(day.dayNumber, exercise.exerciseId)) }) {
                            Text(stringResource(Res.string.aicustomplan_preview_swap_action), style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(onClick = { viewModel.onEvent(AiCustomPlanPreviewEvent.RequestAdjustSet(day.dayNumber, exercise.exerciseId)) }) {
                            Text(stringResource(Res.string.aicustomplan_preview_adjust_set_title), style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = { viewModel.onEvent(AiCustomPlanPreviewEvent.RemoveExercise(day.dayNumber, exercise.exerciseId)) }) {
                            Text(stringResource(Res.string.aicustomplan_preview_remove_action), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuotaFullContent(quotaFull: AiCustomPlanPreviewState.QuotaFull, viewModel: AiCustomPlanPreviewViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(Res.string.aicustomplan_preview_quota_full_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(Res.string.aicustomplan_preview_quota_full_body), style = MaterialTheme.typography.bodyMedium)
        quotaFull.existingAiPlans.forEach { plan ->
            AppElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(plan.smartGoalMeta ?: plan.id, style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onClick = { viewModel.onEvent(AiCustomPlanPreviewEvent.DeleteExistingDraft(plan.id)) }) {
                        Text(stringResource(Res.string.aicustomplan_preview_delete_action))
                    }
                }
            }
        }
    }
}

@Composable
private fun Banner(text: String, background: Color) {
    AppElevatedCard(modifier = Modifier.fillMaxWidth(), backgroundColor = background) {
        Text(text, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
