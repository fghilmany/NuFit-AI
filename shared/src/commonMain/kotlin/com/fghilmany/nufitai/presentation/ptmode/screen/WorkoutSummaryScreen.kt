package com.fghilmany.nufitai.presentation.ptmode.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.core.designsystem.component.AppCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.presentation.ptmode.component.PrBanner
import com.fghilmany.nufitai.presentation.ptmode.component.RpeLevel
import com.fghilmany.nufitai.presentation.ptmode.component.formatWeightKg
import com.fghilmany.nufitai.presentation.ptmode.component.rpeLevels
import com.fghilmany.nufitai.presentation.ptmode.viewmodel.WorkoutSummaryEvent
import com.fghilmany.nufitai.presentation.ptmode.viewmodel.WorkoutSummaryState
import com.fghilmany.nufitai.presentation.ptmode.viewmodel.WorkoutSummaryViewModel
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.ptmode_summary_close_button
import nufitai.shared.generated.resources.ptmode_summary_close_content_description
import nufitai.shared.generated.resources.ptmode_summary_duration_label
import nufitai.shared.generated.resources.ptmode_summary_duration_value
import nufitai.shared.generated.resources.ptmode_summary_exercise_label
import nufitai.shared.generated.resources.ptmode_summary_exercise_value
import nufitai.shared.generated.resources.ptmode_summary_headline
import nufitai.shared.generated.resources.ptmode_summary_rpe_level_label
import nufitai.shared.generated.resources.ptmode_summary_rpe_section_title
import nufitai.shared.generated.resources.ptmode_summary_save_button
import nufitai.shared.generated.resources.ptmode_summary_subtitle
import nufitai.shared.generated.resources.ptmode_summary_title
import nufitai.shared.generated.resources.ptmode_summary_total_volume_label
import nufitai.shared.generated.resources.ptmode_summary_volume_value
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * issue #80 P-06 (Figma node 12:188). AC-9: reachable states are Loaded/Error only from the
 * user's perspective -- the session is already saved by the time this loads (WorkoutSummaryViewModel.load),
 * so "Tutup" is always safe.
 */
@Composable
fun WorkoutSummaryScreen(
    planDayId: String,
    onDone: () -> Unit,
    viewModel: WorkoutSummaryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(planDayId) { viewModel.load(planDayId) }
    LaunchedEffect(state) {
        if (state is WorkoutSummaryState.Saved) onDone()
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopBar(onClose = onDone)
        when (val current = state) {
            WorkoutSummaryState.Loading -> LoadingBox()
            WorkoutSummaryState.Saved -> LoadingBox() // brief -- LaunchedEffect navigates away
            is WorkoutSummaryState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(current.message) }
            is WorkoutSummaryState.Loaded -> SummaryContent(
                state = current,
                onSelectRpe = { viewModel.onEvent(WorkoutSummaryEvent.SelectRpe(it)) },
                onSave = { viewModel.onEvent(WorkoutSummaryEvent.Save) },
                onClose = onDone,
            )
        }
    }
}

@Composable
private fun TopBar(onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(Res.string.ptmode_summary_title), style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onClose) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(Res.string.ptmode_summary_close_content_description))
        }
    }
}

@Composable
private fun SummaryContent(
    state: WorkoutSummaryState.Loaded,
    onSelectRpe: (Int) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
) {
    val summary = state.summary
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.size(64.dp).background(NuFitColors.SecondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = NuFitColors.OnSecondaryContainer, modifier = Modifier.size(32.dp))
                }
                Text(stringResource(Res.string.ptmode_summary_headline), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Text(
                    stringResource(Res.string.ptmode_summary_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        item {
            AppCard(modifier = Modifier.fillMaxWidth(), backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
                Column {
                    Text(stringResource(Res.string.ptmode_summary_total_volume_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        stringResource(Res.string.ptmode_summary_volume_value, formatWeightKg(summary.totalVolumeKg)),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    label = stringResource(Res.string.ptmode_summary_duration_label),
                    value = stringResource(Res.string.ptmode_summary_duration_value, summary.durationMinutes),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = stringResource(Res.string.ptmode_summary_exercise_label),
                    value = stringResource(Res.string.ptmode_summary_exercise_value, summary.completedExerciseCount, summary.totalExerciseCount),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        items(summary.personalRecords) { record -> PrBanner(record) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(Res.string.ptmode_summary_rpe_section_title), style = MaterialTheme.typography.titleMedium)
                RpeSelector(selectedRpe = state.selectedRpe, onSelectRpe = onSelectRpe)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                AppButton(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(containerColor = NuFitColors.Primary, contentColor = NuFitColors.OnPrimary),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(Res.string.ptmode_summary_save_button)) }
                TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.ptmode_summary_close_button))
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier, backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun RpeSelector(selectedRpe: Int?, onSelectRpe: (Int) -> Unit) {
    val levels = rpeLevels()
    val defaultLevel = levels[4] // "Tingkat 5: Sedang" -- matches the Figma default
    val current: RpeLevel = levels.find { it.value == selectedRpe } ?: defaultLevel

    AppCard(modifier = Modifier.fillMaxWidth(), backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(Res.string.ptmode_summary_rpe_level_label, current.value, current.label),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(current.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    Slider(
        value = current.value.toFloat(),
        onValueChange = { onSelectRpe(it.toInt()) },
        valueRange = 1f..10f,
        steps = 8,
        modifier = Modifier.fillMaxWidth(),
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("1", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("10", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
