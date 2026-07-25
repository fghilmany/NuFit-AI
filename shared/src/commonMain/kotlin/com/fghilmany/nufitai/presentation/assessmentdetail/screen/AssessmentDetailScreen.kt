package com.fghilmany.nufitai.presentation.assessmentdetail.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.domain.exerciselibrary.entity.ExerciseFlag
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.monthlyplan.entity.PlanSource
import com.fghilmany.nufitai.presentation.assessmentdetail.component.AnswerSummaryGrid
import com.fghilmany.nufitai.presentation.assessmentdetail.component.AssessmentHistoryList
import com.fghilmany.nufitai.presentation.assessmentdetail.component.FlagExplanationCard
import com.fghilmany.nufitai.presentation.assessmentdetail.component.FullAssessmentCtaCard
import com.fghilmany.nufitai.presentation.assessmentdetail.component.LevelBadgeCard
import com.fghilmany.nufitai.presentation.assessmentdetail.component.RetakeFooterLink
import com.fghilmany.nufitai.presentation.assessmentdetail.component.SelectedPlanCard
import com.fghilmany.nufitai.presentation.assessmentdetail.component.explanationImpact
import com.fghilmany.nufitai.presentation.assessmentdetail.component.shortLabel
import com.fghilmany.nufitai.presentation.assessmentdetail.viewmodel.AssessmentDetailEvent
import com.fghilmany.nufitai.presentation.assessmentdetail.viewmodel.AssessmentDetailState
import com.fghilmany.nufitai.presentation.assessmentdetail.viewmodel.AssessmentDetailTab
import com.fghilmany.nufitai.presentation.assessmentdetail.viewmodel.AssessmentDetailViewModel
import com.fghilmany.nufitai.presentation.assessmentdetail.viewmodel.RetakeDialogState
import com.fghilmany.nufitai.presentation.fullassessment.component.shortLabel as exerciseFlagShortLabel
import com.fghilmany.nufitai.ui.theme.NuFitColors
import com.fghilmany.nufitai.usecase.assessmentdetail.AssessmentDetailSummary
import org.koin.compose.viewmodel.koinViewModel

/** P-09 (03-assessment-detail.md, issue #77, Figma node 12:987). */
@Composable
fun AssessmentDetailScreen(
    onBack: () -> Unit,
    onRetakeLocal: () -> Unit,
    onRetakeLoggedIn: () -> Unit,
    onOpenFullAssessment: () -> Unit,
    viewModel: AssessmentDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        val current = state
        if (current is AssessmentDetailState.RetakeReady) {
            when (current.source) {
                PlanSource.LOCAL_TEMPLATE -> onRetakeLocal()
                PlanSource.LOGGED_IN_RULE_ENGINE -> onRetakeLoggedIn()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val current = state) {
            AssessmentDetailState.Loading, is AssessmentDetailState.RetakeReady -> LoadingBox()
            is AssessmentDetailState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(current.message) }
            is AssessmentDetailState.Loaded -> AssessmentDetailContent(current, viewModel, onOpenFullAssessment)
        }

        TopBar(onBack = onBack, modifier = Modifier.align(Alignment.TopStart))
    }
}

@Composable
private fun AssessmentDetailContent(
    state: AssessmentDetailState.Loaded,
    viewModel: AssessmentDetailViewModel,
    onOpenFullAssessment: () -> Unit,
) {
    when (state.retakeDialog) {
        RetakeDialogState.Confirming -> RetakeConfirmDialog(
            onConfirm = { viewModel.onEvent(AssessmentDetailEvent.ConfirmRetake) },
            onCancel = { viewModel.onEvent(AssessmentDetailEvent.CancelRetake) },
        )
        RetakeDialogState.Blocked -> RetakeBlockedDialog(onDismiss = { viewModel.onEvent(AssessmentDetailEvent.CancelRetake) })
        RetakeDialogState.Hidden -> Unit
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabBar(
            activeTab = state.activeTab,
            onTabSelected = { viewModel.onEvent(AssessmentDetailEvent.SwitchTab(it)) },
            modifier = Modifier.padding(top = 80.dp),
        )

        when (state.activeTab) {
            AssessmentDetailTab.HASIL -> HasilTabContent(
                summary = state.summary,
                onRetakeClick = { viewModel.onEvent(AssessmentDetailEvent.RequestRetake) },
                onOpenFullAssessment = onOpenFullAssessment,
            )
            AssessmentDetailTab.RIWAYAT -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            ) {
                item { AssessmentHistoryList(state.history) }
            }
        }
    }
}

@Composable
private fun HasilTabContent(summary: AssessmentDetailSummary, onRetakeClick: () -> Unit, onOpenFullAssessment: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        item { LevelBadgeCard(summary.quickAssessment) }

        val healthFlags = summary.parQResult?.flagsGenerated.orEmpty()
        if (healthFlags.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    healthFlags.forEach { flag -> FlagExplanationCard(flag.shortLabel(), flag.explanationImpact()) }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Ringkasan Jawaban", style = MaterialTheme.typography.titleLarge)
                AnswerSummaryGrid(summary.quickAssessment)
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Rencana Terpilih", style = MaterialTheme.typography.titleLarge)
                SelectedPlanCard(summary.quickAssessment, summary.activePlan)
            }
        }

        val fullAssessment = summary.fullAssessment
        if (fullAssessment != null) {
            item { FullAssessmentDetailsSection(fullAssessment) }
        } else {
            item { FullAssessmentCtaCard(onClick = onOpenFullAssessment) } // AC-2: routes to Route.FullAssessmentStub, a placeholder pending Auth & Sync (08-auth-sync.md)
        }

        item { Box(Modifier.fillMaxWidth(), Alignment.Center) { RetakeFooterLink(onClick = onRetakeClick) } }
    }
}

/** Full-tier alternate flow (UC-1) -- BMI, full PAR-Q flags, self-test, each independently null-safe. */
@Composable
private fun FullAssessmentDetailsSection(fullAssessment: FullAssessmentResult, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Data Full Assessment", style = MaterialTheme.typography.titleLarge)

        val allFlags: Set<ExerciseFlag> = fullAssessment.parQKategoriB + fullAssessment.flagsPostural + fullAssessment.flagsGerak
        allFlags.forEach { flag -> FlagExplanationCard(flag.exerciseFlagShortLabel(), flag.explanationImpact()) }

        val capacityTest = fullAssessment.capacityTest
        Text(
            "Push-up: ${capacityTest?.pushup?.reps?.let { "$it reps" } ?: "Belum diisi"}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            "Plank: ${capacityTest?.plank?.detik?.let { "$it detik" } ?: "Belum diisi"}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            "Sit-to-stand: ${capacityTest?.sitToStand?.reps?.let { "$it reps" } ?: "Belum diisi"}",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun TabBar(activeTab: AssessmentDetailTab, onTabSelected: (AssessmentDetailTab) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        TabItem("Hasil", isActive = activeTab == AssessmentDetailTab.HASIL, onClick = { onTabSelected(AssessmentDetailTab.HASIL) })
        TabItem("Riwayat", isActive = activeTab == AssessmentDetailTab.RIWAYAT, onClick = { onTabSelected(AssessmentDetailTab.RIWAYAT) })
    }
}

@Composable
private fun TabItem(label: String, isActive: Boolean, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        color = if (isActive) NuFitColors.Primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.clickable(onClick = onClick).padding(bottom = 8.dp),
    )
}

@Composable
private fun RetakeConfirmDialog(onConfirm: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Ulangi Assessment?") },
        text = { Text("Plan aktif saat ini akan diarsipkan dan kamu akan menjalani assessment dari awal. Riwayat log kamu tetap aman.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Ulangi") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Batal") } },
    )
}

@Composable
private fun RetakeBlockedDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Belum bisa mengulang assessment") },
        text = { Text("Selesaikan atau akhiri sesi latihan yang sedang berjalan terlebih dahulu sebelum mengulang assessment.") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Mengerti") } },
    )
}

@Composable
private fun TopBar(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .height(64.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
            }
            Text("Hasil Assessment", style = MaterialTheme.typography.titleMedium)
        }
        Icon(Icons.Filled.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) // inert -- no share target this pass
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
