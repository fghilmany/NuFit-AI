package com.fghilmany.nufitai.presentation.monthlyplan.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.core.designsystem.component.AppElevatedCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlannedExercise
import com.fghilmany.nufitai.presentation.monthlyplan.component.estimatedDurationMinutes
import com.fghilmany.nufitai.presentation.monthlyplan.component.exerciseCount
import com.fghilmany.nufitai.presentation.monthlyplan.viewmodel.SessionDetailState
import com.fghilmany.nufitai.presentation.monthlyplan.viewmodel.SessionDetailViewModel
import com.fghilmany.nufitai.presentation.onboarding.component.OnboardingTopBar
import com.fghilmany.nufitai.presentation.onboarding.component.TipsBanner
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.monthlyplan_duration_minutes_label
import nufitai.shared.generated.resources.monthlyplan_exercise_count_label
import nufitai.shared.generated.resources.monthlyplan_session_detail_description_fallback
import nufitai.shared.generated.resources.monthlyplan_session_detail_description_templated
import nufitai.shared.generated.resources.monthlyplan_session_detail_exercise_meta
import nufitai.shared.generated.resources.monthlyplan_session_detail_main_exercises_title
import nufitai.shared.generated.resources.monthlyplan_session_detail_start_button
import nufitai.shared.generated.resources.monthlyplan_session_detail_tip_body
import nufitai.shared.generated.resources.monthlyplan_session_detail_tip_title
import nufitai.shared.generated.resources.monthlyplan_session_detail_top_bar_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * issue #76 P-04. The Figma pull for this node hit the plugin's Figma-MCP rate limit
 * mid-implementation -- built from the techspec's textual description (header, duration+
 * exercise-count chip, session description, exercise cards, "Saran NuFit AI" tip card, sticky
 * "Mulai Sesi Ini" CTA) plus the visual language already confirmed on P-03's real pulled design
 * (same card/color/type tokens), not guessed from scratch. Flag for a follow-up pixel pass once
 * the rate limit clears.
 */
@Composable
fun SessionDetailScreen(
    planDayId: String,
    onBack: () -> Unit,
    onStartSession: (String) -> Unit,
    viewModel: SessionDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(planDayId) { viewModel.load(planDayId) }

    Column(modifier = Modifier.fillMaxSize()) {
        OnboardingTopBar(title = stringResource(Res.string.monthlyplan_session_detail_top_bar_title), onBack = onBack)
        Box(modifier = Modifier.weight(1f)) {
            when (val current = state) {
                SessionDetailState.Loading -> LoadingBox()
                is SessionDetailState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(current.message) }
                is SessionDetailState.Loaded -> SessionDetailContent(current, onStartSession = { onStartSession(planDayId) })
            }
        }
    }
}

@Composable
private fun SessionDetailContent(state: SessionDetailState.Loaded, onStartSession: () -> Unit) {
    val day = state.planDay
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SummaryChip(Icons.Filled.FitnessCenter, stringResource(Res.string.monthlyplan_exercise_count_label, day.exerciseCount()))
                    SummaryChip(Icons.Filled.Timer, stringResource(Res.string.monthlyplan_duration_minutes_label, day.estimatedDurationMinutes()))
                }
            }
            item {
                Text(
                    day.templateLetter?.let { stringResource(Res.string.monthlyplan_session_detail_description_templated, it) }
                        ?: stringResource(Res.string.monthlyplan_session_detail_description_fallback),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val mainExercises = day.mainExercises.orEmpty()
            if (mainExercises.isNotEmpty()) {
                item { Text(stringResource(Res.string.monthlyplan_session_detail_main_exercises_title), style = MaterialTheme.typography.titleLarge) }
                items(mainExercises) { exercise -> ExerciseRow(exercise, state.exercisesById[exercise.exerciseId]?.name ?: exercise.exerciseId) }
            }
            item {
                TipsBanner(
                    title = stringResource(Res.string.monthlyplan_session_detail_tip_title),
                    body = stringResource(Res.string.monthlyplan_session_detail_tip_body),
                    icon = Icons.Filled.FitnessCenter,
                )
            }
        }
        AppButton(
            onClick = onStartSession,
            colors = ButtonDefaults.buttonColors(containerColor = NuFitColors.PrimaryContainer),
            modifier = Modifier.fillMaxWidth().padding(24.dp),
        ) { Text(stringResource(Res.string.monthlyplan_session_detail_start_button)) }
    }
}

@Composable
private fun SummaryChip(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(percent = 50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
private fun ExerciseRow(exercise: PlannedExercise, name: String) {
    AppElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(12.dp)))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(
                        Res.string.monthlyplan_session_detail_exercise_meta,
                        exercise.sets,
                        exercise.repRangeOrDuration,
                        exercise.rpeTargetMin,
                        exercise.rpeTargetMax,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
