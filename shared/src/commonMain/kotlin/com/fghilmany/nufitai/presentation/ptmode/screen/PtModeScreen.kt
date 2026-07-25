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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.core.designsystem.component.AppCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.core.keepawake.KeepScreenOn
import com.fghilmany.nufitai.presentation.ptmode.component.EndSessionDialog
import com.fghilmany.nufitai.presentation.ptmode.component.ExerciseSwapDialog
import com.fghilmany.nufitai.presentation.ptmode.component.SetInputSteppers
import com.fghilmany.nufitai.presentation.ptmode.component.formatWeightKg
import com.fghilmany.nufitai.presentation.ptmode.viewmodel.PtModeEvent
import com.fghilmany.nufitai.presentation.ptmode.viewmodel.PtModeState
import com.fghilmany.nufitai.presentation.ptmode.viewmodel.PtModeViewModel
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.ptmode_active_set_label
import nufitai.shared.generated.resources.ptmode_confirm_set_button
import nufitai.shared.generated.resources.ptmode_pause_content_description
import nufitai.shared.generated.resources.ptmode_rest_timer_label
import nufitai.shared.generated.resources.ptmode_rest_timer_value
import nufitai.shared.generated.resources.ptmode_set_counter
import nufitai.shared.generated.resources.ptmode_skip_exercise_button
import nufitai.shared.generated.resources.ptmode_skip_rest_button
import nufitai.shared.generated.resources.ptmode_swap_exercise_button
import nufitai.shared.generated.resources.ptmode_target_line
import nufitai.shared.generated.resources.ptmode_top_bar_eyebrow
import nufitai.shared.generated.resources.ptmode_top_bar_progress
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * issue #80 P-05 (Figma node 12:308). Dark immersive treatment (NuFitColors.Primary) matches
 * the pulled design. Rest countdown/backgrounding accuracy comes entirely from
 * PtModeViewModel's wall-clock restEndAt ticker -- this screen only renders whatever it emits.
 */
@Composable
fun PtModeScreen(
    planDayId: String,
    onSessionCompleted: (planDayId: String) -> Unit,
    onExit: () -> Unit,
    viewModel: PtModeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showEndSessionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(planDayId) { viewModel.load(planDayId) }
    LaunchedEffect(state) {
        val completed = state as? PtModeState.Completed ?: return@LaunchedEffect
        onSessionCompleted(completed.planDayId)
    }

    KeepScreenOn()

    Box(modifier = Modifier.fillMaxSize().background(NuFitColors.Primary)) {
        when (val current = state) {
            PtModeState.Loading -> LoadingBox()
            is PtModeState.Completed -> LoadingBox() // brief -- LaunchedEffect navigates away
            is PtModeState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(current.message, color = Color.White) }
            is PtModeState.Active -> {
                if (showEndSessionDialog) {
                    EndSessionDialog(
                        onContinueLater = { showEndSessionDialog = false; onExit() },
                        onEndNow = { showEndSessionDialog = false; viewModel.onEvent(PtModeEvent.EndSessionNow) },
                    )
                }
                if (current.swapSheetVisible) {
                    ExerciseSwapDialog(
                        candidates = current.swapCandidates,
                        onSelect = { exerciseId -> viewModel.onEvent(PtModeEvent.SelectSwapCandidate(exerciseId)) },
                        onDismiss = { viewModel.onEvent(PtModeEvent.CloseSwapSheet) },
                    )
                }
                PtModeContent(
                    state = current,
                    onWeightChange = { delta -> viewModel.onEvent(PtModeEvent.AdjustWeight(delta)) },
                    onRepsChange = { delta -> viewModel.onEvent(PtModeEvent.AdjustReps(delta)) },
                    onConfirmSet = { viewModel.onEvent(PtModeEvent.ConfirmSet) },
                    onSkipRest = { viewModel.onEvent(PtModeEvent.SkipRest) },
                    onSwapExercise = { viewModel.onEvent(PtModeEvent.OpenSwapSheet) },
                    onSkipExercise = { viewModel.onEvent(PtModeEvent.SkipExercise) },
                    onPauseClick = { showEndSessionDialog = true },
                )
            }
        }
    }
}

@Composable
private fun PtModeContent(
    state: PtModeState.Active,
    onWeightChange: (Double) -> Unit,
    onRepsChange: (Int) -> Unit,
    onConfirmSet: () -> Unit,
    onSkipRest: () -> Unit,
    onSwapExercise: () -> Unit,
    onSkipExercise: () -> Unit,
    onPauseClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            exerciseIndex = state.session.currentExerciseIndex + 1,
            totalExercises = state.totalExerciseCount,
            onPauseClick = onPauseClick,
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        ) {
            item {
                ExerciseMediaCard(
                    exerciseName = state.currentExercise.name,
                    restRemainingSeconds = state.restRemainingSeconds,
                    onSkipRest = onSkipRest,
                )
            }
            item {
                Column(modifier = Modifier.padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(state.currentExercise.name, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text(
                        stringResource(Res.string.ptmode_target_line, state.currentPlanned.sets, state.currentPlanned.repRangeOrDuration, formatWeightKg(state.weightKg)),
                        style = MaterialTheme.typography.titleMedium,
                        color = NuFitColors.InversePrimary,
                    )
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(percent = 50))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                    ) {
                        Text(
                            stringResource(Res.string.ptmode_set_counter, state.session.currentSetNumber, state.currentPlanned.sets),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                        )
                    }
                }
            }
            item {
                SetInputSteppers(
                    weightKg = state.weightKg,
                    reps = state.reps,
                    onWeightChange = onWeightChange,
                    onRepsChange = onRepsChange,
                    modifier = Modifier.padding(top = 24.dp),
                )
            }
            item {
                AppButton(
                    onClick = onConfirmSet,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = NuFitColors.Primary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                ) { Text(stringResource(Res.string.ptmode_confirm_set_button)) }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(onClick = onSwapExercise) {
                        Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                        Text(stringResource(Res.string.ptmode_swap_exercise_button), color = Color.White.copy(alpha = 0.8f))
                    }
                    TextButton(onClick = onSkipExercise) {
                        Text(stringResource(Res.string.ptmode_skip_exercise_button), color = Color.White.copy(alpha = 0.8f))
                        Icon(Icons.Filled.SkipNext, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(exerciseIndex: Int, totalExercises: Int, onPauseClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                stringResource(Res.string.ptmode_top_bar_eyebrow).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = NuFitColors.InversePrimary,
            )
            Text(
                stringResource(Res.string.ptmode_top_bar_progress, exerciseIndex, totalExercises),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
        }
        IconButton(onClick = onPauseClick) {
            Icon(Icons.Filled.Pause, contentDescription = stringResource(Res.string.ptmode_pause_content_description), tint = Color.White)
        }
    }
}

@Composable
private fun ExerciseMediaCard(exerciseName: String, restRemainingSeconds: Int?, onSkipRest: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    stringResource(Res.string.ptmode_active_set_label, exerciseName),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                )
                if (restRemainingSeconds != null) {
                    RestTimerBadge(restRemainingSeconds, onSkipRest)
                }
            }
        }
    }
}

@Composable
private fun RestTimerBadge(remainingSeconds: Int, onSkipRest: () -> Unit) {
    Column(horizontalAlignment = Alignment.End) {
        Text(stringResource(Res.string.ptmode_rest_timer_label), style = MaterialTheme.typography.labelSmall, color = NuFitColors.InversePrimary)
        Text(
            stringResource(Res.string.ptmode_rest_timer_value, remainingSeconds / 60, remainingSeconds % 60),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )
        TextButton(onClick = onSkipRest) {
            Text(stringResource(Res.string.ptmode_skip_rest_button), style = MaterialTheme.typography.labelSmall, color = NuFitColors.InversePrimary)
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color.White)
    }
}
