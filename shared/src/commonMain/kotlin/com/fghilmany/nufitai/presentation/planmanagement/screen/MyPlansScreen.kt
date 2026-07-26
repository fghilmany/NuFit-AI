package com.fghilmany.nufitai.presentation.planmanagement.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppCard
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanStatus
import com.fghilmany.nufitai.presentation.monthlyplan.component.BottomNavBar
import com.fghilmany.nufitai.presentation.monthlyplan.component.NavTab
import com.fghilmany.nufitai.presentation.monthlyplan.component.QuickAccessBanner
import com.fghilmany.nufitai.presentation.planmanagement.component.PlanCard
import com.fghilmany.nufitai.presentation.planmanagement.viewmodel.MyPlansEvent
import com.fghilmany.nufitai.presentation.planmanagement.viewmodel.MyPlansState
import com.fghilmany.nufitai.presentation.planmanagement.viewmodel.MyPlansViewModel
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.planmanagement_confirm_activate_body
import nufitai.shared.generated.resources.planmanagement_confirm_activate_title
import nufitai.shared.generated.resources.planmanagement_confirm_cancel
import nufitai.shared.generated.resources.planmanagement_confirm_delete_body
import nufitai.shared.generated.resources.planmanagement_confirm_delete_title
import nufitai.shared.generated.resources.planmanagement_confirm_ok
import nufitai.shared.generated.resources.planmanagement_empty_state
import nufitai.shared.generated.resources.planmanagement_entry_ai_badge
import nufitai.shared.generated.resources.planmanagement_entry_ai_title
import nufitai.shared.generated.resources.planmanagement_entry_template_badge
import nufitai.shared.generated.resources.planmanagement_entry_template_title
import nufitai.shared.generated.resources.planmanagement_quota_label
import nufitai.shared.generated.resources.planmanagement_section_title
import nufitai.shared.generated.resources.planmanagement_top_bar_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** P-14 (issue #82, Figma node 12:1251). */
@Composable
fun MyPlansScreen(
    onCreateFromTemplate: () -> Unit,
    onCreateWithAi: () -> Unit,
    onExerciseLibraryClick: () -> Unit,
    onHomeClick: () -> Unit,
    viewModel: MyPlansViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val current = state) {
            MyPlansState.Loading -> LoadingBox()
            is MyPlansState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(current.message) }
            is MyPlansState.Loaded -> LoadedContent(current, viewModel, onCreateFromTemplate, onCreateWithAi, onExerciseLibraryClick)
        }

        BottomNavBar(
            activeTab = NavTab.RENCANA,
            onNavigate = { tab -> if (tab == NavTab.HOME) onHomeClick() },
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}

@Composable
private fun LoadedContent(
    state: MyPlansState.Loaded,
    viewModel: MyPlansViewModel,
    onCreateFromTemplate: () -> Unit,
    onCreateWithAi: () -> Unit,
    onExerciseLibraryClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { TopBarSection() }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                CreateEntryCard(
                    icon = Icons.Filled.Description,
                    title = stringResource(Res.string.planmanagement_entry_template_title),
                    badge = stringResource(Res.string.planmanagement_entry_template_badge),
                    badgeColor = NuFitColors.SecondaryContainer,
                    onClick = onCreateFromTemplate,
                    modifier = Modifier.weight(1f),
                )
                CreateEntryCard(
                    icon = Icons.Filled.AutoAwesome,
                    title = stringResource(Res.string.planmanagement_entry_ai_title),
                    badge = stringResource(Res.string.planmanagement_entry_ai_badge),
                    badgeColor = NuFitColors.PrimaryContainer,
                    onClick = onCreateWithAi,
                    modifier = Modifier.weight(1f),
                    isPrimary = true,
                )
            }
        }

        item { QuickAccessBanner(onClick = onExerciseLibraryClick) }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.planmanagement_section_title), style = MaterialTheme.typography.titleLarge)
                QuotaPill(state.quotaCount, state.quotaMax)
            }
        }

        if (state.plans.isEmpty()) {
            item { Text(stringResource(Res.string.planmanagement_empty_state), style = MaterialTheme.typography.bodyMedium) }
        }

        items(state.plans) { plan ->
            PlanCard(
                plan = plan,
                progressPercent = if (plan.status == PlanStatus.ACTIVE) state.activePlanProgressPercent else null,
                isBusy = state.isBusy,
                onActivateOrRestore = { viewModel.onEvent(MyPlansEvent.RequestActivate(plan)) },
                onDelete = { viewModel.onEvent(MyPlansEvent.RequestDelete(plan)) },
            )
        }
    }

    state.confirmActivateTarget?.let {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(MyPlansEvent.CancelActivate) },
            title = { Text(stringResource(Res.string.planmanagement_confirm_activate_title)) },
            text = { Text(stringResource(Res.string.planmanagement_confirm_activate_body)) },
            confirmButton = { TextButton(onClick = { viewModel.onEvent(MyPlansEvent.ConfirmActivate) }) { Text(stringResource(Res.string.planmanagement_confirm_ok)) } },
            dismissButton = { TextButton(onClick = { viewModel.onEvent(MyPlansEvent.CancelActivate) }) { Text(stringResource(Res.string.planmanagement_confirm_cancel)) } },
        )
    }

    state.confirmDeleteTarget?.let {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(MyPlansEvent.CancelDelete) },
            title = { Text(stringResource(Res.string.planmanagement_confirm_delete_title)) },
            text = { Text(stringResource(Res.string.planmanagement_confirm_delete_body)) },
            confirmButton = { TextButton(onClick = { viewModel.onEvent(MyPlansEvent.ConfirmDelete) }) { Text(stringResource(Res.string.planmanagement_confirm_ok)) } },
            dismissButton = { TextButton(onClick = { viewModel.onEvent(MyPlansEvent.CancelDelete) }) { Text(stringResource(Res.string.planmanagement_confirm_cancel)) } },
        )
    }
}

@Composable
private fun TopBarSection() {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(Res.string.planmanagement_top_bar_title), style = MaterialTheme.typography.headlineMedium, color = NuFitColors.Primary)
        IconButton(onClick = {}) {
            Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun CreateEntryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    badge: String,
    badgeColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
) {
    AppCard(
        modifier = modifier.clickable(onClick = onClick),
        backgroundColor = if (isPrimary) NuFitColors.Primary else MaterialTheme.colorScheme.surfaceContainerLowest,
        contentPadding = PaddingValues(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = if (isPrimary) NuFitColors.OnPrimary else MaterialTheme.colorScheme.onSurface)
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isPrimary) NuFitColors.OnPrimary else MaterialTheme.colorScheme.onSurface,
            )
            Box(
                modifier = Modifier.background(badgeColor, RoundedCornerShape(percent = 50)).padding(horizontal = 10.dp, vertical = 2.dp),
            ) {
                Text(badge, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun QuotaPill(count: Int, max: Int) {
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(percent = 50)).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(stringResource(Res.string.planmanagement_quota_label, count, max), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
