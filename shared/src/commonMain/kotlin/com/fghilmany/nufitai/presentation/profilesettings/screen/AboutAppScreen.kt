package com.fghilmany.nufitai.presentation.profilesettings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.presentation.onboarding.component.OnboardingTopBar
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.profilesettings_about_body
import nufitai.shared.generated.resources.profilesettings_about_top_bar_title
import nufitai.shared.generated.resources.profilesettings_about_version
import org.jetbrains.compose.resources.stringResource

private const val APP_VERSION = "1.0.0"

@Composable
fun AboutAppScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        OnboardingTopBar(title = stringResource(Res.string.profilesettings_about_top_bar_title), onBack = onBack)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(Res.string.profilesettings_about_body), style = MaterialTheme.typography.bodyMedium)
            Text(
                stringResource(Res.string.profilesettings_about_version, APP_VERSION),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
