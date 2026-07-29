package com.fghilmany.nufitai.presentation.profilesettings.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppTextField
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.common_action_cancel
import nufitai.shared.generated.resources.common_action_save
import nufitai.shared.generated.resources.profilesettings_edit_body_data_height_label
import nufitai.shared.generated.resources.profilesettings_edit_body_data_title
import nufitai.shared.generated.resources.profilesettings_edit_body_data_weight_label
import org.jetbrains.compose.resources.stringResource

/** techspec profile-settings.md §9 item 3 -- lightweight dialog, not the full onboarding wizard. */
@Composable
fun BodyDataEditDialog(
    initialHeightCm: Double?,
    initialWeightKg: Double?,
    onDismiss: () -> Unit,
    onSave: (heightCm: Double?, weightKg: Double?) -> Unit,
) {
    var heightInput by remember { mutableStateOf(initialHeightCm?.toString() ?: "") }
    var weightInput by remember { mutableStateOf(initialWeightKg?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.profilesettings_edit_body_data_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it },
                    label = stringResource(Res.string.profilesettings_edit_body_data_height_label),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                AppTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = stringResource(Res.string.profilesettings_edit_body_data_weight_label),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(heightInput.toDoubleOrNull(), weightInput.toDoubleOrNull()) }) {
                Text(stringResource(Res.string.common_action_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_action_cancel)) } },
    )
}
