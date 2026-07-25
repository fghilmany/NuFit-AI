package com.fghilmany.nufitai.presentation.ptmode.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.ptmode_end_session_continue_later
import nufitai.shared.generated.resources.ptmode_end_session_dialog_body
import nufitai.shared.generated.resources.ptmode_end_session_dialog_title
import nufitai.shared.generated.resources.ptmode_end_session_end_now
import org.jetbrains.compose.resources.stringResource

/** issue #80 AC-8/edge-case#3 -- reuses P-05's Pause icon as the entry point (not a second icon not in the mock). */
@Composable
fun EndSessionDialog(onContinueLater: () -> Unit, onEndNow: () -> Unit) {
    AlertDialog(
        onDismissRequest = onContinueLater,
        title = { Text(stringResource(Res.string.ptmode_end_session_dialog_title)) },
        text = { Text(stringResource(Res.string.ptmode_end_session_dialog_body)) },
        confirmButton = { TextButton(onClick = onEndNow) { Text(stringResource(Res.string.ptmode_end_session_end_now)) } },
        dismissButton = { TextButton(onClick = onContinueLater) { Text(stringResource(Res.string.ptmode_end_session_continue_later)) } },
    )
}
