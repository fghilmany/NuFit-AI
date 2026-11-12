package com.fghilmany.nufitai.presentation.profilesettings.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.presentation.profilesettings.viewmodel.SignOutDialogState
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.profilesettings_signout_sync_failed_anyway
import nufitai.shared.generated.resources.profilesettings_signout_sync_failed_body
import nufitai.shared.generated.resources.profilesettings_signout_sync_failed_retry
import nufitai.shared.generated.resources.profilesettings_signout_sync_failed_title
import org.jetbrains.compose.resources.stringResource

/** Shown when sign-out's pre-wipe sync fails (dev decision 2026-11-12: sync before clearing local
 * data, never clear on a failed sync silently) -- lets the user retry (needs network) or confirm
 * signing out anyway, which skips the sync and wipes local data unconditionally. */
@Composable
fun SignOutSyncFailedDialog(state: SignOutDialogState, onRetry: () -> Unit, onSignOutAnyway: () -> Unit, onCancel: () -> Unit) {
    if (state !is SignOutDialogState.SyncFailedConfirm) return

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(stringResource(Res.string.profilesettings_signout_sync_failed_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.profilesettings_signout_sync_failed_body))
                Text(state.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = onSignOutAnyway) {
                Text(stringResource(Res.string.profilesettings_signout_sync_failed_anyway), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onRetry) { Text(stringResource(Res.string.profilesettings_signout_sync_failed_retry)) }
        },
    )
}
