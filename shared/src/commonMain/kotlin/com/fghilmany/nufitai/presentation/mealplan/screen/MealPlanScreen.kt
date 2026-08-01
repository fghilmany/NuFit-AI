package com.fghilmany.nufitai.presentation.mealplan.screen

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.presentation.mealplan.component.MealCard
import com.fghilmany.nufitai.presentation.mealplan.component.RegenerateSuggestionBanner
import com.fghilmany.nufitai.presentation.mealplan.component.WeekNavigator
import com.fghilmany.nufitai.presentation.mealplan.viewmodel.MealPlanViewModel
import com.fghilmany.nufitai.presentation.monthlyplan.component.BottomNavBar
import com.fghilmany.nufitai.presentation.monthlyplan.component.NavTab
import nufitai.shared.generated.resources.Res
import nufitai.shared.generated.resources.mealplan_empty_state_title
import nufitai.shared.generated.resources.mealplan_generate_cta
import nufitai.shared.generated.resources.mealplan_regenerate_confirm_action
import nufitai.shared.generated.resources.mealplan_regenerate_confirm_body
import nufitai.shared.generated.resources.mealplan_regenerate_confirm_cancel
import nufitai.shared.generated.resources.mealplan_regenerate_confirm_title
import nufitai.shared.generated.resources.mealplan_regenerate_cta
import nufitai.shared.generated.resources.mealplan_reminder_permission_denied_note
import nufitai.shared.generated.resources.mealplan_reminder_toggle_label
import nufitai.shared.generated.resources.mealplan_top_bar_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** P-17 A (techspec meal-plan.md). Tab-switch destination like Chat AI/Home/MyPlans/Account. */
@Composable
fun MealPlanScreen(
    onHomeClick: () -> Unit,
    onPlansClick: () -> Unit,
    onChatAiClick: () -> Unit,
    onAccountClick: () -> Unit,
    onMealClick: (plannedMealId: String) -> Unit,
    onPreferenceClick: () -> Unit,
    viewModel: MealPlanViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(onPreferenceClick = onPreferenceClick)

            WeekNavigator(
                weekStartDate = state.weekStartDate,
                isCurrentWeek = state.weekStartDate == state.currentWeekStart,
                canGoNext = state.canGoNext,
                onPrev = viewModel::onPrevWeek,
                onNext = viewModel::onNextWeek,
            )

            state.error?.let { error -> Text(error, color = NuFitColors.Error, modifier = Modifier.padding(horizontal = 16.dp)) }

            if (state.showRegenerateSuggestion) {
                RegenerateSuggestionBanner(
                    onRegenerateClick = viewModel::onGenerateClick,
                    onDismiss = {},
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            if (state.mealPlan == null) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.mealplan_empty_state_title), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    state.mealsByDay.toSortedMap().forEach { (dayNumber, meals) ->
                        item { Text("Hari $dayNumber", style = MaterialTheme.typography.titleSmall) }
                        items(meals, key = { it.id }) { meal ->
                            MealCard(meal = meal, onClick = { onMealClick(meal.id) })
                        }
                    }
                }
            }

            ReminderRow(
                enabled = state.reminderEnabled,
                hasPermission = state.reminderHasPermission,
                onToggle = viewModel::onToggleReminder,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            if (state.isGenerating) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                AppButton(
                    text = stringResource(if (state.mealPlan == null) Res.string.mealplan_generate_cta else Res.string.mealplan_regenerate_cta),
                    onClick = viewModel::onGenerateClick,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 96.dp),
                )
            }
        }

        BottomNavBar(
            activeTab = NavTab.MAKAN,
            onNavigate = { tab ->
                when (tab) {
                    NavTab.HOME -> onHomeClick()
                    NavTab.RENCANA -> onPlansClick()
                    NavTab.CHAT_AI -> onChatAiClick()
                    NavTab.AKUN -> onAccountClick()
                    else -> Unit
                }
            },
            modifier = Modifier.align(Alignment.BottomStart),
        )

        if (state.pendingRegenerateConfirm) {
            AlertDialog(
                onDismissRequest = viewModel::onDismissRegenerateConfirm,
                title = { Text(stringResource(Res.string.mealplan_regenerate_confirm_title)) },
                text = { Text(stringResource(Res.string.mealplan_regenerate_confirm_body)) },
                confirmButton = {
                    TextButton(onClick = viewModel::onConfirmRegenerate) { Text(stringResource(Res.string.mealplan_regenerate_confirm_action)) }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::onDismissRegenerateConfirm) { Text(stringResource(Res.string.mealplan_regenerate_confirm_cancel)) }
                },
            )
        }
    }
}

@Composable
private fun TopBar(onPreferenceClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(Res.string.mealplan_top_bar_title), style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = onPreferenceClick) {
            Icon(Icons.Filled.Settings, contentDescription = stringResource(Res.string.mealplan_top_bar_title))
        }
    }
}

@Composable
private fun ReminderRow(enabled: Boolean, hasPermission: Boolean, onToggle: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.mealplan_reminder_toggle_label), style = MaterialTheme.typography.bodyMedium)
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
        if (enabled && !hasPermission) {
            Text(
                stringResource(Res.string.mealplan_reminder_permission_denied_note),
                style = MaterialTheme.typography.labelSmall,
                color = NuFitColors.Error,
            )
        }
    }
}
