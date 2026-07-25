package com.fghilmany.nufitai.presentation.ptmode.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.domain.exerciselibrary.entity.Exercise
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.common_action_cancel
import nufitai.shared.generated.resources.ptmode_swap_sheet_empty
import nufitai.shared.generated.resources.ptmode_swap_sheet_title
import org.jetbrains.compose.resources.stringResource

/** issue #80 UC-2 "Ganti gerakan" -- candidates come from the existing GetExerciseAlternatives usecase. */
@Composable
fun ExerciseSwapDialog(candidates: List<Exercise>, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.ptmode_swap_sheet_title)) },
        text = {
            if (candidates.isEmpty()) {
                Text(stringResource(Res.string.ptmode_swap_sheet_empty))
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                    items(candidates) { exercise ->
                        Text(
                            exercise.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(exercise.id) }.padding(vertical = 12.dp),
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_action_cancel)) } },
    )
}
