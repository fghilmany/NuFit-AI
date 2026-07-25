package com.fghilmany.nufitai.presentation.monthlyplan.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.presentation.monthlyplan.component.AssessmentDetailBanner
import com.fghilmany.nufitai.presentation.monthlyplan.component.BottomNavBar
import com.fghilmany.nufitai.presentation.monthlyplan.component.QuickAccessBanner
import com.fghilmany.nufitai.presentation.monthlyplan.component.SessionCard
import com.fghilmany.nufitai.presentation.monthlyplan.component.WeekAccordionHeader
import com.fghilmany.nufitai.presentation.monthlyplan.component.WeekCompleteCard
import com.fghilmany.nufitai.presentation.monthlyplan.viewmodel.HomeEvent
import com.fghilmany.nufitai.presentation.monthlyplan.viewmodel.HomeState
import com.fghilmany.nufitai.presentation.monthlyplan.viewmodel.HomeViewModel
import com.fghilmany.nufitai.presentation.monthlyplan.viewmodel.WeekEntry
import com.fghilmany.nufitai.presentation.onboarding.component.ProgressBar
import com.fghilmany.nufitai.core.designsystem.theme.NuFitColors
import com.fghilmany.nufitai.usecase.monthlyplan.SessionStatus
import org.koin.compose.viewmodel.koinViewModel

/** issue #76 P-03 (Figma node 12:539). */
@Composable
fun HomeScreen(
    onSessionClick: (planDayId: String) -> Unit,
    onStartSession: (planDayId: String) -> Unit,
    onAssessmentDetailClick: () -> Unit,
    onExerciseLibraryClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val current = state) {
            HomeState.Loading -> LoadingBox()
            HomeState.Empty -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Belum ada plan aktif", style = MaterialTheme.typography.bodyLarge)
            }
            is HomeState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(current.message) }
            is HomeState.Loaded -> HomeContent(current, viewModel, onSessionClick, onStartSession, onAssessmentDetailClick, onExerciseLibraryClick)
        }

        TopAppBarSection(modifier = Modifier.align(Alignment.TopStart))
        BottomNavBar(modifier = Modifier.align(Alignment.BottomStart))
    }
}

@Composable
private fun HomeContent(
    state: HomeState.Loaded,
    viewModel: HomeViewModel,
    onSessionClick: (String) -> Unit,
    onStartSession: (String) -> Unit,
    onAssessmentDetailClick: () -> Unit,
    onExerciseLibraryClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 104.dp, bottom = 96.dp), // 64dp bar + status-bar inset buffer
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        item { GreetingSection(state.currentWeekNumber, state.progressPercent) }

        item {
            val currentWeek = state.weeks.find { it.weekNumber == state.currentWeekNumber }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Sesi Minggu Ini", style = MaterialTheme.typography.titleLarge)
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    currentWeek?.days.orEmpty().forEach { day ->
                        val status = currentWeek?.statuses?.get(day.id) ?: SessionStatus.UPCOMING
                        SessionCard(day, status, onClick = { onSessionClick(day.id) }, onStartClick = { onStartSession(day.id) })
                    }
                    if (state.weekCompleteMessage != null) {
                        WeekCompleteCard(state.weekCompleteMessage)
                    }
                }
            }
        }

        item {
            QuickAccessBanner(onClick = onExerciseLibraryClick)
        }

        item {
            AssessmentDetailBanner(onClick = onAssessmentDetailClick)
        }

        items(state.weeks.filter { it.weekNumber != state.currentWeekNumber }) { week ->
            WeekSection(week, state.expandedWeek, viewModel, onSessionClick, onStartSession)
        }
    }
}

@Composable
private fun WeekSection(
    week: WeekEntry,
    expandedWeek: Int?,
    viewModel: HomeViewModel,
    onSessionClick: (String) -> Unit,
    onStartSession: (String) -> Unit,
) {
    val isExpanded = week.weekNumber == expandedWeek
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WeekAccordionHeader(
            weekNumber = week.weekNumber,
            isExpanded = isExpanded,
            onClick = { viewModel.onEvent(HomeEvent.ToggleWeekExpanded(week.weekNumber)) },
        )
        if (isExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 8.dp)) {
                week.days.forEach { day ->
                    val status = week.statuses[day.id] ?: SessionStatus.UPCOMING
                    SessionCard(day, status, onClick = { onSessionClick(day.id) }, onStartClick = { onStartSession(day.id) })
                }
            }
        }
    }
}

@Composable
private fun GreetingSection(currentWeekNumber: Int, progressPercent: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Halo, User! 👋", style = MaterialTheme.typography.headlineLarge)
            Text("Minggu $currentWeekNumber dari 4", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Progress: $progressPercent%", style = MaterialTheme.typography.bodyMedium, color = NuFitColors.Primary)
            ProgressBar(progress = progressPercent / 100f, modifier = Modifier.width(96.dp))
        }
    }
}

@Composable
private fun TopAppBarSection(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(NuFitColors.PrimaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = NuFitColors.OnPrimaryContainer)
            }
            Text("NuFit AI", style = MaterialTheme.typography.headlineMedium, color = NuFitColors.Primary)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Filled.Notifications, contentDescription = "Notifikasi", tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
