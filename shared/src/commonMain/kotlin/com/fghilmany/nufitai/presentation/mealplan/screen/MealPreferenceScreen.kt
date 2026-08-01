package com.fghilmany.nufitai.presentation.mealplan.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.domain.mealplan.entity.MealScheduleMode
import com.fghilmany.nufitai.presentation.mealplan.component.AllergyTagInput
import com.fghilmany.nufitai.presentation.mealplan.component.CuisineChipRow
import com.fghilmany.nufitai.presentation.mealplan.component.MoodGrid
import com.fghilmany.nufitai.presentation.mealplan.component.ScheduleModeCard
import com.fghilmany.nufitai.presentation.mealplan.viewmodel.MealPreferenceViewModel
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.mealplan_preference_allergy_title
import nufitai.shared.generated.resources.mealplan_preference_cuisine_subtitle
import nufitai.shared.generated.resources.mealplan_preference_cuisine_title
import nufitai.shared.generated.resources.mealplan_preference_mood_title
import nufitai.shared.generated.resources.mealplan_preference_save_cta
import nufitai.shared.generated.resources.mealplan_preference_schedule_ai_random_body
import nufitai.shared.generated.resources.mealplan_preference_schedule_ai_random_title
import nufitai.shared.generated.resources.mealplan_preference_schedule_manual_body
import nufitai.shared.generated.resources.mealplan_preference_schedule_manual_title
import nufitai.shared.generated.resources.mealplan_preference_schedule_title
import nufitai.shared.generated.resources.mealplan_preference_top_bar_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** P-17 B (AC-5). */
@Composable
fun MealPreferenceScreen(onBack: () -> Unit, viewModel: MealPreferenceViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
            Text(stringResource(Res.string.mealplan_preference_top_bar_title), style = MaterialTheme.typography.titleLarge)
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.mealplan_preference_cuisine_title), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(Res.string.mealplan_preference_cuisine_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                CuisineChipRow(selected = state.cuisineThemes, onToggle = viewModel::onToggleCuisineTheme)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.mealplan_preference_mood_title), style = MaterialTheme.typography.titleSmall)
                MoodGrid(selected = state.mood, onSelect = viewModel::onSetMood)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.mealplan_preference_allergy_title), style = MaterialTheme.typography.titleSmall)
                AllergyTagInput(
                    allergies = state.allergies,
                    draft = state.allergyDraft,
                    onDraftChange = viewModel::onAllergyDraftChange,
                    onAdd = viewModel::onAddAllergy,
                    onRemove = viewModel::onRemoveAllergy,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.mealplan_preference_schedule_title), style = MaterialTheme.typography.titleSmall)
                ScheduleModeCard(
                    title = stringResource(Res.string.mealplan_preference_schedule_ai_random_title),
                    body = stringResource(Res.string.mealplan_preference_schedule_ai_random_body),
                    isSelected = state.scheduleMode == MealScheduleMode.AI_RANDOM,
                    onClick = { viewModel.onSetScheduleMode(MealScheduleMode.AI_RANDOM) },
                )
                ScheduleModeCard(
                    title = stringResource(Res.string.mealplan_preference_schedule_manual_title),
                    body = stringResource(Res.string.mealplan_preference_schedule_manual_body),
                    isSelected = state.scheduleMode == MealScheduleMode.MANUAL_PER_DAY,
                    onClick = { viewModel.onSetScheduleMode(MealScheduleMode.MANUAL_PER_DAY) },
                )
            }

            state.error?.let { error -> Text(error, color = MaterialTheme.colorScheme.error) }

            AppButton(
                text = stringResource(Res.string.mealplan_preference_save_cta),
                onClick = viewModel::onSave,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
