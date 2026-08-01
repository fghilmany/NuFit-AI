package com.fghilmany.nufitai.presentation.mealplan.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppCard
import com.fghilmany.nufitai.core.designsystem.component.AppElevatedCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.mealplan.entity.MealSlot
import com.fghilmany.nufitai.domain.mealplan.entity.PlannedMeal
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.mealplan_prep_minutes_label
import nufitai.shared.generated.resources.mealplan_slot_breakfast
import nufitai.shared.generated.resources.mealplan_slot_dinner
import nufitai.shared.generated.resources.mealplan_slot_lunch
import org.jetbrains.compose.resources.stringResource

@Composable
fun MealSlot.label(): String = when (this) {
    MealSlot.BREAKFAST -> stringResource(Res.string.mealplan_slot_breakfast)
    MealSlot.LUNCH -> stringResource(Res.string.mealplan_slot_lunch)
    MealSlot.DINNER -> stringResource(Res.string.mealplan_slot_dinner)
}

/** Screenshot §2 (meal-plan.md) -- text-only card (no photo, per dev decision: no image-sourcing pipeline exists). */
@Composable
fun MealCard(meal: PlannedMeal, onClick: () -> Unit, modifier: Modifier = Modifier) {
    AppElevatedCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(meal.slot.label(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(meal.name, style = MaterialTheme.typography.titleMedium, color = NuFitColors.Primary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(stringResource(Res.string.mealplan_prep_minutes_label, meal.prepMinutes))
                Pill(meal.tag)
            }
        }
    }
}

@Composable
private fun Pill(label: String) {
    AppCard(
        backgroundColor = NuFitColors.SecondaryContainer,
        shape = RoundedCornerShape(percent = 50),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = NuFitColors.OnSecondaryContainer)
    }
}
