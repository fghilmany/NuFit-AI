package com.fghilmany.nufitai.presentation.account.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.core.designsystem.component.AppElevatedCard
import com.fghilmany.nufitai.domain.sync.entity.SyncStatus
import com.fghilmany.nufitai.presentation.account.viewmodel.AccountState
import com.fghilmany.nufitai.presentation.account.viewmodel.AccountViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.account_cta_login
import nufitai.shared.generated.resources.account_guest_body
import nufitai.shared.generated.resources.account_guest_title
import nufitai.shared.generated.resources.account_logged_in_label
import nufitai.shared.generated.resources.account_logout_action
import nufitai.shared.generated.resources.account_sync_now_action
import nufitai.shared.generated.resources.account_sync_status_failed
import nufitai.shared.generated.resources.account_sync_status_idle
import nufitai.shared.generated.resources.account_sync_status_success
import nufitai.shared.generated.resources.account_sync_status_syncing
import nufitai.shared.generated.resources.account_top_bar_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** P-15 stub (techspec §3/§6, auth-sync.md) -- full scope stays in 09-profile-settings.md. */
@Composable
fun AccountScreen(onNavigateToLogin: () -> Unit, viewModel: AccountViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(Res.string.account_top_bar_title), style = MaterialTheme.typography.headlineMedium)

        when (val current = state) {
            AccountState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            AccountState.Guest -> GuestContent(onNavigateToLogin)
            is AccountState.LoggedIn -> LoggedInContent(current.syncStatus, onSyncNow = viewModel::syncNowManually, onLogout = viewModel::signOutNow)
            is AccountState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun GuestContent(onNavigateToLogin: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(Res.string.account_guest_title), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(Res.string.account_guest_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AppButton(text = stringResource(Res.string.account_cta_login), onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun LoggedInContent(syncStatus: SyncStatus, onSyncNow: () -> Unit, onLogout: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(Res.string.account_logged_in_label), style = MaterialTheme.typography.titleMedium)
        AppElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Text(syncStatus.label(), modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
        }
        TextButton(onClick = onSyncNow) { Text(stringResource(Res.string.account_sync_now_action)) }
        AppButton(text = stringResource(Res.string.account_logout_action), onClick = onLogout, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun SyncStatus.label(): String = when (this) {
    SyncStatus.Idle -> stringResource(Res.string.account_sync_status_idle)
    SyncStatus.Syncing -> stringResource(Res.string.account_sync_status_syncing)
    is SyncStatus.Success -> {
        val local = lastSyncedAt.toLocalDateTime(TimeZone.currentSystemDefault())
        val time = "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
        stringResource(Res.string.account_sync_status_success, time)
    }
    is SyncStatus.Failed -> stringResource(Res.string.account_sync_status_failed)
}
