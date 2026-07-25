package com.fghilmany.nufitai.presentation.onboarding.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fghilmany.nufitai.core.designsystem.component.AppButton
import com.fghilmany.nufitai.core.designsystem.component.AppTextField
import com.fghilmany.nufitai.presentation.onboarding.component.OnboardingTopBar
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.BodyDataEvent
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.BodyDataState
import com.fghilmany.nufitai.presentation.onboarding.viewmodel.BodyDataViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BodyDataScreen(
    onDone: () -> Unit,
    viewModel: BodyDataViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is BodyDataState.Saved || state is BodyDataState.Skipped) onDone()
    }

    val content = state as? BodyDataState.Content ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        OnboardingTopBar(title = "Data Tubuh")

        Column(
            modifier = Modifier.weight(1f).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Data tubuh (opsional)", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Isi kalau mau, atau lewati saja -- kamu tetap bisa mengisinya nanti di pengaturan.",
                style = MaterialTheme.typography.bodyMedium,
            )

            AppTextField(
                value = content.heightCm,
                onValueChange = { viewModel.onEvent(BodyDataEvent.UpdateHeight(it)) },
                label = "Tinggi badan (cm)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            AppTextField(
                value = content.weightKg,
                onValueChange = { viewModel.onEvent(BodyDataEvent.UpdateWeight(it)) },
                label = "Berat badan (kg)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            AppButton(
                onClick = { viewModel.onEvent(BodyDataEvent.Save) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Simpan")
            }
            OutlinedButton(
                onClick = { viewModel.onEvent(BodyDataEvent.Skip) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Lewati")
            }
        }
    }
}
