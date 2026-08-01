package com.fghilmany.nufitai.presentation.mealplan.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.mealplan_regenerate_suggestion_body
import nufitai.shared.generated.resources.mealplan_regenerate_suggestion_cta
import org.jetbrains.compose.resources.stringResource

/** AC-9 -- no screenshot; dev-approved design decision. Dismissible, never auto-triggers regeneration. */
@Composable
fun RegenerateSuggestionBanner(onRegenerateClick: () -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = NuFitColors.WarningContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.mealplan_regenerate_suggestion_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = NuFitColors.OnWarningContainerBody,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onDismiss) { Text("×") }
            }
            TextButton(onClick = onRegenerateClick) { Text(stringResource(Res.string.mealplan_regenerate_suggestion_cta)) }
        }
    }
}
