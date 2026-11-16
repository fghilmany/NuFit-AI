package com.fghilmany.nufitai.presentation.onboarding.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.onboarding_entry_body
import nufitai.shared.generated.resources.onboarding_entry_cta_login
import nufitai.shared.generated.resources.onboarding_entry_cta_quick_assessment
import nufitai.shared.generated.resources.onboarding_entry_title
import org.jetbrains.compose.resources.stringResource

/** Shown right after Splash for a brand-new user -- login opsional (keputusan #2), so this is
 * a real fork, not a soft nudge toward one path. Stateless -- pure navigation, no ViewModel. */
@Composable
fun EntryChoiceScreen(
    onLoginClick: () -> Unit,
    onQuickAssessmentClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Column(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Box(
                modifier = Modifier.size(96.dp).background(NuFitColors.Primary, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.onboarding_entry_title), style = MaterialTheme.typography.headlineMedium, color = NuFitColors.Primary)
                Text(
                    stringResource(Res.string.onboarding_entry_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppButton(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.onboarding_entry_cta_login))
                }
                OutlinedButton(onClick = onQuickAssessmentClick, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.onboarding_entry_cta_quick_assessment))
                }
            }
        }
    }
}
