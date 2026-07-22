package com.fghilmany.nufitai.presentation.onboarding.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.domain.onboarding.entity.ParQQuestionId
import com.fghilmany.nufitai.presentation.onboarding.component.OnboardingTopBar
import com.fghilmany.nufitai.presentation.onboarding.component.ParQQuestionCopy
import com.fghilmany.nufitai.presentation.onboarding.component.QuestionCard
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.ParQEvent
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.ParQState
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.ParQViewModel
import com.fghilmany.nufitai.ui.theme.NuFitColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ParQScreen(
    onSubmitted: (requiresDoctorConsult: Boolean, parQResultId: String) -> Unit,
    viewModel: ParQViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        val submitted = state as? ParQState.Submitted ?: return@LaunchedEffect
        onSubmitted(submitted.requiresDoctorConsult, submitted.parQResultId)
    }

    when (val current = state) {
        is ParQState.Questions -> ParQContent(
            answers = current.answers,
            canProceed = current.canProceed,
            onAnswer = { questionId, value -> viewModel.onEvent(ParQEvent.Answer(questionId, value)) },
            onSubmit = { viewModel.onEvent(ParQEvent.Submit) },
        )
        ParQState.Submitting -> LoadingScreen()
        is ParQState.Submitted -> LoadingScreen() // brief -- LaunchedEffect above navigates away
        is ParQState.Error -> ErrorScreen(current.message)
    }
}

@Composable
private fun ParQContent(
    answers: Map<ParQQuestionId, Boolean?>,
    canProceed: Boolean,
    onAnswer: (ParQQuestionId, Boolean) -> Unit,
    onSubmit: () -> Unit,
) {
    val answeredCount = answers.values.count { it != null }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        OnboardingTopBar(
            title = "Cek Kesehatan Dulu",
            trailing = { Icon(Icons.Filled.Shield, contentDescription = null, tint = NuFitColors.Primary) },
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { ProgressRow(answeredCount) }
            item { IntroCard() }
            items(ParQQuestionCopy) { (questionId, text) ->
                val index = ParQQuestionCopy.indexOfFirst { it.first == questionId } + 1
                QuestionCard(
                    questionNumber = index,
                    questionText = text,
                    selected = answers[questionId],
                    onAnswer = { value -> onAnswer(questionId, value) },
                )
            }
            item { SafetyNote() }
        }

        Button(
            onClick = onSubmit,
            enabled = canProceed,
            modifier = Modifier.fillMaxWidth().padding(24.dp),
        ) {
            Text("Lanjutkan")
        }
    }
}

@Composable
private fun ProgressRow(answeredCount: Int) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "LANGKAH 1 DARI 1",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
            Text(
                "$answeredCount / ${ParQQuestionCopy.size} Dijawab",
                style = MaterialTheme.typography.labelLarge,
                color = NuFitColors.Primary,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        )
    }
}

@Composable
private fun IntroCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(NuFitColors.SecondaryContainer.copy(alpha = 0.3f))
            .padding(25.dp),
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(NuFitColors.PrimaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.HealthAndSafety, contentDescription = null, tint = NuFitColors.OnPrimary, modifier = Modifier.size(20.dp))
        }
        Text(
            "Sebelum mulai, mari pastikan latihan Anda aman. Jawab pertanyaan berikut dengan " +
                "jujur agar kami bisa menyesuaikan program yang tepat untuk Anda.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

@Composable
private fun SafetyNote() {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Icon(
            Icons.Filled.Shield,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp),
        )
        Text(
            "Data Anda dijaga kerahasiaannya dan hanya digunakan untuk optimasi algoritma latihan NuFit AI.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message)
    }
}
