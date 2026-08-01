package com.fghilmany.nufitai.presentation.mealplan.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppElevatedCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.mealplan.entity.MealMood
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.mealplan_preference_mood_cepat
import nufitai.shared.generated.resources.mealplan_preference_mood_comfort
import nufitai.shared.generated.resources.mealplan_preference_mood_pedas
import nufitai.shared.generated.resources.mealplan_preference_mood_sehat
import org.jetbrains.compose.resources.stringResource

@Composable
private fun MealMood.label(): String = when (this) {
    MealMood.CEPAT -> stringResource(Res.string.mealplan_preference_mood_cepat)
    MealMood.COMFORT -> stringResource(Res.string.mealplan_preference_mood_comfort)
    MealMood.SEHAT -> stringResource(Res.string.mealplan_preference_mood_sehat)
    MealMood.PEDAS -> stringResource(Res.string.mealplan_preference_mood_pedas)
}

/** P-17 B "Suasana Hati (Mood)" -- 2x2 single-select grid. */
@Composable
fun MoodGrid(selected: MealMood?, onSelect: (MealMood) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MealMood.entries.chunked(2).forEach { rowMoods ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                rowMoods.forEach { mood ->
                    MoodCard(mood = mood, isSelected = mood == selected, onClick = { onSelect(mood) }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MoodCard(mood: MealMood, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppElevatedCard(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        border = if (isSelected) BorderStroke(2.dp, NuFitColors.Primary) else null,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(mood.label(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
