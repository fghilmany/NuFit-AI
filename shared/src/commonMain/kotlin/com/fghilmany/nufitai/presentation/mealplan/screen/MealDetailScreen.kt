package com.fghilmany.nufitai.presentation.mealplan.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.mealplan.entity.MealLogStatus
import com.fghilmany.nufitai.presentation.mealplan.component.label
import com.fghilmany.nufitai.presentation.mealplan.viewmodel.MealDetailState
import com.fghilmany.nufitai.presentation.mealplan.viewmodel.MealDetailViewModel
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.mealplan_detail_calories_label
import nufitai.shared.generated.resources.mealplan_detail_ingredients_title
import nufitai.shared.generated.resources.mealplan_detail_mark_eaten
import nufitai.shared.generated.resources.mealplan_detail_mark_skipped
import nufitai.shared.generated.resources.mealplan_detail_status_eaten
import nufitai.shared.generated.resources.mealplan_detail_status_skipped
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** P-17 detail + AC-7 (BLOCKING) eaten/skip logging. No screenshot -- mirrors `ExerciseDetailScreen`'s layout conventions. */
@Composable
fun MealDetailScreen(plannedMealId: String, onBack: () -> Unit, viewModel: MealDetailViewModel = koinViewModel()) {
    LaunchedEffect(plannedMealId) { viewModel.load(plannedMealId) }
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
        }

        when (val current = state) {
            MealDetailState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            is MealDetailState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(current.message) }
            is MealDetailState.Loaded -> Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(current.plannedMeal.slot.label(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(current.plannedMeal.name, style = MaterialTheme.typography.headlineSmall, color = NuFitColors.Primary)
                Text(current.plannedMeal.description, style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(
                        Res.string.mealplan_detail_calories_label,
                        current.plannedMeal.macros.calories,
                        current.plannedMeal.macros.proteinG,
                        current.plannedMeal.macros.carbG,
                        current.plannedMeal.macros.fatG,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )

                Text(stringResource(Res.string.mealplan_detail_ingredients_title), style = MaterialTheme.typography.titleSmall)
                current.plannedMeal.ingredients.forEach { ingredient -> Text("• $ingredient", style = MaterialTheme.typography.bodyMedium) }

                when (current.status) {
                    MealLogStatus.PENDING -> {
                        AppButton(text = stringResource(Res.string.mealplan_detail_mark_eaten), onClick = { viewModel.onSetStatus(MealLogStatus.EATEN) }, modifier = Modifier.fillMaxWidth())
                        AppButton(text = stringResource(Res.string.mealplan_detail_mark_skipped), onClick = { viewModel.onSetStatus(MealLogStatus.SKIPPED) }, modifier = Modifier.fillMaxWidth())
                    }
                    MealLogStatus.EATEN -> Text(stringResource(Res.string.mealplan_detail_status_eaten), style = MaterialTheme.typography.titleSmall, color = NuFitColors.Secondary)
                    MealLogStatus.SKIPPED -> Text(stringResource(Res.string.mealplan_detail_status_skipped), style = MaterialTheme.typography.titleSmall, color = NuFitColors.Error)
                }
            }
        }
    }
}
