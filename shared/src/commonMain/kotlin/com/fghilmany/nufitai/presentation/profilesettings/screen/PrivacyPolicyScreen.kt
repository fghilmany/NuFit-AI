package com.fghilmany.nufitai.presentation.profilesettings.screen

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
import nufitai.shared.generated.resources.profilesettings_privacy_body
import nufitai.shared.generated.resources.profilesettings_privacy_top_bar_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        OnboardingTopBar(title = stringResource(Res.string.profilesettings_privacy_top_bar_title), onBack = onBack)
        Text(
            stringResource(Res.string.profilesettings_privacy_body),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),
        )
    }
}
