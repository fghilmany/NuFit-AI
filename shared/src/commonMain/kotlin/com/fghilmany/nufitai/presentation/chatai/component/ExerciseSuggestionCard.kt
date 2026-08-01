package com.fghilmany.nufitai.presentation.chatai.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.core.designsystem.component.AppCard
import com.fghilmany.nufitai.core.designsystem.component.AppElevatedCard
import com.fghilmany.nufitai.core.designsystem.component.BundledMediaImage
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import com.fghilmany.nufitai.presentation.exerciselibrary.component.shortLabel
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.chatai_suggestion_apply_cta
import nufitai.shared.generated.resources.chatai_suggestion_apply_error_not_found
import nufitai.shared.generated.resources.chatai_suggestion_apply_success
import nufitai.shared.generated.resources.chatai_suggestion_ask_more_cta
import org.jetbrains.compose.resources.stringResource

/**
 * Screenshot §2 (chat-ai.md) -- embedded in an assistant bubble. AC-4 (BLOCKING): "Terapkan ke
 * Plan" only shown when [replacesExerciseId] is non-null (ExerciseSuggestion.replacesExerciseId
 * = null means informational-only per the domain model's own doc comment).
 */
@Composable
fun ExerciseSuggestionCard(
    exercise: Exercise,
    replacesExerciseId: String?,
    applyState: ApplyCardState,
    onApplyToPlan: () -> Unit,
    onAskMore: () -> Unit,
    onExerciseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppElevatedCard(modifier = modifier.fillMaxWidth(), onClick = onExerciseClick) {
        Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))) {
            BundledMediaImage(resourcePath = exercise.imagePath, contentDescription = exercise.name, modifier = Modifier.fillMaxWidth().height(120.dp))
        }
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Badge(exercise.level.shortLabel())
                Badge(exercise.equipmentCategory.shortLabel())
            }
            Text(exercise.name, style = MaterialTheme.typography.titleMedium, color = NuFitColors.Primary)

            when (applyState) {
                is ApplyCardState.Applied -> Text(stringResource(Res.string.chatai_suggestion_apply_success), style = MaterialTheme.typography.bodySmall, color = NuFitColors.Secondary)
                is ApplyCardState.Failed -> Text(stringResource(Res.string.chatai_suggestion_apply_error_not_found), style = MaterialTheme.typography.bodySmall, color = NuFitColors.Error)
                ApplyCardState.Idle -> Unit
            }

            if (replacesExerciseId != null && applyState !is ApplyCardState.Applied) {
                AppButton(text = stringResource(Res.string.chatai_suggestion_apply_cta), onClick = onApplyToPlan, modifier = Modifier.fillMaxWidth())
            }
            AppButton(
                onClick = onAskMore,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NuFitColors.SecondaryContainer, contentColor = NuFitColors.OnSecondaryContainer),
            ) {
                Text(stringResource(Res.string.chatai_suggestion_ask_more_cta))
            }
        }
    }
}

sealed interface ApplyCardState {
    data object Idle : ApplyCardState
    data object Applied : ApplyCardState
    data object Failed : ApplyCardState
}

@Composable
private fun Badge(label: String) {
    AppCard(
        backgroundColor = NuFitColors.SecondaryContainer,
        shape = RoundedCornerShape(percent = 50),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = NuFitColors.OnSecondaryContainer)
    }
}
