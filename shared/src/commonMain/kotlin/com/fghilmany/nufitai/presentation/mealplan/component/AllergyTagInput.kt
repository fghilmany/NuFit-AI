package com.fghilmany.nufitai.presentation.mealplan.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.core.designsystem.component.AppTextField
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.mealplan_preference_allergy_add
import nufitai.shared.generated.resources.mealplan_preference_allergy_placeholder
import org.jetbrains.compose.resources.stringResource

/** P-17 B "Alergi & Pantangan" -- text input + Tambah, entries render as removable red pills. */
@Composable
fun AllergyTagInput(
    allergies: List<String>,
    draft: String,
    onDraftChange: (String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            AppTextField(
                value = draft,
                onValueChange = onDraftChange,
                placeholder = stringResource(Res.string.mealplan_preference_allergy_placeholder),
                modifier = Modifier.weight(1f),
            )
            AppButton(text = stringResource(Res.string.mealplan_preference_allergy_add), onClick = onAdd)
        }
        if (allergies.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allergies) { allergy ->
                    AssistChip(
                        onClick = { onRemove(allergy) },
                        label = { Text(allergy) },
                        trailingIcon = { Icon(Icons.Filled.Close, contentDescription = null) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = NuFitColors.ErrorContainer, labelColor = NuFitColors.OnErrorContainer),
                    )
                }
            }
        }
    }
}
