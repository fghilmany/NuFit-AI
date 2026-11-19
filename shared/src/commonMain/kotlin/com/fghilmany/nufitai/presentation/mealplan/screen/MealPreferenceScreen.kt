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
import androidx.compose.foundation.layout.Box
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
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.core.designsystem.component.AppTextField
import com.fghilmany.nufitai.presentation.mealplan.component.AllergyTagInput
import com.fghilmany.nufitai.presentation.mealplan.component.CuisineChipRow
import com.fghilmany.nufitai.presentation.mealplan.component.MoodGrid
import com.fghilmany.nufitai.presentation.mealplan.viewmodel.MealPreferenceViewModel
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.mealplan_preference_allergy_title
import nufitai.shared.generated.resources.mealplan_preference_cuisine_subtitle
import nufitai.shared.generated.resources.mealplan_preference_cuisine_title
import nufitai.shared.generated.resources.mealplan_preference_mood_title
import nufitai.shared.generated.resources.mealplan_preference_notes_placeholder
import nufitai.shared.generated.resources.mealplan_preference_notes_title
import nufitai.shared.generated.resources.mealplan_preference_save_cta
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
                Text(stringResource(Res.string.mealplan_preference_notes_title), style = MaterialTheme.typography.titleSmall)
                AppTextField(
                    value = state.notes,
                    onValueChange = viewModel::onNotesChange,
                    placeholder = stringResource(Res.string.mealplan_preference_notes_placeholder),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Dev report (18 Nov 2026): button was correctly disabled while isSaving, but nothing
            // indicated why -- no spinner anywhere, just a dead button.
            if (state.isSaving) {
                Box(Modifier.fillMaxWidth(), Alignment.Center) { CircularProgressIndicator() }
            } else {
                AppButton(
                    text = stringResource(Res.string.mealplan_preference_save_cta),
                    onClick = viewModel::onSave,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
