package com.fghilmany.nufitai.presentation.onboarding.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.domain.onboarding.entity.ParQQuestionId
import com.fghilmany.nufitai.presentation.onboarding.component.CheckboxRow
import com.fghilmany.nufitai.presentation.onboarding.component.OnboardingTopBar
import com.fghilmany.nufitai.presentation.onboarding.component.shortLabel
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.DoctorConsultEvent
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.DoctorConsultState
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.DoctorConsultViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DoctorConsultScreen(
    onContinue: () -> Unit,
    viewModel: DoctorConsultViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is DoctorConsultState.Continued) onContinue()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OnboardingTopBar(title = "Perhatian Kesehatan")

        Box(modifier = Modifier.weight(1f)) {
            when (val current = state) {
                DoctorConsultState.Loading -> LoadingBox()
                is DoctorConsultState.Content -> DoctorConsultContent(
                    flaggedQuestions = current.flaggedQuestions,
                    acknowledged = current.acknowledged,
                    onToggleAcknowledge = { viewModel.onEvent(DoctorConsultEvent.ToggleAcknowledge) },
                    onContinueClick = { viewModel.onEvent(DoctorConsultEvent.Continue) },
                )
                DoctorConsultState.Continued -> LoadingBox() // brief -- LaunchedEffect above navigates away
                is DoctorConsultState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(current.message) }
            }
        }
    }
}

@Composable
private fun DoctorConsultContent(
    flaggedQuestions: List<ParQQuestionId>,
    acknowledged: Boolean,
    onToggleAcknowledge: () -> Unit,
    onContinueClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(25.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sebaiknya konsultasi ke dokter dulu", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Berdasarkan jawaban Anda, ada kondisi yang sebaiknya didiskusikan dengan dokter " +
                        "sebelum memulai program. Kami tetap menyesuaikan latihan Anda agar aman, tapi " +
                        "konsultasi medis kami sarankan.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        ) {
            Column(modifier = Modifier.padding(25.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Kondisi yang Anda tandai:", style = MaterialTheme.typography.titleSmall)
                flaggedQuestions.forEach { questionId ->
                    Text("• ${questionId.shortLabel()}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        CheckboxRow(
            checked = acknowledged,
            label = "Saya memahami dan akan/sudah berkonsultasi dengan dokter mengenai kondisi ini.",
            onToggle = onToggleAcknowledge,
            modifier = Modifier.padding(top = 16.dp),
        )

        Text(
            "Ini bukan diagnosis medis. Aplikasi ini bukan pengganti konsultasi dengan tenaga medis profesional.",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 8.dp),
        )

        Button(
            onClick = onContinueClick,
            enabled = acknowledged,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        ) {
            Text("Lanjutkan")
        }
    }
}

@Composable
private fun LoadingBox() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
