package com.fghilmany.nufitai.presentation.auth.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppElevatedCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.core.util.isIOSPlatform
import com.fghilmany.nufitai.presentation.auth.viewmodel.LoginState
import com.fghilmany.nufitai.presentation.auth.viewmodel.LoginViewModel
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.auth_login_benefit_backup_body
import nufitai.shared.generated.resources.auth_login_benefit_backup_title
import nufitai.shared.generated.resources.auth_login_benefit_personalization_body
import nufitai.shared.generated.resources.auth_login_benefit_personalization_title
import nufitai.shared.generated.resources.auth_login_benefit_restore_body
import nufitai.shared.generated.resources.auth_login_benefit_restore_title
import nufitai.shared.generated.resources.auth_login_cta_apple
import nufitai.shared.generated.resources.auth_login_cta_google
import nufitai.shared.generated.resources.auth_login_legal
import nufitai.shared.generated.resources.auth_login_subtitle
import nufitai.shared.generated.resources.auth_login_title
import nufitai.shared.generated.resources.common_action_skip
import nufitai.shared.generated.resources.common_app_name
import nufitai.shared.generated.resources.ic_apple_logo
import nufitai.shared.generated.resources.ic_google_logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * P-10 (techspec §2, auth-sync.md, Figma node 12:1558). Google-only, no registration form
 * (locked decision #2) -- the decorative username/password card in the design is intentionally
 * NOT built. TODO(dev): the hero illustration (woman + AI robot high-fiving) isn't reproduced
 * here -- only a full-page mockup screenshot was available (the decorative form is baked into
 * its pixels), not a clean cropped export; swap the placeholder icon circle once one exists.
 * The Google/Apple marks ARE real (`ic_google_logo`/`ic_apple_logo`, dev-supplied SVGs).
 */
@Composable
fun LoginScreen(
    onSignedIn: (needsCompleteProfile: Boolean) -> Unit,
    onSkip: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        val current = state
        if (current is LoginState.Success) onSignedIn(current.needsCompleteProfile)
    }

    val backgroundBrush = Brush.verticalGradient(
        listOf(NuFitColors.SecondaryFixed.copy(alpha = 0.35f), MaterialTheme.colorScheme.background),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(backgroundBrush).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Text(
                stringResource(Res.string.common_app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NuFitColors.Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            // Placeholder for the hero illustration (see TODO above).
            AppElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier.size(96.dp).clip(CircleShape).background(NuFitColors.SecondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.FitnessCenter,
                            contentDescription = null,
                            tint = NuFitColors.OnSecondaryContainer,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(Res.string.auth_login_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    stringResource(Res.string.auth_login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item {
            AppElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    BenefitRow(
                        Icons.Filled.CloudUpload,
                        stringResource(Res.string.auth_login_benefit_backup_title),
                        stringResource(Res.string.auth_login_benefit_backup_body),
                    )
                    BenefitRow(
                        Icons.Filled.Psychology,
                        stringResource(Res.string.auth_login_benefit_personalization_title),
                        stringResource(Res.string.auth_login_benefit_personalization_body),
                    )
                    BenefitRow(
                        Icons.Filled.PhoneAndroid,
                        stringResource(Res.string.auth_login_benefit_restore_title),
                        stringResource(Res.string.auth_login_benefit_restore_body),
                    )
                }
            }
        }

        item {
            val current = state
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (current == LoginState.SigningIn) {
                    Box(Modifier.fillMaxWidth(), Alignment.Center) { CircularProgressIndicator() }
                } else {
                    OutlinedButton(
                        onClick = viewModel::signInWithGoogle,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NuFitColors.OnBackground),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(Res.drawable.ic_google_logo),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(stringResource(Res.string.auth_login_cta_google))
                        }
                    }
                    if (isIOSPlatform()) {
                        Button(
                            onClick = viewModel::signInWithApple,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(percent = 50),
                            colors = ButtonDefaults.buttonColors(containerColor = NuFitColors.OnBackground, contentColor = NuFitColors.Background),
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painterResource(Res.drawable.ic_apple_logo),
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(stringResource(Res.string.auth_login_cta_apple))
                            }
                        }
                    }
                }
                if (current is LoginState.Error) {
                    Text(current.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.common_action_skip), textDecoration = TextDecoration.Underline)
                }
                // DEBUG ONLY -- remove once real Google/Apple SDKs are wired (techspec §3).
                TextButton(onClick = viewModel::debugSignIn, modifier = Modifier.fillMaxWidth()) {
                    Text("[DEBUG] Simulate sign-in")
                }
            }
        }

        item {
            Text(
                stringResource(Res.string.auth_login_legal),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun BenefitRow(icon: ImageVector, title: String, body: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(NuFitColors.SecondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = NuFitColors.OnSecondaryContainer, modifier = Modifier.size(22.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
