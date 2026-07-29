package com.fghilmany.nufitai.presentation.profilesettings.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.domain.language.entity.AppLanguage
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.common_action_cancel
import nufitai.shared.generated.resources.profilesettings_language_english
import nufitai.shared.generated.resources.profilesettings_language_indonesian
import nufitai.shared.generated.resources.profilesettings_language_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun LanguageSelectDialog(current: AppLanguage, onSelect: (AppLanguage) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.profilesettings_language_title)) },
        text = {
            Column {
                AppLanguage.entries.forEach { language ->
                    Row(
                        modifier = Modifier.selectable(selected = language == current, onClick = { onSelect(language) }),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = language == current, onClick = { onSelect(language) })
                        Text(
                            stringResource(
                                if (language == AppLanguage.INDONESIAN) Res.string.profilesettings_language_indonesian else Res.string.profilesettings_language_english,
                            ),
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_action_cancel)) } },
    )
}
