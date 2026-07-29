package com.fghilmany.nufitai.presentation.profilesettings.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.presentation.profilesettings.viewmodel.DeleteAccountDialogState
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.common_action_cancel
import nufitai.shared.generated.resources.profilesettings_delete_confirm_action
import nufitai.shared.generated.resources.profilesettings_delete_confirm_body
import nufitai.shared.generated.resources.profilesettings_delete_confirm_title
import org.jetbrains.compose.resources.stringResource

/**
 * techspec profile-settings.md §8/§9 item 6/8 -- `Confirming` and `Deleting` share this dialog
 * (state-hoisted, no local visibility flag); `Failed` re-shows the same dialog with the error
 * appended so the user can retry without losing context. Body copy makes the "local data kept"
 * nuance (§9 item 8) explicit.
 */
@Composable
fun DeleteAccountConfirmDialog(state: DeleteAccountDialogState, onConfirm: () -> Unit, onCancel: () -> Unit) {
    if (state is DeleteAccountDialogState.Hidden) return

    val isDeleting = state is DeleteAccountDialogState.Deleting
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onCancel() },
        title = { Text(stringResource(Res.string.profilesettings_delete_confirm_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.profilesettings_delete_confirm_body))
                if (isDeleting) CircularProgressIndicator()
                if (state is DeleteAccountDialogState.Failed) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isDeleting) {
                Text(stringResource(Res.string.profilesettings_delete_confirm_action), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel, enabled = !isDeleting) { Text(stringResource(Res.string.common_action_cancel)) }
        },
    )
}
