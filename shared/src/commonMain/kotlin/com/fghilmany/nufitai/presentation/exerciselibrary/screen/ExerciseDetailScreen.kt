package com.fghilmany.nufitai.presentation.exerciselibrary.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.presentation.exerciselibrary.component.AlternativesRow
import com.fghilmany.nufitai.presentation.exerciselibrary.component.CommonMistakesCard
import com.fghilmany.nufitai.presentation.exerciselibrary.component.InstructionsCard
import com.fghilmany.nufitai.presentation.exerciselibrary.component.SafetyTipsCard
import com.fghilmany.nufitai.presentation.exerciselibrary.component.TargetMuscleChips
import com.fghilmany.nufitai.presentation.exerciselibrary.viewmodel.ExerciseDetailState
import com.fghilmany.nufitai.presentation.exerciselibrary.viewmodel.ExerciseDetailViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * P-08 (issue #79, Figma node 12:835). Full-screen route only this pass -- PT Mode's
 * sheet/overlay entry point (UC-2 alt flow) is deferred, `05-pt-mode.md` doesn't exist yet.
 */
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    onBack: () -> Unit,
    onAlternativeClick: (exerciseId: String) -> Unit,
    viewModel: ExerciseDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(exerciseId) { viewModel.load(exerciseId) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(onBack = onBack)
        Box(modifier = Modifier.weight(1f)) {
            when (val current = state) {
                ExerciseDetailState.Loading -> LoadingBox()
                is ExerciseDetailState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(current.message) }
                is ExerciseDetailState.Loaded -> DetailContent(current, onAlternativeClick)
            }
        }
    }
}

@Composable
private fun DetailContent(state: ExerciseDetailState.Loaded, onAlternativeClick: (String) -> Unit) {
    val exercise = state.exercise
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            // No bundled photo assets yet -- placeholder tint (§9 item 11, same fallback as monthly-plan.md).
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.weight(1f).height(160.dp).background(NuFitColors.SurfaceContainerHigh, RoundedCornerShape(16.dp)),
                )
                Box(
                    modifier = Modifier.weight(1f).height(160.dp).background(NuFitColors.SurfaceContainerHigh, RoundedCornerShape(16.dp)),
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Otot Target", style = MaterialTheme.typography.titleLarge, color = NuFitColors.Primary)
                TargetMuscleChips(exercise.targetMusclesPrimary)
            }
        }

        item { InstructionsCard(exercise.instructions) }
        item { CommonMistakesCard(exercise.commonMistakes) }
        item { SafetyTipsCard(exercise.safetyTips) }
        item { AlternativesRow(state.alternatives, onAlternativeClick) }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali", tint = NuFitColors.Primary)
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
